/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the interest rate future security description.
 */
public class InterestRateFuturesTransactionDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final IborIndex IBOR_INDEX = IndexIborMaster.getInstance().getIndex("EURIBOR3M", CALENDAR);

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -IBOR_INDEX.getSpotLag(), CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, IBOR_INDEX.getTenor(), IBOR_INDEX.getBusinessDayConvention(), CALENDAR,
      IBOR_INDEX.isEndOfMonth());
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private static final double TRADE_PRICE = 0.9925;

  private static final InterestRateFutureTransactionDefinition ERU2_DEFINITION = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE,
      SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTradeDate() {
    new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, null, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, null, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, null);
  }

  @Test
  public void getter() {
    assertEquals(LAST_TRADING_DATE, ERU2_DEFINITION.getLastTradingDate());
    assertEquals(IBOR_INDEX, ERU2_DEFINITION.getIborIndex());
    assertEquals(NOTIONAL, ERU2_DEFINITION.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2_DEFINITION.getPaymentAccrualFactor());
    assertEquals(NAME, ERU2_DEFINITION.getName());
    assertEquals(SPOT_LAST_TRADING_DATE, ERU2_DEFINITION.getFixingPeriodStartDate());
    assertEquals(ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, IBOR_INDEX.getTenor(), IBOR_INDEX.getBusinessDayConvention(), CALENDAR, IBOR_INDEX.isEndOfMonth()),
        ERU2_DEFINITION.getFixingPeriodEndDate());
    assertEquals(IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE), ERU2_DEFINITION.getFixingPeriodAccrualFactor());
  }

  @Test
  public void equalHash() {
    InterestRateFutureTransactionDefinition other = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE,
        IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
    assertTrue(ERU2_DEFINITION.equals(other));
    assertTrue(ERU2_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureTransactionDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureTransactionDefinition(TRADE_DATE.plusDays(1), TRADE_PRICE, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL,
        FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE + 0.1, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL,
        FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY + 1, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL,
        FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE.plusDays(1), SPOT_LAST_TRADING_DATE, FIXING_END_DATE, IBOR_INDEX, NOTIONAL,
        FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    IborIndex otherIndex = new IborIndex(IBOR_INDEX.getCurrency(), IBOR_INDEX.getTenor(), IBOR_INDEX.getSpotLag(), CALENDAR, IBOR_INDEX.getDayCount(), IBOR_INDEX.getBusinessDayConvention(),
        !IBOR_INDEX.isEndOfMonth());
    modifiedFuture = new InterestRateFutureTransactionDefinition(TRADE_DATE, TRADE_PRICE, QUANTITY, LAST_TRADING_DATE, SPOT_LAST_TRADING_DATE, FIXING_END_DATE, otherIndex, NOTIONAL, FUTURE_FACTOR,
        NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    assertFalse(ERU2_DEFINITION.equals(IBOR_INDEX));
    assertFalse(ERU2_DEFINITION.equals(null));
  }

  @Test
  public void toDerivative() {
    double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
    double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
    double FIXING_ACCRUAL = IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    InterestRateFutureTransaction ERU2 = new InterestRateFutureTransaction(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, TRADE_PRICE, NOTIONAL, FUTURE_FACTOR,
        QUANTITY, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    InterestRateFutureTransaction convertedERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE, CURVES);
    assertTrue("Rate future security converter", ERU2.equals(convertedERU2));
  }
}
