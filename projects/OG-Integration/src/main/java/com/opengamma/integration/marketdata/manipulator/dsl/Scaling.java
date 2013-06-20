/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 * Manipulator that scales a single market data value.
 */
public class Scaling implements StructureManipulator<Double> {

  /** Field name for Fudge message. */
  private static final String SCALING_FACTOR = "scalingFactor";

  /** Scaling factor applied to the market data value. */
  private final double _scalingFactor;

  /* package */ Scaling(double scalingFactor) {
    ArgumentChecker.notNull(scalingFactor, "scalingFactor");
    if (Double.isInfinite(scalingFactor) || Double.isNaN(scalingFactor)) {
      throw new IllegalArgumentException("scalingFactor must not be infinite or NaN. value=" + scalingFactor);
    }
    _scalingFactor = scalingFactor;
  }

  @Override
  public Double execute(Double structure) {
    return structure * _scalingFactor;
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SCALING_FACTOR, null, _scalingFactor);
    return msg;
  }

  public static Scaling fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double scalingFactor = deserializer.fieldValueToObject(Double.class, msg.getByName(SCALING_FACTOR));
    return new Scaling(scalingFactor);
  }
}
