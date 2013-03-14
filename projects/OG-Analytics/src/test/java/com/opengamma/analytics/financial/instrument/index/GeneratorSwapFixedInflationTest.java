/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class GeneratorSwapFixedInflationTest {

  private static final IndexPrice[] PRICE_INDEXES = MulticurveProviderDiscountDataSets.getPriceIndexes();
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IndexPrice PRICE_INDEX_GPB = PRICE_INDEXES[1];
  private static final Currency CUR = PRICE_INDEX_EUR.getCurrency();
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean EOM = true;
  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2008, 8, 14);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final int COUPON_TENOR_YEAR = 10;
  private static final Period COUPON_TENOR = Period.ofYears(COUPON_TENOR_YEAR);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR, EOM);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final int SPOT_LAG = 2;
  private static final double INDEX_MAY_2008_INTERPOLATED = 108.45483870967742; // May index: 108.23 - June Index = 108.64
  private static final double INDEX_MAY_2008 = 108.23;
  private static final ZonedDateTime REFERENCE_START_DATE = DateUtils.getUTCDate(2008, 5, 18);
  private static final ZonedDateTime REFERENCE_START_DATE_MONTHLY = DateUtils.getUTCDate(2008, 5, 1);
  private static final ZonedDateTime[] REFERENCE_END_DATE = new ZonedDateTime[2];
  private static final boolean IS_LINEAR = true;
  private static final boolean IS_NOT_LINEAR = false;

  static {
    REFERENCE_END_DATE[0] = PAYMENT_DATE.minusMonths(MONTH_LAG).withDayOfMonth(1);
    REFERENCE_END_DATE[1] = PAYMENT_DATE.minusMonths(MONTH_LAG - 1).withDayOfMonth(1);
  }

  //  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final DoubleTimeSeries<ZonedDateTime> HICPX_TS = MulticurveProviderDiscountDataSets.euroHICPXFrom2009();

  public static final GeneratorAttributeIR ATTRIBUTE = new GeneratorAttributeIR(COUPON_TENOR);

  private static final GeneratorSwapFixedInflation GENERATOR_SWAP_INFLATION_LINEAR = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
      SPOT_LAG, IS_LINEAR);
  private static final GeneratorSwapFixedInflation GENERATOR_SWAP_INFLATION_PIECEWISE = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
      SPOT_LAG, IS_NOT_LINEAR);

  @Test
  /**
   * Tests the getters.
   */
  public void getterLinear() {
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY, GENERATOR_SWAP_INFLATION_LINEAR.getBusinessDayConvention());
    assertEquals("GeneratorSwap: getter", CALENDAR, GENERATOR_SWAP_INFLATION_LINEAR.getCalendar());
    assertEquals("GeneratorSwap: getter", PRICE_INDEX_EUR, GENERATOR_SWAP_INFLATION_LINEAR.getIndexPrice());
    String name = "generator";
    assertTrue(name.equals(GENERATOR_SWAP_INFLATION_LINEAR.getName()));
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR.getName(), GENERATOR_SWAP_INFLATION_LINEAR.toString());
    assertEquals("GeneratorSwap: getter", MONTH_LAG, GENERATOR_SWAP_INFLATION_LINEAR.getMonthLag());
    assertEquals("GeneratorSwap: getter", HICPX_TS, GENERATOR_SWAP_INFLATION_LINEAR.getPriceIndexTimeSeries());
    assertTrue("GeneratorSwap: getter", EOM == GENERATOR_SWAP_INFLATION_LINEAR.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG, GENERATOR_SWAP_INFLATION_LINEAR.getSpotLag());
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getterPiecewise() {
    assertEquals("GeneratorSwap: getter", BUSINESS_DAY, GENERATOR_SWAP_INFLATION_PIECEWISE.getBusinessDayConvention());
    assertEquals("GeneratorSwap: getter", CALENDAR, GENERATOR_SWAP_INFLATION_PIECEWISE.getCalendar());
    assertEquals("GeneratorSwap: getter", PRICE_INDEX_EUR, GENERATOR_SWAP_INFLATION_PIECEWISE.getIndexPrice());
    String name = "generator";
    assertTrue(name.equals(GENERATOR_SWAP_INFLATION_PIECEWISE.getName()));
    assertEquals(GENERATOR_SWAP_INFLATION_PIECEWISE.getName(), GENERATOR_SWAP_INFLATION_PIECEWISE.toString());
    assertEquals("GeneratorSwap: getter", MONTH_LAG, GENERATOR_SWAP_INFLATION_PIECEWISE.getMonthLag());
    assertEquals("GeneratorSwap: getter", HICPX_TS, GENERATOR_SWAP_INFLATION_PIECEWISE.getPriceIndexTimeSeries());
    assertTrue("GeneratorSwap: getter", EOM == GENERATOR_SWAP_INFLATION_PIECEWISE.isEndOfMonth());
    assertEquals("GeneratorSwap: getter", SPOT_LAG, GENERATOR_SWAP_INFLATION_PIECEWISE.getSpotLag());
  }

  @Test
  /**
   * Tests the equals.
   */
  public void equalHash() {
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR, GENERATOR_SWAP_INFLATION_LINEAR);
    GeneratorSwapFixedInflation generatorDuplicate = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR, generatorDuplicate);
    assertEquals(GENERATOR_SWAP_INFLATION_LINEAR.hashCode(), generatorDuplicate.hashCode());
    GeneratorSwapFixedInflation generatorModified;
    DoubleTimeSeries<ZonedDateTime> modifiedTimeSeries = MulticurveProviderDiscountDataSets.usCpiFrom2009();
    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, modifiedTimeSeries, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_GPB, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    BusinessDayConvention modifiedBusinessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, modifiedBusinessDay, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    Calendar modifiesCalendar = new MondayToFridayCalendar("B");
    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, modifiesCalendar, EOM, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, false, MONTH_LAG,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, 2,
        SPOT_LAG, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        1, IS_LINEAR);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    generatorModified = new GeneratorSwapFixedInflation("generator", PRICE_INDEX_EUR, HICPX_TS, BUSINESS_DAY, CALENDAR, EOM, MONTH_LAG,
        SPOT_LAG, false);
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));

    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(generatorModified));
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(null));
    assertFalse(GENERATOR_SWAP_INFLATION_LINEAR.equals(CUR));
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponMonthlyConstructor() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponMonthlyDefinition inflationCpn = new CouponInflationZeroCouponMonthlyDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL, PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE_MONTHLY, INDEX_MAY_2008, REFERENCE_END_DATE[0], false);
    CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, PAYMENT_DATE, START_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn);
    SwapFixedInflationZeroCouponDefinition generateSwap = GENERATOR_SWAP_INFLATION_PIECEWISE.generateInstrument(TODAY, zeroCpnRate, NOTIONAL, ATTRIBUTE);
    assertEquals("Swap zero-coupon inflation constructor", swap, generateSwap);
  }

  @Test
  /**
   * Tests the construction of zero-coupon inflation swaps.
   */
  public void swapFixedInflationZeroCouponInterpolationConstructor() {
    final double zeroCpnRate = 0.02;
    final CouponInflationZeroCouponInterpolationDefinition inflationCpn = new CouponInflationZeroCouponInterpolationDefinition(CUR, PAYMENT_DATE, START_DATE, PAYMENT_DATE, 1.0, NOTIONAL,
        PRICE_INDEX_EUR,
        MONTH_LAG, REFERENCE_START_DATE, INDEX_MAY_2008_INTERPOLATED, REFERENCE_END_DATE, false);
    CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(CUR, PAYMENT_DATE, START_DATE, -NOTIONAL, COUPON_TENOR_YEAR, zeroCpnRate);
    SwapFixedInflationZeroCouponDefinition swap = new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn);
    SwapFixedInflationZeroCouponDefinition generateSwap = GENERATOR_SWAP_INFLATION_LINEAR.generateInstrument(TODAY, zeroCpnRate, NOTIONAL, ATTRIBUTE);
    assertEquals("Swap zero-coupon inflation constructor", swap, generateSwap);
  }

}