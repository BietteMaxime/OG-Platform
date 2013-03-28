/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * 
 * @param <DATE_TYPE> Type of the date
 * @param <T> Type of the data
 */
public interface FastBackedObjectTimeSeries<DATE_TYPE, T> extends ObjectTimeSeries<DATE_TYPE, T> {

  DateTimeConverter<DATE_TYPE> getConverter();

  FastObjectTimeSeries<?, T> getFastSeries();

  FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final FastObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final T other, final BinaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> operate(final UnaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(final FastObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(final FastBackedObjectTimeSeries<?, T> other,
      final BinaryOperator<T> operator);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other);

  FastBackedObjectTimeSeries<DATE_TYPE, T> lag(final int days);

  FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries();

  FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries(DateTimeNumericEncoding encoding);

  FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries();

  FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding);

  FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries();

  FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding encoding);

  FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries();

  FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding encoding);

}
