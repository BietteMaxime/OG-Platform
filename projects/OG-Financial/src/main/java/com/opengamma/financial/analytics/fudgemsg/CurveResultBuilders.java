/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
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
    /** Field for the sensitivity to a particular forward curve at a given time */
    private static final String SENSITIVITY_TO_FORWARD_VALUE = "sensitivityForForward";

    @Override
    public MulticurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<String, List<DoublesPair>> yieldCurveSensitivities = new HashMap<>();
      final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities = new HashMap<>();
      final List<FudgeField> yieldCurveFields = message.getAllByName(SENSITIVITY_TO_YIELD_DATA);
      for (final FudgeField yieldCurveField : yieldCurveFields) {
        final FudgeMsg perCurveMessage = (FudgeMsg) yieldCurveField.getValue();
        final String yieldCurveName = perCurveMessage.getString(YIELD_CURVE_NAME);
        final List<FudgeField> timeFields = perCurveMessage.getAllByName(SENSITIVITY_TO_YIELD_TIME);
        final List<FudgeField> valueFields = perCurveMessage.getAllByName(SENSITIVITY_TO_YIELD_VALUE);
        if (timeFields.size() != valueFields.size()) {
          throw new OpenGammaRuntimeException("number of times and values not equal");
        }
        final List<DoublesPair> sensitivities = new ArrayList<>();
        for (int i = 0; i < timeFields.size(); i++) {
          final Double time = deserializer.fieldValueToObject(Double.class, timeFields.get(i));
          final Double sensitivity = deserializer.fieldValueToObject(Double.class, valueFields.get(i));
          sensitivities.add(new DoublesPair(time, sensitivity));
        }
        yieldCurveSensitivities.put(yieldCurveName, sensitivities);
      }
      final List<FudgeField> forwardCurveFields = message.getAllByName(SENSITIVITY_TO_FORWARD_DATA);
      for (final FudgeField forwardCurveField : forwardCurveFields) {
        final FudgeMsg perCurveMessage = (FudgeMsg) forwardCurveField.getValue();
        final String forwardCurveName = perCurveMessage.getString(FORWARD_CURVE_NAME);
        final List<FudgeField> valueFields = perCurveMessage.getAllByName(SENSITIVITY_TO_FORWARD_VALUE);
        final List<ForwardSensitivity> sensitivities = new ArrayList<>();
        for (int i = 0; i < valueFields.size(); i++) {
          final ForwardSensitivity sensitivity = deserializer.fieldValueToObject(ForwardSensitivity.class, valueFields.get(i));
          sensitivities.add(sensitivity);
        }
        forwardCurveSensitivities.put(forwardCurveName, sensitivities);
      }
      return MulticurveSensitivity.of(yieldCurveSensitivities, forwardCurveSensitivities);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveSensitivity object) {
      final Map<String, List<DoublesPair>> yieldCurveSensitivities = object.getYieldDiscountingSensitivities();
      final Map<String, List<ForwardSensitivity>> forwardSensitivities = object.getForwardSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : yieldCurveSensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        perCurveMessage.add(YIELD_CURVE_NAME, entry.getKey());
        for (final DoublesPair pair : entry.getValue()) {
          perCurveMessage.add(SENSITIVITY_TO_YIELD_TIME, pair.first);
          perCurveMessage.add(SENSITIVITY_TO_YIELD_VALUE, pair.second);
        }
        message.add(SENSITIVITY_TO_YIELD_DATA, perCurveMessage);
      }
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : forwardSensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        perCurveMessage.add(FORWARD_CURVE_NAME, entry.getKey());
        for (final ForwardSensitivity sensitivity : entry.getValue()) {
          serializer.addToMessageWithClassHeaders(perCurveMessage, SENSITIVITY_TO_FORWARD_VALUE, null, sensitivity);
        }
        message.add(SENSITIVITY_TO_FORWARD_DATA, perCurveMessage);
      }
    }

  }

  /**
   * Fudge builder for {@link MultipleCurrencyMulticurveSensitivity}
   */
  @FudgeBuilderFor(MultipleCurrencyMulticurveSensitivity.class)
  public static final class MultipleCurrencyMulticurveSensitivityBuilder extends AbstractFudgeBuilder<MultipleCurrencyMulticurveSensitivity> {
    /** The currencies field */
    private static final String CURRENCY = "currency";
    /** The sensitivities field */
    private static final String SENSITIVITIES = "sensitivities";

    @Override
    public MultipleCurrencyMulticurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencies = message.getAllByName(CURRENCY);
      final List<FudgeField> sensitivities = message.getAllByName(SENSITIVITIES);
      if (currencies.size() != sensitivities.size()) {
        throw new OpenGammaRuntimeException("Should have same number of sensitivities as currencies");
      }
      MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
      for (int i = 0; i < currencies.size(); i++) {
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final MulticurveSensitivity sensitivity = deserializer.fieldValueToObject(MulticurveSensitivity.class, sensitivities.get(i));
        result = result.plus(currency, sensitivity);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MultipleCurrencyMulticurveSensitivity object) {
      final Map<Currency, MulticurveSensitivity> sensitivities = object.getSensitivities();
      for (final Map.Entry<Currency, MulticurveSensitivity> entry : sensitivities.entrySet()) {
        message.add(CURRENCY, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, SENSITIVITIES, null, entry.getValue());
      }
    }

  }

  @FudgeBuilderFor(SimpleParameterSensitivity.class)
  public static final class SimpleParameterSensitivityBuilder extends AbstractFudgeBuilder<SimpleParameterSensitivity> {
    /** The curve name field */
    private static final String CURVE_NAME = "curveName";
    /** The sensitivity field */
    private static final String SENSITIVITY = "sensitivity";
    /** The sensitivity vector per curve field */
    private static final String SENSITIVITIES_FOR_CURVE = "sensitivitiesForCurve";

    @Override
    public SimpleParameterSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LinkedHashMap<String, DoubleMatrix1D> sensitivities = new LinkedHashMap<>();
      final List<FudgeField> curves = message.getAllByName(CURVE_NAME);
      final List<FudgeField> sensitivitiesPerCurve = message.getAllByName(SENSITIVITIES_FOR_CURVE);
      if (curves.size() != sensitivitiesPerCurve.size()) {
        throw new OpenGammaRuntimeException("Should have a vector of sensitivities for each curve name");
      }
      for (int i = 0; i < curves.size(); i++) {
        final String curve = (String) curves.get(i).getValue();
        final FudgeMsg perCurveMessage = (FudgeMsg) sensitivitiesPerCurve.get(i).getValue();
        final List<FudgeField> perCurveFields = perCurveMessage.getAllByName(SENSITIVITY);
        final double[] values = new double[perCurveFields.size()];
        for (int j = 0; j < perCurveFields.size(); j++) {
          values[j] = (Double) perCurveFields.get(j).getValue();
        }
        sensitivities.put(curve, new DoubleMatrix1D(values));
      }
      return new SimpleParameterSensitivity(sensitivities);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SimpleParameterSensitivity object) {
      final Map<String, DoubleMatrix1D> sensitivities = object.getSensitivities();
      for (final Map.Entry<String, DoubleMatrix1D> entry : sensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        message.add(CURVE_NAME, entry.getKey());
        final double[] sensitivity = entry.getValue().getData();
        for (final double d : sensitivity) {
          perCurveMessage.add(SENSITIVITY, d);
        }
        message.add(SENSITIVITIES_FOR_CURVE, perCurveMessage);
      }
    }

  }
}
