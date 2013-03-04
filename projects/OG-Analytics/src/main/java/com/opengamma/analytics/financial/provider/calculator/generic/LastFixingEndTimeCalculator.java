/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Calculator of the last fixing end time.
 * For fixed coupon, it is 0.
 */
public final class LastFixingEndTimeCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final LastFixingEndTimeCalculator INSTANCE = new LastFixingEndTimeCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static LastFixingEndTimeCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private LastFixingEndTimeCalculator() {
  }

  // -----     Deposit     ------

  @Override
  public Double visitCash(final Cash cash) {
    return cash.getEndTime();
  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit) {
    return deposit.getEndTime();
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return 0.0;
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    return payment.getFixingPeriodEndTime();
  }

  @Override
  public Double visitCouponOIS(final CouponOIS payment) {
    return payment.getFixingPeriodEndTime();
  }

  // -----     Annuity     ------

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    double result = 0.0;
    for (int loopp = 0; loopp < annuity.getNumberOfPayments(); loopp++) {
      result = Math.max(result, annuity.getNthPayment(loopp).accept(this));
    }
    return result;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  // -----     Swap     ------

  @Override
  public Double visitSwap(final Swap<?, ?> swap) {
    final double a = swap.getFirstLeg().accept(this);
    final double b = swap.getSecondLeg().accept(this);
    return Math.max(a, b);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visitSwap(swap);
  }

}
