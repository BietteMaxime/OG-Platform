/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayDateTimeDoubleTimeSeries.class)
public class ArrayDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ArrayDateTimeDoubleTimeSeries> {
  @Override
  public ArrayDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArrayDateTimeDoubleTimeSeries(converter, (FastLongDoubleTimeSeries) dts);
  }
}
