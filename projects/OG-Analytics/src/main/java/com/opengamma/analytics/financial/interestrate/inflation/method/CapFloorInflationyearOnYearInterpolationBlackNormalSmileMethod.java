/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod {
  /**
   * The method unique instance.
   */
  private static final CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod INSTANCE = new CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod();

  /**
   * Private constructor.
   */
  private CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorInflationyearOnYearInterpolationBlackNormalSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  /**
   * Computes the net amount.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount netAmount(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(black, "Black provider");
    final double timeToMaturity = cap.getReferenceEndTime()[1] - cap.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), timeToMaturity, cap.isCap());
    final double priceIndexStart0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[0]);
    final double priceIndexStart1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[1]);
    final double priceIndexStart = cap.getWeightStart() * priceIndexStart0 + (1 - cap.getWeightStart()) * priceIndexStart1;
    final double priceIndexEnd0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[0]);
    final double priceIndexEnd1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[1]);
    final double priceIndexEnd = cap.getWeightEnd() * priceIndexEnd0 + (1 - cap.getWeightEnd()) * priceIndexEnd1;
    final double forward = priceIndexEnd / priceIndexStart - 1;
    final double volatility = black.getBlackParameters().getVolatility(cap.getReferenceEndTime()[1], cap.getStrike());
    final NormalFunctionData dataBlack = new NormalFunctionData(forward, 1.0, volatility);
    final Function1D<NormalFunctionData, Double> func = NORMAL_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

  /**
   * Computes the present value.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    final MultipleCurrencyAmount nonDiscountedPresentValue = netAmount(cap, black);
    final double df = black.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    return nonDiscountedPresentValue.multipliedBy(df);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cap/floor in the Black model.
   * No smile impact is taken into account; equivalent to a sticky strike smile description.
   * @param cap The caplet/floorlet.
   * @param black The Black implied volatility and multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(black, "Black provider");
    InflationProviderInterface inflation = black.getInflationProvider();
    final double timeToMaturity = cap.getReferenceEndTime()[1] - cap.getLastKnownFixingTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), timeToMaturity, cap.isCap());
    final double priceIndexStart0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[0]);
    final double priceIndexStart1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceStartTime()[1]);
    final double priceIndexStart = cap.getWeightStart() * priceIndexStart0 + (1 - cap.getWeightStart()) * priceIndexStart1;
    final double priceIndexEnd0 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[0]);
    final double priceIndexEnd1 = black.getInflationProvider().getPriceIndex(cap.getPriceIndex(), cap.getReferenceEndTime()[1]);
    final double priceIndexEnd = cap.getWeightEnd() * priceIndexEnd0 + (1 - cap.getWeightEnd()) * priceIndexEnd1;
    final double forward = priceIndexEnd / priceIndexStart - 1;
    final double df = black.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listPrice = new ArrayList<DoublesPair>();
    listPrice.add(new DoublesPair(cap.getReferenceEndTime()[0], cap.getWeightEnd() / priceIndexStart));
    listPrice.add(new DoublesPair(cap.getReferenceEndTime()[1], (1 - cap.getWeightEnd()) / priceIndexStart));
    listPrice.add(new DoublesPair(cap.getReferenceStartTime()[0], -cap.getWeightStart() * priceIndexEnd / (priceIndexStart * priceIndexStart)));
    listPrice.add(new DoublesPair(cap.getReferenceStartTime()[1], -(1 - cap.getWeightStart()) * priceIndexEnd / (priceIndexStart * priceIndexStart)));
    resultMapPrice.put(inflation.getName(cap.getPriceIndex()), listPrice);
    final InflationSensitivity forwardDi = InflationSensitivity.ofPriceIndex(resultMapPrice);
    final double dfDr = -cap.getPaymentTime() * df;
    final double volatility = black.getBlackParameters().getVolatility(cap.getReferenceEndTime()[1], cap.getStrike());
    final NormalFunctionData dataBlack = new NormalFunctionData(forward, 1.0, volatility);
    double[] priceDerivatives = new double[3];
    final double bsAdjoint = NORMAL_FUNCTION.getPriceAdjoint(option, dataBlack, priceDerivatives);
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(inflation.getName(cap.getCurrency()), list);
    InflationSensitivity result = InflationSensitivity.ofYieldDiscounting(resultMap);
    result = result.multipliedBy(priceDerivatives[0]);
    result = result.plus(forwardDi.multipliedBy(df * priceDerivatives[1]));
    result = result.multipliedBy(cap.getNotional() * cap.getPaymentYearFraction());
    return MultipleCurrencyInflationSensitivity.of(cap.getCurrency(), result);
  }

}
