/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.time.DateUtils;

/**
 * The TimeCalculator computes the difference between two instants as 'Analytics time', 
 * which is actually a measure of years. This is used primarily for interest accrual and curve/surface interpolation
 */
public class TimeCalculatorTest {

  private static final double TOLERANCE = 1.0E-50;

  @Test
  /** Same instant must have no time between */
  public void sameInstant() {
    final ZonedDateTime now = ZonedDateTime.now();
    assertEquals(0.0, TimeCalculator.getTimeBetween(now, now));
  }

  @Test
  /** No time between instants on same date */
  public void sameDay() {

    final ZonedDateTime midday = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDDAY, TimeZone.UTC);
    final ZonedDateTime midnight = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, TimeZone.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midday, midnight);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** No time between instants on same date */
  public void sameDay2() {

    final ZonedDateTime midday = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDDAY, TimeZone.UTC);
    final ZonedDateTime midnight = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, TimeZone.UTC);
    final double yearFraction = TimeCalculator.getTimeBetween(midnight, midday);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** Time between same instants but specified under time zones that fall on different days.
      This is trapped as daycount computation first converts each ZonedDateTime to LocalDate. */
  public void sameTimeDifferentLocalDates() {

    final ZonedDateTime midnightLondon = ZonedDateTime.of(LocalDate.of(2012, 03, 12), LocalTime.MIDNIGHT, TimeZone.UTC);
    final ZonedDateTime sevenNewYork = ZonedDateTime.of(LocalDate.of(2012, 03, 11), LocalTime.of(19, 0), TimeZone.of("EST"));
    assertTrue(midnightLondon.equalInstant(sevenNewYork));
    final double yearFraction = TimeCalculator.getTimeBetween(sevenNewYork, midnightLondon);
    assertEquals(0.0, yearFraction, TOLERANCE);
  }

  @Test
  /** Time between normal days (in a non leap year) */
  public void normal() {
    final ZonedDateTime date1 = DateUtils.getUTCDate(2010, 8, 18);
    final ZonedDateTime date2 = DateUtils.getUTCDate(2010, 8, 21);
    final double time = TimeCalculator.getTimeBetween(date1, date2);
    final double timeExpected = 3.0 / 365.0;
    assertEquals("TimeCalculator: normal days", timeExpected, time, TOLERANCE);
  }

  @Test
  /** Time between arrays */
  public void array() {
    final ZonedDateTime date1 = DateUtils.getUTCDate(2010, 8, 18);
    final ZonedDateTime date2 = DateUtils.getUTCDate(2010, 8, 21);
    final ZonedDateTime[] dateArray1 = new ZonedDateTime[] {date1, date2 };
    final ZonedDateTime[] dateArray2 = new ZonedDateTime[] {date2, date1 };
    final double[] timeCalculated = TimeCalculator.getTimeBetween(dateArray1, dateArray2);
    final double timeExpected = 3.0 / 365.0;
    assertArrayEquals("TimeCalculator: normal days array", new double[] {timeExpected, -timeExpected }, timeCalculated, TOLERANCE);
    final double[] timeCalculated2 = TimeCalculator.getTimeBetween(date1, dateArray2);
    assertArrayEquals("TimeCalculator: normal days array", new double[] {timeExpected, 0.0 }, timeCalculated2, TOLERANCE);
  }

}
