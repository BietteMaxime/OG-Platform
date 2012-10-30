/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.ListDateDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListDateDoubleTimeSeries
 */
@FudgeBuilderFor(ListDateDoubleTimeSeries.class)
public class ListDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ListDateDoubleTimeSeries> {
  @Override
  public ListDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ListDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
