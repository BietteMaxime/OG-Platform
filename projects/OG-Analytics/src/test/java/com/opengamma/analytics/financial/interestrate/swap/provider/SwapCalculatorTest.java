/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

public class SwapCalculatorTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  private static final IborIndex USDLIBOR6M = INDEX_LIST[3];
  private static final Calendar NYC = USDLIBOR3M.getCalendar();

  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  // Swap Fixed-Ibor
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final Period SWAP_TENOR = DateUtils.periodOfYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 5, 17);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE_FIXED = 0.025;
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED, true);

  // Swap Ibor-ibor
  private static final double SPREAD3 = 0.0020;
  private static final double SPREAD6 = 0.0005;
  private static final SwapIborIborDefinition SWAP_IBORSPREAD_IBORSPREAD_DEFINITION = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL,
      USDLIBOR3M, SPREAD3, true), AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
  private static final SwapDefinition SWAP_IBOR_IBORSPREAD_DEFINITION = new SwapDefinition(AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, true),
      AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));

  public static final String NOT_USED = "Not used";
  public static final String[] NOT_USED_A = {NOT_USED, NOT_USED, NOT_USED, NOT_USED };

  // Calculators
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_3 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 11, 15) }, new double[] {
      0.0080, 0.0090, 0.0100, 0.0110, 0.0140, 0.0160 });
  private static final ArrayZonedDateTimeDoubleTimeSeries FIXING_TS_6 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 10),
      DateUtils.getUTCDate(2012, 5, 15), DateUtils.getUTCDate(2012, 5, 16) }, new double[] {0.0095, 0.0120, 0.0130 });
  private static final ArrayZonedDateTimeDoubleTimeSeries[] FIXING_TS_3_6 = new ArrayZonedDateTimeDoubleTimeSeries[] {FIXING_TS_3, FIXING_TS_6 };
  //  private static final ConstantSpreadHorizonThetaCalculator THETAC = ConstantSpreadHorizonThetaCalculator.getInstance();

  private static final PV01CurveParametersCalculator PV01CPC = PV01CurveParametersCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSPVC = new ParameterSensitivityParameterCalculator<MulticurveProviderInterface>(
      PresentValueCurveSensitivityDiscountingCalculator.getInstance());

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m

  //  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-6;

  private static final double BP1 = 1.0E-4; // The size of the scaling: 1 basis point. 

  @Test
  public void parSpreadFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, NOT_USED_A);
    final MultipleCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadFixedIborAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapFixedIborDefinition swap0Definition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, USD6MLIBOR3M, NOTIONAL, RATE_FIXED + parSpread, true);
    final SwapFixedCoupon<Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final MultipleCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, NOT_USED_A);
    final MultipleCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadIborSpreadIborSpreadAfterFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 16);
    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, SPREAD3 + parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final MultipleCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()), 0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test for a swap with first leg without spread and par spread computed on that leg.
   */
  public void parSpreadIborIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    @SuppressWarnings("unchecked")
    final Swap<? extends Payment, ? extends Payment> swap = new Swap<Payment, Payment>((Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getFirstLeg().toDerivative(referenceDate, NOT_USED_A),
        (Annuity<Payment>) SWAP_IBOR_IBORSPREAD_DEFINITION.getSecondLeg().toDerivative(referenceDate, new String[] {NOT_USED_A[0], NOT_USED_A[2] }));
    final double parSpread = swap.accept(PSMQDC, MULTICURVES);
    final SwapIborIborDefinition swap0Definition = new SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR3M, parSpread, true),
        AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, SWAP_TENOR, NOTIONAL, USDLIBOR6M, SPREAD6, false));
    final Swap<Coupon, Coupon> swap0 = swap0Definition.toDerivative(referenceDate, NOT_USED_A);
    final MultipleCurrencyAmount pv = swap0.accept(PVDC, MULTICURVES);
    assertEquals("ParSpreadCalculator: fixed-coupon swap", pv.getAmount(swap.getFirstLeg().getCurrency()), 0, TOLERANCE_PV);
  }

  //  @Test
  //  public void parSpreadCurveSensitivityFixedIborBeforeFirstFixing() {
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
  //    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
  //    final String fwdCurveName = ((CouponIbor) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
  //    InterestRateCurveSensitivity pscsComputed = PSCSC.visit(swap, PROVIDER);
  //    pscsComputed = pscsComputed.cleaned();
  //    final double[] timesDsc = new double[swap.getSecondLeg().getNumberOfPayments()];
  //    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
  //      timesDsc[loopcpn] = swap.getSecondLeg().getNthPayment(loopcpn).getPaymentTime();
  //    }
  //    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSMQDC, PROVIDER, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
  //    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
  //    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));
  //    final Set<Double> timesFwdSet = new TreeSet<Double>();
  //    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
  //      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
  //      timesFwdSet.add(((CouponIbor) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
  //    }
  //    final Double[] timesFwd = timesFwdSet.toArray(new Double[0]);
  //    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSMQDC, PROVIDER, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
  //    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
  //    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  //  }
  //
  //  @Test
  //  public void parSpreadCurveSensitivityIborSpreadIborSpreadBeforeFirstFixing() {
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
  //    final Swap<Coupon, Coupon> swap = SWAP_IBORSPREAD_IBORSPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
  //    final String fwdCurveName = ((CouponIborSpread) swap.getSecondLeg().getNthPayment(0)).getForwardCurveName();
  //    InterestRateCurveSensitivity pscsComputed = PSCSC.visit(swap, PROVIDER);
  //    pscsComputed = pscsComputed.cleaned();
  //    final double[] timesDsc = new double[swap.getFirstLeg().getNumberOfPayments()];
  //    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
  //      timesDsc[loopcpn] = swap.getFirstLeg().getNthPayment(loopcpn).getPaymentTime();
  //    }
  //    final List<DoublesPair> sensiDscFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSMQDC, PROVIDER, swap.getFirstLeg().getDiscountCurve(), timesDsc, 1.0E-10);
  //    final List<DoublesPair> sensiDscComputed = pscsComputed.getSensitivities().get(swap.getFirstLeg().getDiscountCurve());
  //    assertTrue("parSpread: curve sensitivity - dsc", InterestRateCurveSensitivityUtils.compare(sensiDscFD, sensiDscComputed, TOLERANCE_SPREAD_DELTA));
  //
  //    final Set<Double> timesFwdSet = new TreeSet<Double>();
  //    for (int loopcpn = 0; loopcpn < swap.getFirstLeg().getNumberOfPayments(); loopcpn++) {
  //      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
  //      timesFwdSet.add(((CouponIborSpread) swap.getFirstLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
  //    }
  //    for (int loopcpn = 0; loopcpn < swap.getSecondLeg().getNumberOfPayments(); loopcpn++) {
  //      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodStartTime());
  //      timesFwdSet.add(((CouponIborSpread) swap.getSecondLeg().getNthPayment(loopcpn)).getFixingPeriodEndTime());
  //    }
  //    final Double[] timesFwd = timesFwdSet.toArray(new Double[0]);
  //    final List<DoublesPair> sensiFwdFD = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(swap, PSMQDC, PROVIDER, fwdCurveName, ArrayUtils.toPrimitive(timesFwd), 1.0E-10);
  //    final List<DoublesPair> sensiFwdComputed = pscsComputed.getSensitivities().get(fwdCurveName);
  //    assertTrue("parSpread: curve sensitivity - fwd", InterestRateCurveSensitivityUtils.compare(sensiFwdFD, sensiFwdComputed, TOLERANCE_SPREAD_DELTA));
  //  }

  @Test
  public void pv01CurveParametersBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    ReferenceAmount<Pair<String, Currency>> pv01Computed = swap.accept(PV01CPC, MULTICURVES);
    ReferenceAmount<Pair<String, Currency>> pv01Expected = new ReferenceAmount<>();
    MultipleCurrencyParameterSensitivity pvps = PSPVC.calculateSensitivity(swap, MULTICURVES, MULTICURVES.getAllNames());
    for (Pair<String, Currency> nameCcy : pvps.getAllNamesCurrency()) {
      double total = 0.0;
      double[] array = pvps.getSensitivity(nameCcy).getData();
      for (int loopa = 0; loopa < array.length; loopa++) {
        total += array[loopa];
      }
      total *= BP1;
      pv01Expected.add(nameCcy, total);
    }
    assertEquals("PV01CurveParametersCalculator: fixed-coupon swap", pv01Expected, pv01Computed);
  }

  @Test
  public void todayPaymentFixedIborBeforeFirstFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstIborPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0100 * NOTIONAL * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(0).getPaymentYearFraction(),
        cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborOnFirstFixedPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 19);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", SWAP_FIXED_IBOR_DEFINITION.getFixedLeg().getNthPayment(0).getAmount() + 0.0140 * NOTIONAL
        * SWAP_FIXED_IBOR_DEFINITION.getIborLeg().getNthPayment(1).getPaymentYearFraction(), cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  public void todayPaymentFixedIborBetweenPayments() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 14);
    final SwapFixedCoupon<Coupon> swap = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, NOT_USED_A);
    final MultipleCurrencyAmount cash = swap.accept(TPC);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 0.0, cash.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: fixed-coupon swap", 1, cash.getCurrencyAmounts().length);
  }

  //  @Test
  //  public void thetaFixedIborBeforeFirstFixing() {
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 11);
  //    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1);
  //    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
  //    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
  //    final double pvToday = PVC.visit(swapToday, CURVES);
  //    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
  //    final double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
  //    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
  //    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  //  }
  //
  //  @Test
  //  public void thetaFixedIborOneDayBeforeFirstFixing() {
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 5, 14);
  //    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1);
  //    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
  //    final ArrayZonedDateTimeDoubleTimeSeries fixing3extended = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 5, 14), DateUtils.getUTCDate(2012, 5, 15) },
  //        new double[] {0.0090, 0.0090 });
  //    final ArrayZonedDateTimeDoubleTimeSeries[] fixing36 = new ArrayZonedDateTimeDoubleTimeSeries[] {fixing3extended, FIXING_TS_6 };
  //    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), fixing36, CURVE_NAMES);
  //    final double pvToday = PVC.visit(swapToday, CURVES);
  //    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
  //    final double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
  //    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - pvToday, theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
  //    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  //  }
  //
  //  @Test
  //  public void thetaFixedIborOverFirstPayment() {
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 8, 17);
  //    final MultipleCurrencyAmount theta = THETAC.getTheta(SWAP_FIXED_IBOR_DEFINITION, referenceDate, CURVE_NAMES, CURVES, FIXING_TS_3_6, 1);
  //    final SwapFixedCoupon<Coupon> swapToday = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate, FIXING_TS_3_6, CURVE_NAMES);
  //    final SwapFixedCoupon<Coupon> swapTomorrow = SWAP_FIXED_IBOR_DEFINITION.toDerivative(referenceDate.plusDays(1), FIXING_TS_3_6, CURVE_NAMES);
  //    final double pvToday = PVC.visit(swapToday, CURVES);
  //    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(CURVES, TimeCalculator.getTimeBetween(referenceDate, referenceDate.plusDays(1)));
  //    final double pvTomorrow = PVC.visit(swapTomorrow, tomorrowData);
  //    final double todayCash = ((CouponFixed) swapToday.getSecondLeg().getNthPayment(0)).getAmount();
  //    assertEquals("ThetaCalculator: fixed-coupon swap", pvTomorrow - (pvToday - todayCash), theta.getAmount(USDLIBOR3M.getCurrency()), TOLERANCE_PV);
  //    assertEquals("ThetaCalculator: fixed-coupon swap", 1, theta.getCurrencyAmounts().length);
  //  }
}
