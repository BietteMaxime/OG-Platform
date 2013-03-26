/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint.object;

/**
 * 
 * @param <T> The type of the data
 */
public interface FastMutableLongObjectTimeSeries<T> extends FastLongObjectTimeSeries<T> {
  void primitivePutDataPoint(long time, T value);

  void primitiveRemoveDataPoint(long time);

  void clear();
}
