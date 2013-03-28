/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 */
@FudgeBuilderFor(FastArrayLongDoubleTimeSeries.class)
public class FastArrayLongDoubleTimeSeriesFudgeBuilder extends FastLongDoubleTimeSeriesFudgeBuilder<FastArrayLongDoubleTimeSeries> implements
    FudgeBuilder<FastArrayLongDoubleTimeSeries> {
  
  public FastArrayLongDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values) {
    return new FastArrayLongDoubleTimeSeries(encoding, times, values);
  }

}
