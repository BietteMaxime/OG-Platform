/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.provider.description.inflation.InflationConvexityAdjustmentProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the convexity adjustment between two times for year on year coupons and for zero coupons (this adjustment is also used for the computation of the forward in optional inflation instruments) 
 */
public class InflationMarketModelConvexityAdjustmentForCoupon {

  /**
   * Computes the convexity adjustment for year on year inflation swap with a monthly index.
   * @param coupon The year on year coupon.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getYearOnYearConvexityAdjustment(final CouponInflationYearOnYearMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double firstFixingTime = coupon.getReferenceStartTime();
    final double secondFixingTime = coupon.getReferenceEndTime();
    final double firstNaturalPaymentTime = coupon.getNaturalPaymentStartTime();
    final double secondNaturalPaymentTime = coupon.getNaturalPaymentEndTime();
    final double paymentTime = coupon.getPaymentTime();
    final double volatilityStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double volatilityEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[1];
    final double correlationInflation = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexCorrelation().getZValue(firstFixingTime, secondFixingTime);
    final double correlationInflationRateStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(firstFixingTime);
    final double correlationInflationRateEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(secondFixingTime);
    final double volBondForwardStart = getVolBondForward(firstNaturalPaymentTime, paymentTime, inflationConvexity);
    final double volBondForwardEnd = getVolBondForward(secondNaturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatilityStart * (volatilityStart - volatilityEnd * correlationInflation - volBondForwardStart * correlationInflationRateStart) * firstNaturalPaymentTime
        + volatilityEnd * volBondForwardEnd * correlationInflationRateEnd * secondNaturalPaymentTime;
    return Math.exp(adjustment);

  }

  /**
   * Computes the convexity adjustment for year on year inflation swap with an interpolated index.
   * @param coupon The year on year coupon.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getYearOnYearConvexityAdjustment(final CouponInflationYearOnYearInterpolation coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double firstFixingTime = coupon.getWeightStart() * coupon.getReferenceStartTime()[0] + (1 - coupon.getWeightStart()) * coupon.getReferenceStartTime()[1];
    final double secondFixingTime = coupon.getWeightEnd() * coupon.getReferenceEndTime()[0] + (1 - coupon.getWeightEnd()) * coupon.getReferenceEndTime()[1];
    final double firstNaturalPaymentTime = coupon.getNaturalPaymentStartTime();
    final double secondNaturalPaymentTime = coupon.getNaturalPaymentEndTime();
    final double paymentTime = coupon.getPaymentTime();
    final double volatilityStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double volatilityEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[1];
    final double correlationInflation = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexCorrelation().getZValue(firstFixingTime, secondFixingTime);
    final double correlationInflationRateStart = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(firstFixingTime);
    final double correlationInflationRateEnd = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(secondFixingTime);
    final double volBondForwardStart = getVolBondForward(firstNaturalPaymentTime, paymentTime, inflationConvexity);
    final double volBondForwardEnd = getVolBondForward(secondNaturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatilityStart * (volatilityStart - volatilityEnd * correlationInflation - volBondForwardStart * correlationInflationRateStart) * firstNaturalPaymentTime
        + volatilityEnd * volBondForwardEnd * correlationInflationRateEnd * secondNaturalPaymentTime;
    return Math.exp(adjustment);

  }

  /**
   * Computes the convexity adjustment for zero coupon inflation swap with a monthly index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CouponInflationZeroCouponMonthly coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getReferenceEndTime();
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the convexity adjustment for zero coupon inflation swap with an interpolated index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CouponInflationZeroCouponInterpolation coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getWeight() * coupon.getReferenceEndTime()[0] + (1 - coupon.getWeight()) * coupon.getReferenceEndTime()[1];
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the convexity adjustment for zero coupon Gearing inflation swap with a monthly index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CouponInflationZeroCouponMonthlyGearing coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getReferenceEndTime();
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the convexity adjustment for zero coupon Gearing inflation swap with an interpolated index.
  * @param coupon The zero-coupon payment.
   * @param inflationConvexity The inflation provider.
   * @return The convexity adjustment.
   */
  public double getZeroCouponConvexityAdjustment(final CouponInflationZeroCouponInterpolationGearing coupon, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflationConvexity, "Inflation");

    final double fixingTime = coupon.getWeight() * coupon.getReferenceEndTime()[0] + (1 - coupon.getWeight()) * coupon.getReferenceEndTime()[1];
    final double naturalPaymentTime = coupon.getNaturalPaymentTime();
    final double paymentTime = coupon.getPaymentTime();

    final double volatility = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexAtmVolatility()[0];
    final double correlationInflationRate = inflationConvexity.getInflationConvexityAdjustmentParameters().getPriceIndexRateCorrelation().getYValue(fixingTime);
    final double volBondForward = getVolBondForward(naturalPaymentTime, paymentTime, inflationConvexity);
    final double adjustment = volatility * volBondForward * correlationInflationRate * naturalPaymentTime;
    return Math.exp(adjustment);
  }

  /**
   * Computes the volatility of a bond forward, a bond forward is defined by his start time and his end time.
   * @param startTime The 
   * @param endTime The
   * @param inflationConvexity The
   * @return The convexity adjustment.
   */
  public double getVolBondForward(final double startTime, final double endTime, final InflationConvexityAdjustmentProviderInterface inflationConvexity) {
    ArgumentChecker.isTrue(startTime <= endTime, null);
    final IborIndex iborIndex = inflationConvexity.getBlackSmileIborCapParameters().getIndex();
    final int liborTenorInMonth = iborIndex.getTenor().getMonths();
    final int numberOfperiod = (int) Math.round((endTime - startTime) / liborTenorInMonth);

    if (numberOfperiod == 0) {
      return 0.0;
    }
    // generate the schedule
    double[] scheduleTimes = new double[numberOfperiod + 1];
    scheduleTimes[numberOfperiod] = endTime;
    for (int i = 0; i < numberOfperiod; i++) {
      scheduleTimes[i] = startTime + i * liborTenorInMonth / 12;
    }

    double[] volatilityComponents = new double[numberOfperiod];
    volatilityComponents[0] = inflationConvexity.getMulticurveProvider().getForwardRate(iborIndex, scheduleTimes[0], scheduleTimes[1], 1.0);
    volatilityComponents[0] = volatilityComponents[0] / (1 + volatilityComponents[0]) * inflationConvexity.getBlackSmileIborCapParameters().getVolatility(scheduleTimes[1]);
    double varBondForward = volatilityComponents[0] * volatilityComponents[0] * scheduleTimes[1];

    for (int i = 1; i < numberOfperiod; i++) {
      volatilityComponents[i] = inflationConvexity.getMulticurveProvider().getForwardRate(iborIndex, scheduleTimes[i], scheduleTimes[i + 1], 1.0);
      volatilityComponents[i] = volatilityComponents[i] / (1 + volatilityComponents[i]) * inflationConvexity.getBlackSmileIborCapParameters().getVolatility(scheduleTimes[i + 1]);
      varBondForward = varBondForward + volatilityComponents[i] * volatilityComponents[i] * scheduleTimes[i + 1];
      for (int j = 0; j < i; j++) {
        varBondForward = varBondForward + 2 * volatilityComponents[i] * volatilityComponents[j] * scheduleTimes[j + 1] *
            inflationConvexity.getInflationConvexityAdjustmentParameters().getLiborCorrelation().getZValue(scheduleTimes[i], scheduleTimes[j]);
      }
    }

    return Math.sqrt(varBondForward) / endTime;
  }
}
