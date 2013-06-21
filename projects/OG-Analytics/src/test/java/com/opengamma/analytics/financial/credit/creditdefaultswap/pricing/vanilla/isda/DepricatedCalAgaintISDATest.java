/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwapDebug;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDAModelDatasets.ISDA_Results;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class DepricatedCalAgaintISDATest {

  private static final double NOTIONAL = 1e7;

  private static final GenerateCreditDefaultSwapPremiumLegSchedule PREMIUM_LEG_SCHEDULE_BUILDER = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final CalibrateHazardRateTermStructureISDAMethod HAZARD_CURVE_CALIBRATOR = new CalibrateHazardRateTermStructureISDAMethod();
  private static final PresentValueCreditDefaultSwap DEPRICATED_CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final PresentValueCreditDefaultSwapDebug DEPRICATED_CALCULATOR_DEBUG = new PresentValueCreditDefaultSwapDebug();

  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2013, 4, 21);
  private static final ZonedDateTime BASE_DATE = TODAY;
  private static final ZonedDateTime EFFECTIVE_DATE = TODAY.plusDays(1);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2013, 3, 20);

  private static final LocalTime LOCAL_TIME = TODAY.toLocalTime(); // to convert LocalDate to ZonedDateTime
  private static final ZoneId TIME_ZONE = ZoneId.of("Z");

  // points related to the credit curve
  private static final ZonedDateTime[] HR_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 20), DateUtils.getUTCDate(2013, 9, 20), DateUtils.getUTCDate(2014, 3, 20),
      DateUtils.getUTCDate(2015, 3, 20), DateUtils.getUTCDate(2016, 3, 20), DateUtils.getUTCDate(2018, 3, 20), DateUtils.getUTCDate(2023, 3, 20)};
  private static final double[] PAR_SPREAD = new double[] {50, 70, 100, 150, 200, 400, 1000};// {200,200,200,200,200,200,200};//
  private static final double[] SURVIVAL_PROB = new double[] {0.997849512923258, 0.994019898815046, 0.983172759236572, 0.950256828767595, 0.902290187078861, 0.697053603355638, 0.0434236949665211};
  private static final double[] HR_TIMES;
  private static final double[] HR_RATES;
  private static final HazardRateCurve HAZARD_RATE_CURVE;
  private static final double RECOVERY_RATE = 0.4;

  // points related to the yield curve (note: here we use yield curve points directly rather than fit from IR instruments)
  private static final ZonedDateTime[] YC_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 27), DateUtils.getUTCDate(2013, 8, 27), DateUtils.getUTCDate(2013, 11, 27),
      DateUtils.getUTCDate(2014, 5, 27), DateUtils.getUTCDate(2015, 5, 27), DateUtils.getUTCDate(2016, 5, 27), DateUtils.getUTCDate(2018, 5, 27), DateUtils.getUTCDate(2020, 5, 27),
      DateUtils.getUTCDate(2023, 5, 27), DateUtils.getUTCDate(2028, 5, 27), DateUtils.getUTCDate(2033, 5, 27), DateUtils.getUTCDate(2043, 5, 27)};
  // private static final double[] YC_RATES = new double[] {0.02, 0.025, 0.03, 0.04, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05};
  // private static final double[] DISCOUNT_FACT = new double[] {0.994589313438547, 0.989167291057847, 0.979719763307372, 0.954435665114144, 0.898702093861715, 0.855792353208878, 0.776228891799436,
  // 0.70396837996545, 0.608114354791448, 0.47634614417861, 0.373179781356832, 0.229038773546182};
  private static final double[] YC_RATES;
  private static final double[] DISCOUNT_FACT;
  private static final double[] YC_TIMES;
  private static final ISDADateCurve YIELD_CURVE;

  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  // TODO check the purpose of this offset
  private static final double OFFSET = 0.0;// 1. / 365;
  private static final ISDAYieldCurveAndHazardRateCurveProvider CURVES;

  // The parCDS
  private static final CreditDefaultSwapDefinition[] PAR_CDS;

  // examples
  private static final ISDA_Results[] EXAMPLE1 = ISDAModelDatasets.getExample1();
  private static final ISDA_Results[] EXAMPLE3 = ISDAModelDatasets.getExample3();

  // private static final ZonedDateTime[] FEE_LEG_PAYMENT_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 20), DateUtils.getUTCDate(2013, 9, 20), DateUtils.getUTCDate(2013, 12, 20),
  // DateUtils.getUTCDate(2014, 3, 20), DateUtils.getUTCDate(2014, 6, 20), DateUtils.getUTCDate(2014, 9, 22), DateUtils.getUTCDate(2014, 12, 22), DateUtils.getUTCDate(2015, 3, 20),
  // DateUtils.getUTCDate(2015, 6, 22), DateUtils.getUTCDate(2015, 9, 21), DateUtils.getUTCDate(2015, 12, 21), DateUtils.getUTCDate(2016, 3, 21), DateUtils.getUTCDate(2016, 6, 20),
  // DateUtils.getUTCDate(2016, 9, 20), DateUtils.getUTCDate(2016, 12, 20), DateUtils.getUTCDate(2017, 3, 20), DateUtils.getUTCDate(2017, 6, 20), DateUtils.getUTCDate(2017, 9, 20),
  // DateUtils.getUTCDate(2017, 12, 20), DateUtils.getUTCDate(2018, 3, 20), DateUtils.getUTCDate(2018, 6, 20)};
  //
  static {
    final int ccPoints = HR_DATES.length;
    HR_TIMES = new double[ccPoints];
    HR_RATES = new double[ccPoints];
    for (int i = 0; i < ccPoints; i++) {
      HR_TIMES[i] = ACT365.getDayCountFraction(BASE_DATE, HR_DATES[i]);
      // TODO these should be fitted to the par-spreads then compared to the numbers from ISDA - here we take directly from the ISDA fit
      HR_RATES[i] = -Math.log(SURVIVAL_PROB[i]) / HR_TIMES[i];
    }
    HAZARD_RATE_CURVE = new HazardRateCurve(HR_DATES, HR_TIMES, HR_RATES, OFFSET);

    final int ycPoints = YC_DATES.length;

    YC_RATES = new double[ycPoints];
    DISCOUNT_FACT = new double[ycPoints];
    Arrays.fill(DISCOUNT_FACT, 1.0);
    YC_TIMES = new double[ycPoints];
    for (int i = 0; i < ycPoints; i++) {
      YC_TIMES[i] = ACT365.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, YC_RATES, OFFSET); // Remake: this constructor assumes ACT/365
    CURVES = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);

    PAR_CDS = new CreditDefaultSwapDefinition[ccPoints];
    for (int i = 0; i < ccPoints; i++) {
      PAR_CDS[i] = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withEffectiveDate(EFFECTIVE_DATE).withStartDate(START_DATE).withMaturityDate(HR_DATES[i])
          .withRecoveryRate(RECOVERY_RATE).withSpread(PAR_SPREAD[i]);
    }

  }

  @SuppressWarnings("deprecation")
  private void testISDA_Results(final ISDA_Results[] data, final boolean debug) {
    if (debug) {
      System.out.println("ISDACompliantPremiumLegCalculatorAgaintISDATest2.testISDA_Results DO NOT PUSH WITH DEBUG ON\n");
    }
    int failCount = 0;
    int constructionFailCount = 0;

    final int nEx = data.length;
    for (int count = 0; count < nEx; count++) {

      ISDA_Results res = data[count];

      // want to move off ZoneDateTime, but for now need to convert LocalDate back
      ZonedDateTime today = ZonedDateTime.of(res.today, LOCAL_TIME, TIME_ZONE);
      ZonedDateTime start = ZonedDateTime.of(res.startDate, LOCAL_TIME, TIME_ZONE);
      ZonedDateTime end = ZonedDateTime.of(res.endDate, LOCAL_TIME, TIME_ZONE);
      ZonedDateTime effectiveDate = today.plusDays(1); // aka stepin date - hard coded to today+1

      // make a CDS - not the notional is 10MM and cannot be changed (what ever is in res is ignored) TODO must change this 
      final CreditDefaultSwapDefinition cds;
      final CreditDefaultSwapDefinition cds_noAcc;
      try {
        cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withEffectiveDate(effectiveDate).withStartDate(start).withMaturityDate(end).withRecoveryRate(res.recoveryRate)
            .withSpread(res.fracSpread);

        cds_noAcc = ((LegacyVanillaCreditDefaultSwapDefinition) cds).withIncludeAccruedPremium(false);
      } catch (IllegalArgumentException e) {
        // skip the CDSs we can't build until we've fully matched to inputs to ISDA
        constructionFailCount++;
        continue;
      }

      // price it
      double rpv01_clean_ISDA = DEPRICATED_CALCULATOR.calculatePremiumLeg(today, cds, YIELD_CURVE, res.creditCurve, PriceType.CLEAN);
      double rpv01_clean_ISDA_noAccOnDefault = DEPRICATED_CALCULATOR.calculatePremiumLeg(today, cds_noAcc, YIELD_CURVE, res.creditCurve, PriceType.CLEAN);
      double rpv01_dirty_ISDA = DEPRICATED_CALCULATOR.calculatePremiumLeg(today, cds, YIELD_CURVE, res.creditCurve, PriceType.DIRTY);
      double contLeg_ISDA = DEPRICATED_CALCULATOR_DEBUG.calculateContingentLeg(today, cds, YIELD_CURVE, res.creditCurve);

      double premLeg_clean_ISDA = res.fracSpread * rpv01_clean_ISDA;
      double defaultAcc = res.fracSpread * (rpv01_clean_ISDA - rpv01_clean_ISDA_noAccOnDefault);
      double rpv01_accrued = rpv01_dirty_ISDA - rpv01_clean_ISDA;
      double accruedPrem = rpv01_accrued * res.fracSpread;

      // back out the accrued-days by inverting the accrued premium formula (which is ACT/360) - this matched the formula on the ISDA spread sheet
      int accruedDays = (int) Math.round(360 * rpv01_accrued / NOTIONAL);

      if (debug) {
        System.out.println(res.premiumLeg + "\t" + premLeg_clean_ISDA + "\t\t" + res.protectionLeg + "\t" + contLeg_ISDA + "\t\t" + res.defaultAcc + "\t" + defaultAcc + "\t\t" + res.accruedPremium
            + "\t" + accruedPrem + "\t\t" + res.accruedDays + "\t" + accruedDays);
      }
      try {
        assertEquals("Premium Leg:", res.premiumLeg, premLeg_clean_ISDA, 1e-8 * NOTIONAL); // This should be 1e-15*NOTIONAL
        assertEquals("Protection Leg:", res.protectionLeg, contLeg_ISDA, 1e-8 * NOTIONAL); // ditto
        assertEquals("Default Acc:", res.defaultAcc, defaultAcc, 1e-8 * NOTIONAL);
        assertEquals("Accrued Premium: ", res.accruedPremium, accruedPrem, 1e-15 * NOTIONAL); // the accrued is trivial, so should be highly accurate
        assertEquals("Accrued Days: ", res.accruedDays, accruedDays);
      } catch (AssertionError e) {
        failCount++;
      }
    }
    if (debug) {
      System.out.println("\nFailed to construct: " + constructionFailCount + " Failed: " + failCount + "\n");
    }
  }

  @Test(enabled = false)
  public void example1Test() {
    testISDA_Results(EXAMPLE1, true);
  }

  @Test
   (enabled = false)
  public void example3Test() {
    testISDA_Results(EXAMPLE3, true);
  }

  @Test(enabled = false)
  public void exampleSheetTest() {
    testISDA_Results(ISDAModelDatasetsSheetReader.loadSheet("example1.csv", RECOVERY_RATE), true);
  }


  @Test
  public void yieldCurveTest() {
    final int n = YC_TIMES.length;
    for (int i = 0; i < n; i++) {
      double t = YC_TIMES[i];
      double df = YIELD_CURVE.getDiscountFactor(t);
      assertEquals(DISCOUNT_FACT[i], df, 1e-10);
    }
  }

  // This test only passes with OFFSET = 0.0;
  @Test
  public void hazardCurveTest() {
    final int n = HR_TIMES.length;
    for (int i = 0; i < n; i++) {
      double t = HR_TIMES[i];
      double q = HAZARD_RATE_CURVE.getSurvivalProbability(t);
      assertEquals(SURVIVAL_PROB[i], q, 1e-10);
    }
  }

  @SuppressWarnings("deprecation")
  @Test(enabled = false)
  public void presentValueTest() {
    System.out.println("ISDACompliantPremiumLegCalculatorAgaintISDATest2.presentValueTest: THIS TEST SHOULD NOT BE ANABLED FOR PUSH");
    final int n = PAR_CDS.length;
    final HazardRateCurve hazardCurve_CM = HAZARD_CURVE_CALIBRATOR.isdaCalibrateHazardRateCurve(TODAY, (LegacyVanillaCreditDefaultSwapDefinition) PAR_CDS[0], HR_DATES, PAR_SPREAD, YIELD_CURVE);
    // now use our own fit
    HazardRateCurve hazardCurve_RW = fitHazardRateCurve(TODAY, PAR_CDS, PAR_SPREAD, YIELD_CURVE, hazardCurve_CM);

    System.out.println("rpv01_ISDA\t rpv01_CM\t rpv01_ISDA_RW\t rpv01_RW\t  pv_ISDA\t pv_CM\t pv_ISDA_RW\t pv_CM_RW\t pv_RW");
    for (int i = 0; i < n; i++) {
      final double fracSpread = PAR_SPREAD[i] / 10000;

      // from ISDA hazard curve
      double rpv01_clean_ISDA = DEPRICATED_CALCULATOR.calculatePremiumLeg(TODAY, PAR_CDS[i], YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);
      double rpv01_dirty_ISDA = DEPRICATED_CALCULATOR.calculatePremiumLeg(TODAY, PAR_CDS[i], YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.DIRTY);

      double contLeg_ISDA = DEPRICATED_CALCULATOR.calculateContingentLeg(TODAY, PAR_CDS[i], YIELD_CURVE, HAZARD_RATE_CURVE);
      double pv_clean_ISDA = contLeg_ISDA - fracSpread * rpv01_clean_ISDA;
      double pv_dirty_ISDA = contLeg_ISDA - fracSpread * rpv01_dirty_ISDA;
      System.out.println("accruced:\t" + (pv_clean_ISDA - pv_dirty_ISDA));

      // from CM's calibration
      double rpv01_CM = DEPRICATED_CALCULATOR.calculatePremiumLeg(TODAY, PAR_CDS[i], YIELD_CURVE, hazardCurve_CM, PriceType.CLEAN);
      double contLeg_CM = DEPRICATED_CALCULATOR.calculateContingentLeg(TODAY, PAR_CDS[i], YIELD_CURVE, hazardCurve_CM);
      double pv_CM = contLeg_CM - fracSpread * rpv01_CM;

      DummyCDSPricer pricer = new DummyCDSPricer(TODAY, PAR_CDS[i], YIELD_CURVE);

      // RW's pricing with ISDA calibration
      double rpv01_ISDA_RW = 1e7 * pricer.rpv01(HAZARD_RATE_CURVE); // notional of 10MM
      double pv_ISDA_RW = 1e7 * pricer.pv(HAZARD_RATE_CURVE, PAR_SPREAD[i]);

      // RW's pricing with CM calibration
      double pv_CM_RW = 1e7 * pricer.pv(hazardCurve_CM, PAR_SPREAD[i]);

      // from RW's "dummy" calibration
      double rpv01_RW = 1e7 * pricer.rpv01(hazardCurve_RW); // notional of 10MM
      double contLeg_RW = 1e7 * pricer.protectionLeg(hazardCurve_RW);
      double pv_RW = contLeg_RW - fracSpread * rpv01_RW;

      System.out.println(rpv01_clean_ISDA + "\t" + rpv01_CM + "\t" + rpv01_ISDA_RW + "\t" + rpv01_RW + "\t" + pv_clean_ISDA + "\t" + pv_CM + "\t" + pv_ISDA_RW + "\t" + pv_CM_RW + "\t" + pv_RW);

    }
    System.out.println();

    // final int nOutput = 201;
    // for (int jj = 0; jj < nOutput; jj++) {
    // double t = jj * 10.0 / (nOutput - 1);
    // double q1 = HAZARD_RATE_CURVE.getSurvivalProbability(t);
    // double q2 = hazardCurve_CM.getSurvivalProbability(t);
    // double q3 = hazardCurve_RW.getSurvivalProbability(t);
    // System.out.println(t + "\t" + q1 + "\t" + q2 + "\t" + q3);
    // }
  }

  @Test(enabled = false)
  public final void dumpHazardCurve() {
    System.out.println("ISDACompliantPremiumLegCalculatorAgaintISDATest2.dumpHazardCurve: THIS TEST SHOULD NOT BE ANABLED FOR PUSH");
    final HazardRateCurve hazardCurve = HAZARD_CURVE_CALIBRATOR.isdaCalibrateHazardRateCurve(TODAY, (LegacyVanillaCreditDefaultSwapDefinition) PAR_CDS[0], HR_DATES, PAR_SPREAD, YIELD_CURVE);
    int n = 101;
    for (int i = 0; i < n; i++) {
      double t = i * 10. / (n - 1);
      System.out.println(t + "\t" + hazardCurve.getHazardRate(t) + "\t" + hazardCurve.getSurvivalProbability(t));
    }

  }

  private class DummyCDSPricer {

    private final double[] _t;
    private final double[] _df;
    // private final double[] _q;
    private final double[] _delta;
    private final int _nPayments;
    private final double _rr;
    private final ISDADateCurve _yieldCurve;

    // private final HazardRateCurve _hazardRateCurve;

    public DummyCDSPricer(ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve) {
      ZonedDateTime[][] temp = PREMIUM_LEG_SCHEDULE_BUILDER.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      final int n = temp.length;
      _rr = cds.getRecoveryRate();

      ZonedDateTime[] dates = new ZonedDateTime[n];
      for (int i = 0; i < n; i++) {
        dates[i] = temp[i][0];
      }

      _nPayments = n - 1;
      _t = new double[n];
      _df = new double[n];
      // _q = new double[n];
      _delta = new double[n - 1];
      _df[0] = 1.0;
      // _q[0] = 1.0;
      for (int i = 1; i < n; i++) {
        _t[i] = ACT365.getDayCountFraction(valuationDate, dates[i]);
        _delta[i - 1] = ACT360.getDayCountFraction(dates[i - 1], dates[i]);
        _df[i] = yieldCurve.getDiscountFactor(_t[i]);
        // _q[i] = hazardRateCurve.getSurvivalProbability(_t[i]);
      }
      _yieldCurve = yieldCurve;
      // _hazardRateCurve = hazardRateCurve;

    }

    /**
     * Value per unit of spread on a notional of 1 of the premium leg when valuation date is on a payment date (so there is no accrued
     *  premium) - from pp 103 of O'Kane - Modelling Single-name and multi-name Credit Derivatives  
     * @return RPV01 This should be multiplied by the notional and the spread (i.e. spread in basis points divided by 10,000) to give
     * PV of the premium leg 
     */
    public double rpv01(final HazardRateCurve hazardRateCurve) {
      final double[] q = new double[_nPayments + 1];
      q[0] = 1.0;
      for (int i = 1; i <= _nPayments; i++) {
        q[i] = hazardRateCurve.getSurvivalProbability(_t[i]);
      }
      double sum = 0;
      for (int i = 0; i < _nPayments; i++) {
        sum += _delta[i] * _df[i + 1] * (q[i] + q[i + 1]);
      }
      return 0.5 * sum;
    }

    /**
     * PV of protection leg for a notional of 1 - adapted from pp 106 of O'Kane - Modelling Single-name and multi-name Credit Derivatives
     * @return This should be multiplied by the notional to give PV of protection leg 
     */
    public double protectionLeg(final HazardRateCurve hazardRateCurve) {
      final int m = 10; // internal steps between payments of the premium leg
      double sum = 0;
      for (int i = 0; i < _nPayments; i++) {

        double[] df = new double[m + 1];
        double[] q = new double[m + 1];
        df[0] = _df[i];
        q[0] = hazardRateCurve.getSurvivalProbability(_t[i]);

        double t0 = _t[i];
        double step = (_t[i + 1] - _t[i]) / m;
        for (int jj = 1; jj < m; jj++) {
          double t = t0 + (jj) * step;
          df[jj] = _yieldCurve.getDiscountFactor(t);
          q[jj] = hazardRateCurve.getSurvivalProbability(t);
        }
        df[m] = _df[i + 1];
        q[m] = hazardRateCurve.getSurvivalProbability(_t[i + 1]);

        for (int jj = 0; jj < m; jj++) {
          sum += (df[jj] + df[jj + 1]) * (q[jj] - q[jj + 1]);
        }
      }

      return 0.5 * (1 - _rr) * sum;
    }

    /**
     * The PV (on a unit notional) for a payer of protection (i.e. short the credit risk) 
     * @param basisPointsSpread Spread <b>in basis points</b>
     * @return
     */
    public double pv(final HazardRateCurve hazardRateCurve, final double basisPointsSpread) {
      return protectionLeg(hazardRateCurve) - basisPointsSpread / 10000 * rpv01(hazardRateCurve);
    }

  }

  private HazardRateCurve fitHazardRateCurve(ZonedDateTime valuationDate, final CreditDefaultSwapDefinition[] cds, final double[] parSpreads, final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder();
    final HazardCurveFunc func = new HazardCurveFunc(valuationDate, cds, parSpreads, yieldCurve, hazardRateCurve);

    // fist rate is at t = 0, remove it
    double[] rates = hazardRateCurve.getRates();
    final int n = rates.length - 1;
    ArgumentChecker.isTrue(n == cds.length, "cds length inconsistent with knots in curve ");
    double[] temp = new double[n];
    System.arraycopy(rates, 1, temp, 0, n);
    final DoubleMatrix1D start = new DoubleMatrix1D(temp);
    DoubleMatrix1D res = rootFinder.getRoot(func, start);
    return hazardRateCurve.withRates(res.getData());
  }

  private class HazardCurveFunc extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
    private final int _n;
    private final DummyCDSPricer[] _pricers;
    private final double[] _spreads;
    private final HazardRateCurve _hazardRateCurve;

    public HazardCurveFunc(ZonedDateTime valuationDate, final CreditDefaultSwapDefinition[] cds, final double[] parSpreads, final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve) {
      _n = cds.length;
      ArgumentChecker.isTrue(_n == parSpreads.length, "There are {} CDSs and {} par spreads", _n, parSpreads.length);
      ArgumentChecker.isTrue(_n == hazardRateCurve.getNumberOfCurvePoints(), "There are {} CDSs and {} knot points on curve", _n, hazardRateCurve.getNumberOfCurvePoints());

      _pricers = new DummyCDSPricer[_n];
      for (int i = 0; i < _n; i++) {
        _pricers[i] = new DummyCDSPricer(valuationDate, cds[i], yieldCurve);
      }

      _spreads = parSpreads;
      _hazardRateCurve = hazardRateCurve;

    }

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D r) {
      double[] pv = new double[_n];
      HazardRateCurve curve = _hazardRateCurve.withRates(r.getData());
      for (int i = 0; i < _n; i++) {
        pv[i] = _pricers[i].pv(curve, _spreads[i]);
      }
      return new DoubleMatrix1D(pv);
    }

  }

}
