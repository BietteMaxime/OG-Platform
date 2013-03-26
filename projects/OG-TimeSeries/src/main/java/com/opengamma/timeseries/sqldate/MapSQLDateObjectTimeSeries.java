/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.sqldate;

import java.sql.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.object.FastMapIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public class MapSQLDateObjectTimeSeries<T> extends MutableSQLDateObjectTimeSeries.Integer<T> {
  /** An empty time series */
  public static final MapSQLDateObjectTimeSeries<?> EMPTY_SERIES = new MapSQLDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<Date> s_converter = new SQLDateEpochDaysConverter();

  public MapSQLDateObjectTimeSeries() {
    super(new SQLDateEpochDaysConverter(), new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public MapSQLDateObjectTimeSeries(final Date[] dates, final T[] values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public MapSQLDateObjectTimeSeries(final TimeZone timeZone, final Date[] dates, final T[] values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapSQLDateObjectTimeSeries(final List<Date> dates, final List<T> values) {
    super(s_converter, new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public MapSQLDateObjectTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<T> values) {
    super(new SQLDateEpochDaysConverter(timeZone), new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new SQLDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public MapSQLDateObjectTimeSeries(final ObjectTimeSeries<Date, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt(new FastMapIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapSQLDateObjectTimeSeries(final TimeZone timeZone, final ObjectTimeSeries<Date, T> dts) {
    super(new SQLDateEpochDaysConverter(timeZone), (FastMutableIntObjectTimeSeries<T>) new SQLDateEpochDaysConverter(
        timeZone).convertToInt(new FastMapIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS), dts));
  }

  public MapSQLDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public MapSQLDateObjectTimeSeries(final TimeZone timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new SQLDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public MutableSQLDateObjectTimeSeries<T> newInstanceFast(final Date[] dateTimes, final T[] values) {
    return new MapSQLDateObjectTimeSeries<T>(dateTimes, values);
  }
}
