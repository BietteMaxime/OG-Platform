/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

/**
 * 
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The return type of the calculation
 */
public abstract class InstrumentDerivativeVisitorAdapter<DATA_TYPE, RESULT_TYPE> implements InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill, final DATA_TYPE data) {
    return getException(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill, final DATA_TYPE data) {
    return getException(bill, data);
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final DATA_TYPE data) {
    return getException(fixedCouponAnnuity, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra, final DATA_TYPE data) {
    return getException(fra, data);
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final DATA_TYPE data) {
    return getException(genericAnnuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final DATA_TYPE data) {
    return getException(annuity, data);
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final DATA_TYPE data) {
    return getException(swaption, data);
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCDSDerivative(final ISDACDSDerivative cds, final DATA_TYPE data) {
    return getException(cds, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurity(final BondFixedSecurity bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransaction(final BondFixedTransaction bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurity(final BondIborSecurity bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborTransaction(final BondIborTransaction bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurity(final BillSecurity bill) {
    return getException(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransaction(final BillTransaction bill) {
    return getException(bill);
  }

  @Override
  public RESULT_TYPE visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    return getException(fixedCouponAnnuity);
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash, final DATA_TYPE data) {
    return getException(cash, data);
  }

  @Override
  public RESULT_TYPE visitCash(final Cash cash) {
    return getException(cash);
  }

  @Override
  public RESULT_TYPE visitCapFloorIbor(final CapFloorIbor payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMS(final CapFloorCMS payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return getException(fra);
  }

  @Override
  public RESULT_TYPE visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
    return getException(genericAnnuity);
  }

  @Override
  public RESULT_TYPE visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return getException(annuity);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    return getException(swaption);
  }

  @Override
  public RESULT_TYPE visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap) {
    return getException(swap);
  }

  @Override
  public RESULT_TYPE visitFixedPayment(final PaymentFixed payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCDSDerivative(final ISDACDSDerivative cds) {
    return getException(cds);
  }

  // -----     Payment and coupon     -----

  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixed(final CouponFixed payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIbor(final CouponIbor payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverage(final CouponIborAverage payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpread(final CouponIborSpread payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearing(final CouponIborGearing payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompounding(final CouponIborCompounding payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOIS payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOIS(final CouponOIS payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageON(final CouponArithmeticAverageON payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageON(final CouponArithmeticAverageON payment) {
    return getException(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment, final DATA_TYPE data) {
    return getException(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMS(final CouponCMS payment) {
    return getException(payment);
  }

  // -----     Inflation     -----

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon) {
    return getException(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon, final DATA_TYPE data) {
    return getException(coupon, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
    return getException(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final DATA_TYPE data) {
    return getException(bond, data);
  }

  // -----     Futures     -----

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitCashSettledFuture(final CashSettledFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture bondFuture, final DATA_TYPE data) {
    return getException(bondFuture, data);
  }

  @Override
  public RESULT_TYPE visitBondFuture(final BondFuture bondFuture) {
    return getException(bondFuture);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final DATA_TYPE data) {
    return getException(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures) {
    return getException(futures);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransaction(final DeliverableSwapFuturesTransaction futures, final DATA_TYPE data) {
    return getException(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransaction(final DeliverableSwapFuturesTransaction futures) {
    return getException(futures);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
    return getException(option);
  }

  // -----     Annuity     -----

  // -----     Swap     -----

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap, final DATA_TYPE data) {
    return getException(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwap(final Swap<?, ?> swap) {
    return getException(swap);
  }

  // -----     Deposit     -----

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIbor(final DepositIbor deposit) {
    return getException(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpart(final DepositCounterpart deposit) {
    return getException(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit, final DATA_TYPE data) {
    return getException(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZero(final DepositZero deposit) {
    return getException(deposit);
  }

  // -----     Forex     -----

  @Override
  public RESULT_TYPE visitForex(final Forex derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForex(final Forex derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexSwap(final ForexSwap derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    return getException(derivative);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative, final DATA_TYPE data) {
    return getException(derivative, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigital(final ForexOptionDigital derivative) {
    return getException(derivative);
  }

  //-----     Commodity     -----

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalForward(final MetalForward future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFuture(final MetalFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOption(final MetalFutureOption future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureForward(final AgricultureForward future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFuture(final AgricultureFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOption(final AgricultureFutureOption future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyForward(final EnergyForward future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFuture(final EnergyFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOption(final EnergyFutureOption future) {
    return getException(future);
  }

  //  -----     Equity     -----

  @Override
  public RESULT_TYPE visitEquityFuture(final EquityFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityFuture(final EquityFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitIndexFuture(final IndexFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFuture(final EquityIndexFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future, final DATA_TYPE data) {
    return getException(future, data);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFuture(final VolatilityIndexFuture future) {
    return getException(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOption(final EquityIndexOption option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
    return getException(option);
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option, final DATA_TYPE data) {
    return getException(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityOption(final EquityOption option) {
    return getException(option);
  }

  //  -----     VarianceSwap     -----

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap, final DATA_TYPE data) {
    return getException(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitVarianceSwap(final VarianceSwap varianceSwap) {
    return getException(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final DATA_TYPE data) {
    return getException(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
    return getException(varianceSwap);
  }

  //  -----     Deprecated     -----

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx, final DATA_TYPE data) {
    return getException(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexForward(final ForexForward fx) {
    return getException(fx);
  }

  private RESULT_TYPE getException(final InstrumentDerivative definition, final DATA_TYPE data) {
    if (definition != null && data != null) {
      throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support derivatives of type " + definition.getClass().getSimpleName() + " with data of type "
          + data.getClass().getSimpleName());
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this method");
  }

  private RESULT_TYPE getException(final InstrumentDerivative definition) {
    if (definition != null) {
      throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support derivatives of type " + definition.getClass().getSimpleName());
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this method");
  }

}
