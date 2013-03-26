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
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.object.FastMapLongObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public class MapZonedDateTimeObjectTimeSeries<T> extends MutableZonedDateTimeObjectTimeSeries.Long<T> {
  private static final DateTimeConverter<ZonedDateTime> s_converter = new ZonedDateTimeEpochMillisConverter();

  public MapZonedDateTimeObjectTimeSeries() {
    super(new ZonedDateTimeEpochMillisConverter(), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }
  
  public MapZonedDateTimeObjectTimeSeries(ZoneId timeZone) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS));
  }

  public MapZonedDateTimeObjectTimeSeries(final ZonedDateTime[] dates, final T[] values) {
    super(s_converter, new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapZonedDateTimeObjectTimeSeries(final ZoneId timeZone, final ZonedDateTime[] dates, final T[] values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapZonedDateTimeObjectTimeSeries(final List<ZonedDateTime> dates, final List<T> values) {
    super(s_converter, new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public MapZonedDateTimeObjectTimeSeries(final ZoneId timeZone, final List<ZonedDateTime> dates, final List<T> values) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new ZonedDateTimeEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public MapZonedDateTimeObjectTimeSeries(final ZonedDateTimeObjectTimeSeries<T> dts) {
    super(s_converter, (FastMutableLongObjectTimeSeries<T>) s_converter.convertToLong(new FastMapLongObjectTimeSeries<T>(DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public MapZonedDateTimeObjectTimeSeries(final ZoneId timeZone, final ObjectTimeSeries<ZonedDateTime, T> dts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), (FastMutableLongObjectTimeSeries<T>) new ZonedDateTimeEpochMillisConverter(timeZone).convertToLong(new FastMapLongObjectTimeSeries(
        DateTimeNumericEncoding.TIME_EPOCH_MILLIS), dts));
  }

  public MapZonedDateTimeObjectTimeSeries(final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public MapZonedDateTimeObjectTimeSeries(final ZoneId timeZone, final FastMutableLongObjectTimeSeries<T> pmidts) {
    super(new ZonedDateTimeEpochMillisConverter(timeZone), pmidts);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<T> newInstanceFast(final ZonedDateTime[] dateTimes, final T[] values) {
    return new MapZonedDateTimeObjectTimeSeries<T>(((ZonedDateTimeEpochMillisConverter) getConverter()).getTimeZone310(), dateTimes, values);
  }
}
