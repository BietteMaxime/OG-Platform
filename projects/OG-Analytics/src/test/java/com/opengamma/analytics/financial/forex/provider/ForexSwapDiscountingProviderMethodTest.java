/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the method for Forex Swap transaction by discounting on each payment.
 */
public class ForexSwapDiscountingProviderMethodTest {

  private static final MulticurveProviderDiscount PROVIDER = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 6, 27); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, PROVIDER.getFxRate(CUR_1, CUR_2),
      FORWARD_POINTS);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);

  public static final String NOT_USED = "Not used";
  public static final String[] NOT_USED_2 = {NOT_USED, NOT_USED};
  private static final ForexSwap FX_SWAP = (ForexSwap) FX_SWAP_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_2);

  private static final ForexSwapDiscountingProviderMethod METHOD_FX_SWAP = ForexSwapDiscountingProviderMethod.getInstance();
  private static final ForexDiscountingProviderMethod METHOD_FX = ForexDiscountingProviderMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final CurrencyExposureDiscountingCalculator CEDC = CurrencyExposureDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSPSC = new SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface>(
      PSMQCSDC);
  private static final double SHIFT = 1.0E-7;
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSMQCS_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(
      PSMQDC, SHIFT);

  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-8;

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_FX_SWAP.presentValue(FX_SWAP, PROVIDER);
    final MultipleCurrencyAmount pvNear = METHOD_FX.presentValue((FX_SWAP).getNearLeg(), PROVIDER);
    final MultipleCurrencyAmount pvFar = METHOD_FX.presentValue((FX_SWAP).getFarLeg(), PROVIDER);
    assertEquals(pvNear.getAmount(CUR_1) + pvFar.getAmount(CUR_1), pv.getAmount(CUR_1));
    assertEquals(pvNear.getAmount(CUR_2) + pvFar.getAmount(CUR_2), pv.getAmount(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_FX_SWAP.presentValue(FX_SWAP, PROVIDER);
    final MultipleCurrencyAmount pvCalculator = FX_SWAP.accept(PVDC, PROVIDER);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD_FX_SWAP.currencyExposure(FX_SWAP, PROVIDER);
    final MultipleCurrencyAmount pv = METHOD_FX_SWAP.presentValue(FX_SWAP, PROVIDER);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = FX_SWAP.accept(CEDC, PROVIDER);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyMulticurveSensitivity pvs = METHOD_FX_SWAP.presentValueCurveSensitivity(FX_SWAP, PROVIDER);
    pvs.cleaned();
    MultipleCurrencyMulticurveSensitivity pvsNear = METHOD_FX.presentValueCurveSensitivity((FX_SWAP).getNearLeg(), PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvsFar = METHOD_FX.presentValueCurveSensitivity((FX_SWAP).getFarLeg(), PROVIDER);
    pvsNear = pvsNear.plus(pvsFar);
    pvsNear.cleaned();
    assertTrue("Forex swap present value curve sensitivity", pvs.equals(pvsNear));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_FX_SWAP.presentValueCurveSensitivity(FX_SWAP, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = FX_SWAP.accept(PVCSDC, PROVIDER);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentBeforeNearDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(NEAR_DATE.minusDays(1), NOT_USED_2);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentOnNearDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(NEAR_DATE, NOT_USED_2);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getNearLeg().getPaymentCurrency1().getReferenceAmount(),
        cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getNearLeg().getPaymentCurrency2().getReferenceAmount(),
        cash.getAmount(FX_SWAP_DEFINITION.getNearLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentBeforeFarDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(FAR_DATE.minusDays(1), NOT_USED_2);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  public void forexTodayPaymentOnFarDate() {
    final InstrumentDerivative fx = FX_SWAP_DEFINITION.toDerivative(FAR_DATE, NOT_USED_2);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getFarLeg().getPaymentCurrency1().getReferenceAmount(),
        cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_SWAP_DEFINITION.getFarLeg().getPaymentCurrency2().getReferenceAmount(),
        cash.getAmount(FX_SWAP_DEFINITION.getFarLeg().getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests the parSpread method.
   */
  public void parSpread() {
    final double parSpread = METHOD_FX_SWAP.parSpread(FX_SWAP, PROVIDER);
    final ForexSwapDefinition fxSwap0Definition = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, PROVIDER.getFxRate(CUR_1, CUR_2), FORWARD_POINTS
        + parSpread);
    final ForexSwap fxSwap0 = (ForexSwap) fxSwap0Definition.toDerivative(REFERENCE_DATE, NOT_USED_2);
    final MultipleCurrencyAmount pv0 = METHOD_FX_SWAP.presentValue(fxSwap0, PROVIDER);
    assertEquals("Forex swap: par spread", 0, PROVIDER.getFxRates().convert(pv0, CUR_1).getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the par spread method vs calculator
   */
  public void parSpreadMethodVsCalculator() {
    final double parSpreadMethod = METHOD_FX_SWAP.parSpread(FX_SWAP, PROVIDER);
    final double parSpreadCalculator = FX_SWAP.accept(PSMQDC, PROVIDER);
    assertEquals("Forex swap: par spread", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity versus a finite difference computation.
   */
  public void parSpreadCurveSensitivity() {
    final SimpleParameterSensitivity psComputed = PSPSC.calculateSensitivity(FX_SWAP, PROVIDER, PROVIDER.getAllNames());
    final SimpleParameterSensitivity psFD = PSMQCS_FDC.calculateSensitivity(FX_SWAP, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", psFD, psComputed, TOLERANCE_SPREAD_DELTA);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity through the method and through the calculator.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pvcsMethod = METHOD_FX_SWAP.parSpreadCurveSensitivity(FX_SWAP, PROVIDER);
    final MulticurveSensitivity pvcsCalculator = FX_SWAP.accept(PSMQCSDC, PROVIDER);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

}
