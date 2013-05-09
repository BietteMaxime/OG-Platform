/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.interestrate.inflation.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 * Pricing method for inflation Year on Year. The price is computed by index estimation and discounting.
 */
public class CouponInflationYearOnYearInterpolationDiscountingMethod {

  /**
   * Computes the present value of the Year on Year coupon without convexity adjustment.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflation, "Inflation");
    double estimatedIndexStart = indexEstimationStart(coupon, inflation);
    double estimatedIndexEnd = indexEstimationEnd(coupon, inflation);
    double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    double pv = coupon.getPaymentYearFraction() * (estimatedIndexEnd / estimatedIndexStart - (coupon.payNotional() ? 0.0 : 1.0)) * discountFactor * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), pv);
  }

  /**
   * Computes the net amount of the Year on Year coupon with reference index at start of the month.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The net amount.
   */

  public MultipleCurrencyAmount netAmount(CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflation, "Inflation");
    double estimatedIndexStart = indexEstimationStart(coupon, inflation);
    double estimatedIndexEnd = indexEstimationEnd(coupon, inflation);
    double na = (estimatedIndexEnd / estimatedIndexStart - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional();
    return MultipleCurrencyAmount.of(coupon.getCurrency(), na);
  }

  /**
   * Computes the estimated index with the weight and the reference start date.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The estimated index for the reference start date.
   */
  public double indexEstimationStart(CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[1]);
    return coupon.getWeightStart() * estimatedIndexMonth0 + (1 - coupon.getWeightStart()) * estimatedIndexMonth1;

  }

  /**
   * Computes the estimated index with the weight and the reference end date.
   * @param coupon The zero-coupon payment.
   * @param inflation The inflation provider.
   * @return The estimated index for the reference end date.
   */
  public double indexEstimationEnd(CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    final double estimatedIndexMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    final double estimatedIndexMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    return coupon.getWeightEnd() * estimatedIndexMonth0 + (1 - coupon.getWeightEnd()) * estimatedIndexMonth1;
  }

  /**
   * Compute the present value sensitivity to rates of a Inflation coupon.
   * @param coupon The coupon.
   * @param inflation The inflation provider.
   * @return The present value sensitivity.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final CouponInflationYearOnYearInterpolation coupon, final InflationProviderInterface inflation) {
    Validate.notNull(coupon, "Coupon");
    Validate.notNull(inflation, "Inflation");
    double estimatedIndexStartMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[0]);
    double estimatedIndexStartMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceStartTime()[1]);
    double estimatedIndexEndMonth0 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[0]);
    double estimatedIndexEndMonth1 = inflation.getPriceIndex(coupon.getPriceIndex(), coupon.getReferenceEndTime()[1]);
    double estimatedIndexStart = coupon.getWeightStart() * estimatedIndexStartMonth0 + (1 - coupon.getWeightStart()) * estimatedIndexStartMonth1;
    double estimatedIndexEnd = coupon.getWeightEnd() * estimatedIndexEndMonth0 + (1 - coupon.getWeightEnd()) * estimatedIndexEndMonth1;
    double discountFactor = inflation.getDiscountFactor(coupon.getCurrency(), coupon.getPaymentTime());
    // Backward sweep
    final double pvBar = 1.0;
    double discountFactorBar = (estimatedIndexEnd / estimatedIndexStart - (coupon.payNotional() ? 0.0 : 1.0)) * coupon.getNotional() * pvBar;
    double estimatedIndexEndBar = 1.0 / estimatedIndexStart * discountFactor * coupon.getNotional() * pvBar;
    double estimatedIndexStartBar = -estimatedIndexEnd / (estimatedIndexStart * estimatedIndexStart) * discountFactor * coupon.getNotional() * pvBar;
    double estimatedIndexEndMonth1bar = (1 - coupon.getWeightEnd()) * estimatedIndexEndBar;
    double estimatedIndexEndMonth0bar = coupon.getWeightEnd() * estimatedIndexEndBar;
    double estimatedIndexStartMonth1bar = (1 - coupon.getWeightStart()) * estimatedIndexStartBar;
    double estimatedIndexStartMonth0bar = coupon.getWeightStart() * estimatedIndexStartBar;
    final Map<String, List<DoublesPair>> resultMapDisc = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(coupon.getPaymentTime(), -coupon.getPaymentTime() * discountFactor * discountFactorBar));
    resultMapDisc.put(inflation.getName(coupon.getCurrency()), listDiscounting);
    final Map<String, List<DoublesPair>> resultMapPrice = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> listPrice = new ArrayList<DoublesPair>();
    listPrice.add(new DoublesPair(coupon.getReferenceEndTime()[0], estimatedIndexEndMonth0bar));
    listPrice.add(new DoublesPair(coupon.getReferenceEndTime()[1], estimatedIndexEndMonth1bar));
    listPrice.add(new DoublesPair(coupon.getReferenceStartTime()[0], estimatedIndexStartMonth0bar));
    listPrice.add(new DoublesPair(coupon.getReferenceStartTime()[1], estimatedIndexStartMonth1bar));
    resultMapPrice.put(inflation.getName(coupon.getPriceIndex()), listPrice);
    final InflationSensitivity inflationSensitivity = InflationSensitivity.ofYieldDiscountingAndPriceIndex(resultMapDisc, resultMapPrice);
    return MultipleCurrencyInflationSensitivity.of(coupon.getCurrency(), inflationSensitivity);
  }

}
