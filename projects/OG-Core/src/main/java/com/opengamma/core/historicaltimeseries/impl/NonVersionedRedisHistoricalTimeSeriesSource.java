/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.JedisPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.MetricProducer;
import com.opengamma.util.tuple.Pair;

/**
 * An extremely minimal and lightweight {@code HistoricalTimeSeriesSource} that pulls data
 * directly from Redis for situations where versioning, multiple fields, multiple data sources,
 * and identifier management is not necessary.
 * <p/>
 * The following constraints must hold for this Source to be of any utility whatsoever:
 * <ul>
 *   <li>Historical lookups are not required. Because they are not supported.</li>
 *   <li>Version corrections are not required. Because they are not supported.</li>
 *   <li>Each time series has a <b>single</b> {@link ExternalId} which then acts
 *       as the {@link UniqueId} internally.</li>
 *   <li>Each external ID has a single time series (thus there is not the capacity to store
 *       different Data Source, Data Provider, Observation Time, Data Field series).</li>
 * </ul>
 * <p/>
 * Where a method is not supported semantically, an {@link UnsupportedOperationException}
 * will be thrown. Where use indicates that this class may be being used incorrectly,
 * a log message will be written at {@code WARN} level.
 */
public class NonVersionedRedisHistoricalTimeSeriesSource extends AbstractRedisHistoricalTimeSeriesSource implements MetricProducer {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisHistoricalTimeSeriesSource.class);
  private Timer _getSeriesTimer = new Timer();
  private Timer _getPointTimer = new Timer();
  
  public NonVersionedRedisHistoricalTimeSeriesSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisHistoricalTimeSeriesSource(JedisPool jedisPool, String redisPrefix) {
    super(jedisPool, redisPrefix);
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getSeriesTimer = summaryRegistry.timer(namePrefix + ".get");
    _getPointTimer = summaryRegistry.timer(namePrefix + ".getPoint");
  }

  public void setTimeSeriesPoint(UniqueId uniqueId, LocalDate valueDate, double value) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(valueDate, "valueDate");
    String redisKey = toRedisKey(uniqueId);
    
    setTimeSeriesPoint(redisKey, valueDate, value);
  }

  @Override
  protected Timer getSeriesLoadTimer() {
    return _getSeriesTimer;
  }
  
  protected UniqueId toUniqueId(ExternalIdBundle identifierBundle) {
    if (identifierBundle.size() != 1) {
      s_logger.warn("Using NonVersionedRedisHistoricalTimeSeriesSource with bundle {} other than 1. Probable misuse.", identifierBundle);
    }
    ExternalId id = identifierBundle.iterator().next();
    UniqueId uniqueId = UniqueId.of(id.getScheme().getName(), id.getValue());
    return uniqueId;
  }
  
  // ------------------------------------------------------------------------
  // SUPPORTED HISTORICAL TIME SERIES SOURCE OPERATIONS:
  // ------------------------------------------------------------------------
  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    try (Timer.Context context = _getPointTimer.time()) {
      
      Map<String, String> valuesFromRedis = loadValuesFromRedis(uniqueId);
      
      if ((valuesFromRedis == null) || valuesFromRedis.isEmpty()) {
        return null;
      }
      
      SortedMap<String, String> sortedValues = new TreeMap<String, String>(valuesFromRedis);
      return Pair.of(
          LocalDate.parse(sortedValues.lastKey()),
          Double.parseDouble(sortedValues.get(sortedValues.lastKey())));
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(uniqueId);
    if (ts == null) {
      return null;
    }
    
    if (start != null) {
      ArgumentChecker.notNull(end, "end");
      ts = ts.subSeries(start, includeStart, end, includeEnd);
    }
    return Pair.of(ts.getLatestTime(), ts.getLatestValue());
  }
  
  protected LocalDateDoubleTimeSeries getLocalDateDoubleTimeSeries(ExternalIdBundle identifierBundle) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    
    LocalDateDoubleTimeSeries ts = loadTimeSeriesFromRedis(uniqueId);
    return ts;
  }
  
  protected HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    
    LocalDateDoubleTimeSeries ts = getLocalDateDoubleTimeSeries(identifierBundle);
    HistoricalTimeSeries hts = new SimpleHistoricalTimeSeries(uniqueId, ts);
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifierBundle);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifierBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getHistoricalTimeSeries(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    UniqueId uniqueId = toUniqueId(identifierBundle);
    return getLatestDataPoint(uniqueId);
  }

}
