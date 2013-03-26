/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilderFor;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayLocalDateDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayLocalDateDoubleTimeSeries.class)
public class ArrayLocalDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<LocalDate, ArrayLocalDateDoubleTimeSeries> {
  @Override
  public ArrayLocalDateDoubleTimeSeries makeSeries(DateTimeConverter<LocalDate> converter, FastTimeSeries<?> dts) {
    return new ArrayLocalDateDoubleTimeSeries(converter, (FastIntDoubleTimeSeries) dts);
  }
}
