/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 */
public final class ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSMC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSMC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator PVMQSCSMC = PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final ForexSwapDiscountingProviderMethod METHOD_FOREX_SWAP = ForexSwapDiscountingProviderMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MulticurveSensitivity visitCash(final Cash deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, multicurve);
  }

  @Override
  public MulticurveSensitivity visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.parSpreadCurveSensitivity(deposit, multicurve);
  }

  // -----     Payment/Coupon     ------

  @Override
  public MulticurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    return METHOD_FRA.parSpreadCurveSensitivity(fra, multicurve);
  }

  //     -----     Swaps     -----

  @Override
  public MulticurveSensitivity visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(swap, "Swap");
    final Currency ccy1 = swap.getFirstLeg().getCurrency();
    final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSMC, multicurve);
    final MulticurveSensitivity pvcs1 = pvcs.converted(ccy1, multicurve.getFxRates()).getSensitivity(ccy1);
    final MulticurveSensitivity pvmqscs = swap.getFirstLeg().accept(PVMQSCSMC, multicurve);
    final double pvmqs = swap.getFirstLeg().accept(PVMQSMC, multicurve);
    final double pv = multicurve.getFxRates().convert(swap.accept(PVMC, multicurve), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.
    return pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs)));
  }

  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  //     -----     Forex     -----

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The spread.
   */
  @Override
  public MulticurveSensitivity visitForexSwap(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.parSpreadCurveSensitivity(fx, multicurves);
  }

}
