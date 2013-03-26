/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.zoneddatetime;

import java.util.List;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastMapLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * 
 */
public class MapZonedDateTimeDoubleTimeSeries extends MutableZonedDateTimeDoubleTimeSeries.Long {
  private static final DateTimeConverter<ZonedDateTime> s_converter = new ZonedDateTimeEpochMillisConverter();

  public MapZonedDateTimeDoubleTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapZonedDateTimeDoubleTimeSeries(ZoneId timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public MapZonedDateTimeDoubleTimeSeries(final ZonedDateTime[] dates, final double[] values) {
    super(s_converter, new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final ZonedDateTime[] dates, final double[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapZonedDateTimeDoubleTimeSeries(final List<ZonedDateTime> dates, final List<Double> values) {
    super(s_converter, new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final List<ZonedDateTime> dates, final List<Double> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapZonedDateTimeDoubleTimeSeries(final ZonedDateTimeDoubleTimeSeries dts) {
    super(s_converter, (FastMutableLongDoubleTimeSeries) s_converter.convertToLong(new FastMapLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }
  
  public MapZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final DoubleTimeSeries<ZonedDateTime> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), (FastMutableLongDoubleTimeSeries) new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(new FastMapLongDoubleTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapZonedDateTimeDoubleTimeSeries(final FastMutableLongDoubleTimeSeries pmidts) {
    super(s_converter, pmidts);
  }
  
  public MapZonedDateTimeDoubleTimeSeries(final DateTimeConverter<ZonedDateTime> converter, final FastMutableLongDoubleTimeSeries pmidts) {
    super(converter, pmidts);
  }

  public MapZonedDateTimeDoubleTimeSeries(final ZoneId timeZone, final FastMutableLongDoubleTimeSeries pmidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public MapZonedDateTimeDoubleTimeSeries newInstanceFast(final ZonedDateTime[] dateTimes, final double[] values) {
    return new MapZonedDateTimeDoubleTimeSeries(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
