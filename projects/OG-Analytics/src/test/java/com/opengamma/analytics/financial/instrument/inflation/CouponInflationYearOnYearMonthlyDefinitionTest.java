/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the year on year inflation constructors.
 */
public class CouponInflationYearOnYearMonthlyDefinitionTest {

  private static final String NAME = "Euro HICP x";
  private static final Currency CUR = Currency.EUR;
  private static final IndexPrice PRICE_INDEX = new IndexPrice(NAME, CUR);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE.minusDays(1); // For getter test
  private static final ZonedDateTime ACCRUAL_START_DATE = ACCRUAL_END_DATE.minusMonths(12);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_APRIL_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final ZonedDateTime REFERENCE_END_DATE = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
  private static final ZonedDateTime REFERENCE_START_DATE = REFERENCE_END_DATE.minusMonths(12);
  private static final CouponInflationYearOnYearMonthlyDefinition YoY_COUPON_DEFINITION = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0,
      NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String PRICE_INDEX_CURVE_NAME = "Price index";
  private static final String[] CURVE_NAMES = new String[] {DISCOUNTING_CURVE_NAME, PRICE_INDEX_CURVE_NAME };
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponInflationYearOnYearMonthlyDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPay() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, null, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, null, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, null, MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE,
        false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefStart() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, null, REFERENCE_END_DATE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRefEnd() {
    new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE, null, false);
  }

  @Test
  /**
   * Tests the class getter.
   */
  public void getter() {
    assertEquals("Inflation Year on Year coupon: getter", CUR, YoY_COUPON_DEFINITION.getCurrency());
    assertEquals("Inflation Year on Year coupon: getter", PAYMENT_DATE, YoY_COUPON_DEFINITION.getPaymentDate());
    assertEquals("Inflation Year on Year coupon: getter", ACCRUAL_START_DATE, YoY_COUPON_DEFINITION.getAccrualStartDate());
    assertEquals("Inflation Year on Year coupon: getter", ACCRUAL_END_DATE, YoY_COUPON_DEFINITION.getAccrualEndDate());
    assertEquals("Inflation Year on Year coupon: getter", 1.0, YoY_COUPON_DEFINITION.getPaymentYearFraction());
    assertEquals("Inflation Year on Year coupon: getter", NOTIONAL, YoY_COUPON_DEFINITION.getNotional());
    assertEquals("Inflation Year on Year coupon: getter", PRICE_INDEX, YoY_COUPON_DEFINITION.getPriceIndex());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_START_DATE, YoY_COUPON_DEFINITION.getReferenceStartDate());
    assertEquals("Inflation Year on Year coupon: getter", REFERENCE_END_DATE, YoY_COUPON_DEFINITION.getReferenceEndDate());
    assertEquals("Inflation Year on Year coupon: getter", MONTH_LAG, YoY_COUPON_DEFINITION.getMonthLag());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals(YoY_COUPON_DEFINITION, YoY_COUPON_DEFINITION);
    CouponInflationYearOnYearMonthlyDefinition couponDuplicate = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX,
        MONTH_LAG, REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    assertEquals(YoY_COUPON_DEFINITION, couponDuplicate);
    assertEquals(YoY_COUPON_DEFINITION.hashCode(), couponDuplicate.hashCode());
    CouponInflationYearOnYearMonthlyDefinition modified;
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE.minusDays(1), ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE.minusDays(1), 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE.minusDays(1),
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE.minusDays(1), false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 2.0, NOTIONAL, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
    modified = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, 1.0, NOTIONAL + 10.0, PRICE_INDEX, MONTH_LAG, REFERENCE_START_DATE,
        REFERENCE_END_DATE, false);
    assertFalse(YoY_COUPON_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the first based on indexation lag.
   */
  public void from2() {
    CouponInflationYearOnYearMonthlyDefinition constructor = new CouponInflationYearOnYearMonthlyDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX, MONTH_LAG,
        REFERENCE_START_DATE, REFERENCE_END_DATE, false);
    CouponInflationYearOnYearMonthlyDefinition from = CouponInflationYearOnYearMonthlyDefinition.from(ACCRUAL_START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX, MONTH_LAG, false);
    assertEquals("Inflation zero-coupon : from", constructor, from);
  }

  @Test
  public void toDerivativesNoData() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    Coupon zeroCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, CURVE_NAMES);
    double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    CouponInflationYearOnYearMonthly zeroCoupon = new CouponInflationYearOnYearMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime, referenceEndTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCouponConverted, zeroCoupon);
  }

  @Test
  public void toDerivativesStartMonthNotknown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2011, 7, 29);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 1),
        DateUtils.getUTCDate(2017, 6, 1), DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1) },
        new double[] {
            127.23, 127.43, 128.23, 128.43 });
    Coupon zeroCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS, CURVE_NAMES);
    double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    final double referenceStartTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_START_DATE);
    final double referenceEndTime = ACT_ACT.getDayCountFraction(pricingDate, REFERENCE_END_DATE);
    CouponInflationYearOnYearMonthly zeroCoupon = new CouponInflationYearOnYearMonthly(CUR, paymentTime, 1.0, NOTIONAL, PRICE_INDEX, referenceStartTime, referenceEndTime, false);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

  @Test
  public void toDerivativesStartMonthKnown() {
    final ZonedDateTime pricingDate = DateUtils.getUTCDate(2018, 6, 25);
    final DoubleTimeSeries<ZonedDateTime> priceIndexTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2017, 5, 1),
        DateUtils.getUTCDate(2017, 6, 1), DateUtils.getUTCDate(2018, 5, 1), DateUtils.getUTCDate(2018, 6, 1) },
        new double[] {
            127.23, 127.43, 128.23, 128.43 });
    Coupon zeroCouponConverted = YoY_COUPON_DEFINITION.toDerivative(pricingDate, priceIndexTS, CURVE_NAMES);
    double paymentTime = ACT_ACT.getDayCountFraction(pricingDate, PAYMENT_DATE);
    CouponFixed zeroCoupon = new CouponFixed(CUR, paymentTime, DISCOUNTING_CURVE_NAME, 1.0, NOTIONAL, 128.23 / 127.23 - 1.0);
    assertEquals("Inflation zero-coupon: toDerivative", zeroCoupon, zeroCouponConverted);
  }

}
