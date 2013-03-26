/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.time;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastListLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public class ArrayDateTimeDoubleTimeSeries extends DateTimeDoubleTimeSeries.Long {
  private static final FastListLongDoubleTimeSeries DEFAULT_SERIES_TEMPLATE = new FastListLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
  @SuppressWarnings("unused")
  public static final ArrayDateTimeDoubleTimeSeries EMPTY_SERIES = new ArrayDateTimeDoubleTimeSeries();
  private static final DateEpochMillisConverter s_converter = new DateEpochMillisConverter();

  public ArrayDateTimeDoubleTimeSeries() { 
    super(new DateEpochMillisConverter(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  public ArrayDateTimeDoubleTimeSeries(final Date[] dates, final double[] values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final Date[] dates, final double[] values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final List<Date> dates, final List<Double> values) {
    super(s_converter, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, s_converter.convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final List<Date> dates, final List<Double> values) {
    super(new DateEpochMillisConverter(timeZone), new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, new DateEpochMillisConverter(timeZone)
        .convertToLong(dates), values));
  }

  public ArrayDateTimeDoubleTimeSeries(final DoubleTimeSeries<Date> dts) {
    super(s_converter, s_converter.convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final DoubleTimeSeries<Date> dts) {
    super(new DateEpochMillisConverter(timeZone), new DateEpochMillisConverter(timeZone).convertToLong(DEFAULT_SERIES_TEMPLATE, dts));
  }

  public ArrayDateTimeDoubleTimeSeries(final FastLongDoubleTimeSeries pidts) {
    super(s_converter, pidts);
  }
  
  public ArrayDateTimeDoubleTimeSeries(final DateTimeConverter<Date> converter, final FastLongDoubleTimeSeries pidts) {
    super(converter, pidts);
  }

  public ArrayDateTimeDoubleTimeSeries(final TimeZone timeZone, final FastLongDoubleTimeSeries pidts) {
    super(new DateEpochMillisConverter(timeZone), pidts);
  }

  @Override
  public DateTimeDoubleTimeSeries newInstanceFast(final Date[] dateTimes, final double[] values) {
    return new ArrayDateTimeDoubleTimeSeries(((DateEpochMillisConverter) getConverter()).getTimeZone(), dateTimes, values);
  }


}
