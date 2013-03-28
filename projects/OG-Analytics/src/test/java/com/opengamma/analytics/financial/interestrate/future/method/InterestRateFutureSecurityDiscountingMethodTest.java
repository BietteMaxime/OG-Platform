/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.calculator.PriceFromCurvesDiscountingCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing without convexity adjustment.
 */
public class InterestRateFutureSecurityDiscountingMethodTest {
  // EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;
  // Time version
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  // private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  //  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE,
      NOTIONAL, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUT_SEC = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final InterestRateFutureTransactionDiscountingMethod METHOD_FUT_TRA = InterestRateFutureTransactionDiscountingMethod.getInstance();
  private static final PriceFromCurvesDiscountingCalculator PRICE_CALCULATOR = PriceFromCurvesDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRCSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Test the price computed from the curves
   */
  public void price() {
    final double price = METHOD_FUT_SEC.price(ERU2, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double forward = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    final double expectedPrice = 1.0 - forward;
    assertEquals("Future price from curves", expectedPrice, price);
  }

  @Test
  /**
   * Tests the method versus the calculator for the price.
   */
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD_FUT_SEC.price(ERU2, CURVES);
    final double priceCalculator = ERU2.accept(PRICE_CALCULATOR, CURVES);
    assertEquals("Bond future security Discounting: Method vs calculator", priceMethod, priceCalculator, 1.0E-10);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRate() {
    final double rate = METHOD_FUT_SEC.parRate(ERU2, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double expectedRate = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    assertEquals("Future price from curves", expectedRate, rate, 1.0E-10);
  }

  @Test
  /**
   * Test the rate computed from the method and from the calculator.
   */
  public void parRateMethodVsCalculator() {
    final double rateMethod = METHOD_FUT_SEC.parRate(ERU2, CURVES);
    final ParRateCalculator calculator = ParRateCalculator.getInstance();
    final double rateCalculator = ERU2.accept(calculator, CURVES);
    assertEquals("Future price from curves", rateMethod, rateCalculator, 1.0E-10);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRateCurveSensitivityMethodVsCalculator() {
    InterestRateCurveSensitivity prSensiMethod = METHOD_FUT_SEC.parRateCurveSensitivity(ERU2, CURVES);
    InterestRateCurveSensitivity prSensiCalculator = new InterestRateCurveSensitivity(ERU2.accept(PRCSC, CURVES));
    AssertSensivityObjects.assertEquals("", prSensiMethod, prSensiCalculator, TOLERANCE_PV_DELTA);
    InterestRateFutureTransaction trERU2 = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY);
    InterestRateCurveSensitivity prSensiCalculator2 = new InterestRateCurveSensitivity(trERU2.accept(PRCSC, CURVES));
    AssertSensivityObjects.assertEquals("", prSensiMethod, prSensiCalculator2, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the par spread.
   */
  public void parSpread() {
    final double parSpread = ERU2.accept(ParSpreadMarketQuoteCalculator.getInstance(), CURVES);
    final InterestRateFutureTransaction futures0 = new InterestRateFutureTransaction(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE + parSpread,
        NOTIONAL, FUTURE_FACTOR,
        QUANTITY, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    final CurrencyAmount pv0 = METHOD_FUT_TRA.presentValue(futures0, CURVES);
    assertEquals("Future par spread", pv0.getAmount(), 0, TOLERANCE_PV);
  }

}
