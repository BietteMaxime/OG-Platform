/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculator of the gamma (second order derivative with respect to the spot rate) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class SpotBlackGammaForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final SpotBlackGammaForexCalculator INSTANCE = new SpotBlackGammaForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SpotBlackGammaForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  SpotBlackGammaForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    ArgumentChecker.isTrue(data instanceof SmileDeltaTermStructureDataBundle, "Must have data bundle with volatility data");
    return METHOD_FXOPTIONVANILLA.spotGammaTheoretical(derivative, data);
  }

}
