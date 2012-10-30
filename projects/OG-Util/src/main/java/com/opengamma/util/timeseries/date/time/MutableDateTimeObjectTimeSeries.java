/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

import java.util.Date;

import com.opengamma.util.timeseries.AbstractMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.AbstractMutableLongObjectTimeSeries;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.MutableObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public interface MutableDateTimeObjectTimeSeries<T> extends DateTimeObjectTimeSeries<T>, MutableObjectTimeSeries<Date, T> {
  /** */
  public abstract static class Integer<T> extends AbstractMutableIntObjectTimeSeries<Date, T> implements MutableDateTimeObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Date> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateTimeObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }

  /** */
  public abstract static class Long<T> extends AbstractMutableLongObjectTimeSeries<Date, T> implements MutableDateTimeObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Date> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Date, T> newInstance(final Date[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract DateTimeObjectTimeSeries<T> newInstanceFast(Date[] dateTimes, T[] values);
  }
}
