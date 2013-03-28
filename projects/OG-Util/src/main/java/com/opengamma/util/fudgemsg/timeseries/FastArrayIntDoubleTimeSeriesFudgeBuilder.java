/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
@FudgeBuilderFor(FastArrayIntDoubleTimeSeries.class)
public class FastArrayIntDoubleTimeSeriesFudgeBuilder extends FastIntDoubleTimeSeriesFudgeBuilder<FastArrayIntDoubleTimeSeries> implements
    FudgeBuilder<FastArrayIntDoubleTimeSeries> {
  
  public FastArrayIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastArrayIntDoubleTimeSeries(encoding, times, values);
  }

}
