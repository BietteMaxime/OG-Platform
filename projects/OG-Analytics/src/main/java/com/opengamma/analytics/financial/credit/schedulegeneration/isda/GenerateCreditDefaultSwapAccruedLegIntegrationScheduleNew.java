/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate the integration schedules for instrument legs which require a numerical integration
 * (for example the contingent and accrued legs of a CDS require numerical evaluation of integrals)
 */
public class GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // TODO : Add check that startTime < endTime in schedule generation routine

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the accrued leg calculation

  public ZonedDateTime[] constructCreditDefaultSwapAccruedLegIntegrationSchedule(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final boolean includeSchedule) {
    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");
    // Do we want to include the CDS premium leg cashflow schedule as points
    // Get the start time of the CDS with respect to the current valuation date (can obviously be negative)
    final double startTime = calculateCreditDefaultSwapStartTime(valuationDate, cds, ACT_365);
    // Get the (offset) maturity of the CDS
    final double offsetMaturityTime = calculateCreditDefaultSwapOffsetMaturity(valuationDate, cds, ACT_365);
    final ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime endDate = cds.getMaturityDate();
    if (cds.getProtectionStart()) {
      endDate = endDate.plusDays(1);
    }
    // Calculate the schedule of integration timenodes for the accrued leg calculation
    final ZonedDateTime[] timeNodes = constructISDACompliantAccruedLegIntegrationSchedule(yieldCurve, hazardRateCurve, startDate, endDate);
    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the contingent leg calculation

  public double[] constructCreditDefaultSwapContingentLegIntegrationSchedule(
      final ZonedDateTime valuationDate,
      final ZonedDateTime startDate,
      final ZonedDateTime endDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");

    // Do we want to include the CDS premium leg cashflow schedule as points
    final boolean includeSchedule = false;

    // Calculate the time at which protection starts
    double protectionStartTime = TimeCalculator.getTimeBetween(valuationDate, startDate, ACT_365); //calculateProtectionStartTime(valuationDate, cds, ACT_365);
    final double protectionEndTime = TimeCalculator.getTimeBetween(valuationDate, endDate, ACT_365);

    // Calculate the maturity of the CDS with respect to the valuation date
    final double maturity = calculateCreditDefaultSwapMaturity(valuationDate, cds, ACT_365);

    if (Double.compare(protectionStartTime, -0.0) == 0) {
      protectionStartTime = 0;
    }
    // Calculate the schedule of integration timenodes for the contingent leg calculation
    final double[] timeNodes = constructISDACompliantIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, protectionStartTime, /*maturity*/protectionEndTime, includeSchedule);

    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to construct a set of timenodes compliant with the ISDA model (adapted from the good RiskCare implementation)

  private double[] constructISDACompliantIntegrationSchedule(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final double startTime,
      final double endTime,
      final boolean includeSchedule) {

    // ------------------------------------------------

    // Check input arguments are not null

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard rate curve");

    ArgumentChecker.notNull(startTime, "Start time");
    ArgumentChecker.notNull(endTime, "End time");

    // TODO : Check ordering of startTime and endTime

    // ------------------------------------------------

    // TODO : Move these lines into the includeSchedule code as they are only used there

    final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegCashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    final ZonedDateTime[] cashflowSchedule = premiumLegCashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    final double[] cashflowScheduleAsDoubles = premiumLegCashflowSchedule.convertTenorsToDoubles(cashflowSchedule, valuationDate, ACT_365);

    // ------------------------------------------------

    // All the timenodes in the list
    final NavigableSet<Double> allTimePoints = new TreeSet<Double>();

    // The subset of timenodes in the range over which protection extends
    Set<Double> timePointsInRange;

    // ------------------------------------------------

    // Add the timenodes on the rates curve
    double[] x = yieldCurve.getTimePoints();

    for (final double element : x) {
      allTimePoints.add(new Double(element));
    }

    // Add the timenodes on the hazard rate curve
    x = hazardRateCurve.getShiftedTimePoints();

    for (final double element : x) {
      allTimePoints.add(new Double(element));
    }

    // TODO : There is a known bug observed when adding endTime to the list, if endTime is very close (numerically) to one of the entries in x
    // TODO : This leads to two numbers which differ at ~O(10^-15). This causes an error in the contingent leg calc leading to a NaN value

    // Add the timenodes at the times when protection starts and ends
    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));

    // ------------------------------------------------

    if (includeSchedule) {

      double offset = 0.0;

      if (cds.getProtectionStart()) {
        offset = cds.getProtectionOffset();
      }

      final double offsetStartTime = TimeCalculator.getTimeBetween(valuationDate, cashflowSchedule[1], ACT_365) - offset;

      allTimePoints.add(new Double(offsetStartTime));

      double periodEndTime = 0.0;
      for (int i = 0; i < cashflowSchedule.length; i++) {

        if (i < cashflowSchedule.length - 1) {
          periodEndTime = cashflowScheduleAsDoubles[i] - offset;
        } else {
          periodEndTime = cashflowScheduleAsDoubles[i];
        }

        allTimePoints.add(new Double(periodEndTime));
      }

      timePointsInRange = allTimePoints.subSet(new Double(offsetStartTime), true, new Double(endTime), true);

    } else {
      timePointsInRange = allTimePoints.subSet(new Double(startTime), true, new Double(endTime), true);
    }

    // ------------------------------------------------

    final Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);

    final double[] timePoints = new double[boxed.length];

    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
    }

    // ------------------------------------------------

    return timePoints;
  }

  // ----------------------------------------------------------------------------------------------------

  // This is a total hack just to get the accrued leg calculation working - need to re-merge with the other functions
  // This is actually how I want to re-write the other function

  private ZonedDateTime[] constructISDACompliantAccruedLegIntegrationSchedule(
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime startDate,
      final ZonedDateTime endDate) {
    ArgumentChecker.isTrue(startDate.isBefore(endDate), "Start date {} must be before end date {}", startDate, endDate);
    final ZonedDateTime[] yieldCurveDates = yieldCurve.getCurveDates();
    final ZonedDateTime[] hazardCurveDates = hazardRateCurve.getCurveTenors();
    final int nYieldCurveDates = yieldCurveDates.length;
    final int nHazardCurveDates = hazardCurveDates.length;
    final int total = nYieldCurveDates + nHazardCurveDates;
    final ZonedDateTime[] result = new ZonedDateTime[total + 2];
    System.arraycopy(yieldCurveDates, 0, result, 0, nYieldCurveDates);
    System.arraycopy(hazardCurveDates, 0, result, nYieldCurveDates, nHazardCurveDates);
    result[total] = startDate;
    result[total + 1] = endDate;
    return new GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew().getTruncatedTimeLine(result, startDate, endDate, false);
  }

  public ZonedDateTime[] getTruncatedTimeLine(final ZonedDateTime[] allDates, final ZonedDateTime startDate, final ZonedDateTime endDate,
      final boolean sorted) {
    ArgumentChecker.notNull(allDates, "all dates");
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    //TODO should allDates not be empty?
    ArgumentChecker.isTrue(startDate.isBefore(endDate), "Start date {} must be before end date {}", startDate, endDate);
    final int n = allDates.length;
    final ZonedDateTime[] dates = new ZonedDateTime[n];
    System.arraycopy(allDates, 0, dates, 0, n);
    final LinkedHashSet<ZonedDateTime> truncated = new LinkedHashSet<>();
    final LocalDate startDateAsLocal = startDate.toLocalDate();
    final LocalDate endDateAsLocal = endDate.toLocalDate();
    for (final ZonedDateTime date : dates) {
      final LocalDate localDate = date.toLocalDate();
      if (!(localDate.isBefore(startDateAsLocal) || localDate.isAfter(endDateAsLocal))) {
        truncated.add(date);
      }
    }
    final int truncatedSize = truncated.size();
    if (truncatedSize == 0) {
      return new ZonedDateTime[] {startDate, endDate };
    }
    final ZonedDateTime[] truncatedArray = truncated.toArray(new ZonedDateTime[truncatedSize]);
    if (!sorted) {
      Arrays.sort(truncatedArray);
    }
    if (truncatedArray[0].equals(startDate)) {
      if (truncatedArray[truncatedSize - 1].equals(endDate)) {
        return truncatedArray;
      }
      final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 0, truncatedSize);
      result[truncatedSize] = endDate;
      return result;
    }
    if (truncatedArray[truncatedSize - 1].equals(endDate)) {
      final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
      result[0] = startDate;
      return result;
    }
    final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 2];
    System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
    result[0] = startDate;
    result[truncatedSize + 1] = endDate;
    return result;
  }

  // -------------------------------------------------------------------------------------------

  public double calculateCreditDefaultSwapStartTime(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final DayCount dayCount) {

    final double startTime = TimeCalculator.getTimeBetween(valuationDate, cds.getStartDate(), dayCount);

    return startTime;
  }

  // -------------------------------------------------------------------------------------------------

  // TODO : Note this is hard coded at the moment
  // Calculate the step in time for the CDS (the point at which protection coverage starts)
  public double calculateCreditDefaultSwapStepinTime(
      final ZonedDateTime valuationDate,
      final DayCount dayCount) {

    final double stepInTime = TimeCalculator.getTimeBetween(valuationDate, valuationDate.plusDays(1), dayCount);

    return stepInTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset step in time for the CDS
  public double calculateCreditDefaultSwapOffsetStepinTime(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final DayCount dayCount) {

    double offset = 0.0;
    final double stepinTime = calculateCreditDefaultSwapStepinTime(valuationDate, dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    final double offsetStepinTime = stepinTime - offset;

    return offsetStepinTime;
  }

  // -------------------------------------------------------------------------------------------------

  public double calculateProtectionStartTime(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final DayCount dayCount) {

    // Calculate the start time of the CDS contract with respect to the valuation date (can be negative obviously)
    final double startTime = calculateCreditDefaultSwapStartTime(valuationDate, cds, dayCount);

    final double stepInTime = calculateCreditDefaultSwapStepinTime(valuationDate, dayCount);

    final double offsetPricingTime = -cds.getProtectionOffset();
    final double offsetStepinTime = stepInTime - cds.getProtectionOffset();

    final double protectionStartTime = Math.max(Math.max(startTime, offsetStepinTime), offsetPricingTime);

    return protectionStartTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the maturity of the CDS with respect to the valuation date
  public double calculateCreditDefaultSwapMaturity(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final DayCount dayCount) {

    final ZonedDateTime mat = cds.getMaturityDate();
    final double maturity = TimeCalculator.getTimeBetween(valuationDate, cds.getMaturityDate(), dayCount);

    return maturity;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset maturity for the CDS contract
  public double calculateCreditDefaultSwapOffsetMaturity(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final DayCount dayCount) {

    double offset = 0.0;

    final double maturity = TimeCalculator.getTimeBetween(valuationDate, cds.getMaturityDate(), dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    final double offsetMaturity = maturity + offset;

    return offsetMaturity;
  }

  // -------------------------------------------------------------------------------------------------
}
