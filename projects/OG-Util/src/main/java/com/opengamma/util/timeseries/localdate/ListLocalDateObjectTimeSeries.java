/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastListIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * @param <T> The type of the data
 */
public class ListLocalDateObjectTimeSeries<T> extends MutableLocalDateObjectTimeSeries.Integer<T> {
  /** A template time series with date encoding */
  public static final FastIntObjectTimeSeries<?> TIMESERIES_TEMPLATE = new FastListIntObjectTimeSeries<Object>(
      DateTimeNumericEncoding.DATE_EPOCH_DAYS);
  /** An empty time series */
  public static final ListLocalDateObjectTimeSeries<?> EMPTY_SERIES = new ListLocalDateObjectTimeSeries<Object>();
  private static final DateTimeConverter<LocalDate> s_converter = new LocalDateEpochDaysConverter();

  protected ListLocalDateObjectTimeSeries(final DateTimeConverter<LocalDate> converter,
      final FastMutableIntObjectTimeSeries<T> fastTS) {
    super(converter, fastTS);
  }

  public ListLocalDateObjectTimeSeries() {
    super(new LocalDateEpochDaysConverter(),
        new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS));
  }

  public ListLocalDateObjectTimeSeries(final LocalDate[] dates, final T[] values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDate[] dates, final T[] values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  public ListLocalDateObjectTimeSeries(final List<LocalDate> dates, final List<T> values) {
    super(s_converter, new FastListIntObjectTimeSeries<T>(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
        s_converter.convertToInt(dates), values));
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final List<LocalDate> dates, final List<T> values) {
    super(new LocalDateEpochDaysConverter(timeZone), new FastListIntObjectTimeSeries<T>(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new LocalDateEpochDaysConverter(timeZone).convertToInt(dates),
        values));
  }

  @SuppressWarnings("unchecked")
  public ListLocalDateObjectTimeSeries(final ObjectTimeSeries<LocalDate, T> dts) {
    super(s_converter, (FastMutableIntObjectTimeSeries<T>) s_converter.convertToInt(
        (FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  @SuppressWarnings("unchecked")
  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final LocalDateObjectTimeSeries<T> dts) {
    super(new LocalDateEpochDaysConverter(timeZone),
        (FastMutableIntObjectTimeSeries<T>) new LocalDateEpochDaysConverter(timeZone).convertToInt(
            (FastIntObjectTimeSeries<T>) TIMESERIES_TEMPLATE, dts));
  }

  public ListLocalDateObjectTimeSeries(final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(s_converter, pmidts);
  }

  public ListLocalDateObjectTimeSeries(final ZoneId timeZone, final FastMutableIntObjectTimeSeries<T> pmidts) {
    super(new LocalDateEpochDaysConverter(timeZone), pmidts);
  }

  @Override
  public LocalDateObjectTimeSeries<T> newInstanceFast(final LocalDate[] dateTimes, final T[] values) {
    return new ListLocalDateObjectTimeSeries<T>(dateTimes, values);
  }

}
