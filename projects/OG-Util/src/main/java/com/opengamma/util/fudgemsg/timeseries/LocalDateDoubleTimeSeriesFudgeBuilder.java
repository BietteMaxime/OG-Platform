/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for LocalDateDoubleTimeSeries.
 */
public class LocalDateDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<LocalDateDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LocalDateDoubleTimeSeries object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LocalDateDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    // TODO Auto-generated method stub
    return null;
  }

}
