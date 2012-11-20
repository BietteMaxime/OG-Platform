/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastMapIntDoubleTimeSeries;

/**
 * 
 */
@FudgeBuilderFor(FastMapIntDoubleTimeSeries.class)
public class FastMapIntDoubleTimeSeriesFudgeBuilder extends FastIntDoubleTimeSeriesFudgeBuilder<FastMapIntDoubleTimeSeries> implements
    FudgeBuilder<FastMapIntDoubleTimeSeries> {

  @Override
  public FastMapIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastMapIntDoubleTimeSeries(encoding, times, values);
  }


}
