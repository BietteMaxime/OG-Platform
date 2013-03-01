/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.DeliverableSwapFuturesSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteCurveSensitivityHullWhiteCalculator INSTANCE = new MarketQuoteCurveSensitivityHullWhiteCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IR_FUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final DeliverableSwapFuturesSecurityHullWhiteMethod METHOD_SWAP_FUT = DeliverableSwapFuturesSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityHullWhiteMethod METHOD_OPT_STIRFUT_MARG = InterestRateFutureOptionMarginSecurityHullWhiteMethod.getInstance();

  @Override
  public MulticurveSensitivity visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    return derivative.accept(this, multicurves);
  }

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_IR_FUT.priceCurveSensitivity(futures, hullWhite);
  }

  @Override
  public MulticurveSensitivity visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAP_FUT.priceCurveSensitivity(futures, hullWhite);
  }

  @Override
  public MulticurveSensitivity visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_OPT_STIRFUT_MARG.priceCurveSensitivity(option, hullWhite);
  }

  @Override
  public MulticurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
