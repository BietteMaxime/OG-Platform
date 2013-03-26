/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastListIntDoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
@FudgeBuilderFor(FastListIntDoubleTimeSeries.class)
public class FastListIntDoubleTimeSeriesFudgeBuilder extends FastIntDoubleTimeSeriesFudgeBuilder<FastListIntDoubleTimeSeries> implements
    FudgeBuilder<FastListIntDoubleTimeSeries> {

  @Override
  public FastListIntDoubleTimeSeries makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values) {
    return new FastListIntDoubleTimeSeries(encoding, times, values);
  }


}
