/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Contains results of calculations associated with curves
 */
/* package */ final class CurveResultBuilders {

  private CurveResultBuilders() {
  }

  /**
   * Fudge builder for {@link ForwardSensitivity}
   */
  @FudgeBuilderFor(ForwardSensitivity.class)
  public static final class ForwardSensitivityBuilder extends AbstractFudgeBuilder<ForwardSensitivity> {
    /** The start time field */
    private static final String START_TIME = "startTime";
    /** The end time field */
    private static final String END_TIME = "endTime";
    /** The accrual factor */
    private static final String ACCRUAL_FACTOR = "accrualFactor";
    /** The value */
    private static final String VALUE = "value";

    @Override
    public ForwardSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double startTime = message.getDouble(START_TIME);
      final double endTime = message.getDouble(END_TIME);
      final double accrualFactor = message.getDouble(ACCRUAL_FACTOR);
      final double value = message.getDouble(VALUE);
      return new ForwardSensitivity(startTime, endTime, accrualFactor, value);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForwardSensitivity object) {
      message.add(START_TIME, object.getStartTime());
      message.add(END_TIME, object.getEndTime());
      message.add(ACCRUAL_FACTOR, object.getAccrualFactor());
      message.add(VALUE, object.getValue());
    }

  }

  /**
   * Fudge builder for {@link MulticurveSensitivity}
   */
  @FudgeBuilderFor(MulticurveSensitivity.class)
  public static final class MulticurveSensitivityBuilder extends AbstractFudgeBuilder<MulticurveSensitivity> {
    /** The yield curve name field */
    private static final String YIELD_CURVE_NAME = "yieldCurveName";
    /** Field for the map containing the sensitivities to yield curves */
    private static final String SENSITIVITY_TO_YIELD_DATA = "allSensitivityToYieldData";
    /** Field for the cash flow times of the sensitivities to a particular yield curve */
    private static final String SENSITIVITY_TO_YIELD_TIME = "cashFlowTimeForYield";
    /** Field for the sensitivity to a particular yield curve at a given time */
    private static final String SENSITIVITY_TO_YIELD_VALUE = "sensitivityForYield";
    /** The forward curve name field */
    private static final String FORWARD_CURVE_NAME = "forwardCurveName";
    /** Field for the map containing the sensitivities to the forward curve */
    private static final String SENSITIVITY_TO_FORWARD_DATA = "allSensitivityToForwardData";
    /** Field for cash flow times of the sensitivities to a particular forward curve */
    private static final String SENSITIVITY_TO_FORWARD_TIME = "cashFlowTimeForForward";
    /** Field for the sensitivity to a particular forward curve at a given time */
    private static final String SENSITIVITY_TO_FORWARD_VALUE = "sensitivityForForward";

    @Override
    public MulticurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return null;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveSensitivity object) {
      final Map<String, List<DoublesPair>> yieldCurveSensitivities = object.getYieldDiscountingSensitivities();
      final Map<String, List<ForwardSensitivity>> forwardSensitivities = object.getForwardSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : yieldCurveSensitivities.entrySet()) {

      }
    }

  }

}
