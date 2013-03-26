/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.yearoffset;

import com.opengamma.timeseries.AbstractIntObjectTimeSeries;
import com.opengamma.timeseries.AbstractLongObjectTimeSeries;
import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * @param <T> The type of the data 
 */
public interface YearOffsetObjectTimeSeries<T> extends ObjectTimeSeries<Double, T>, FastBackedObjectTimeSeries<Double, T> {
  /** */
  public abstract static class Integer<T> extends AbstractIntObjectTimeSeries<Double, T> implements YearOffsetObjectTimeSeries<T> {
    public Integer(final DateTimeConverter<Double> converter, final FastIntObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
  }
  /** */
  public abstract static class Long<T> extends AbstractLongObjectTimeSeries<Double, T> implements YearOffsetObjectTimeSeries<T> {
    public Long(final DateTimeConverter<Double> converter, final FastLongObjectTimeSeries<T> timeSeries) {
      super(converter, timeSeries);
    }

    @Override
    public ObjectTimeSeries<Double, T> newInstance(final Double[] dateTimes, final T[] values) {
      return newInstanceFast(dateTimes, values);
    }

    public abstract YearOffsetObjectTimeSeries<T> newInstanceFast(Double[] dateTimes, T[] values);
  }
}
