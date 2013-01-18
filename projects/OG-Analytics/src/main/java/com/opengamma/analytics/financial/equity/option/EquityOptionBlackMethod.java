/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Pricing method for vanilla Equity Index Option transactions with Black function.
 */
//TODO there is a lot of repeated code in this class and EquityOptionBlackMethod
//TODO the javadocs are out of date
public final class EquityOptionBlackMethod {

  // TODO What else?
  /**
   * Delta wrt Fwd
   * Delta wrt Strike (DualDelta)
   * Gamma (spot, fwd, strike)
   * Vega (wrt impliedVol surface)
   * Rates Delta (again, single rate, and curve)
   */

  private static final EquityOptionBlackMethod INSTANCE = new EquityOptionBlackMethod();

  /**
   * @return The static instance
   */
  public static EquityOptionBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private EquityOptionBlackMethod() {
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> price of an option using the Black formula. PV / ZeroBond(timeToSettlement) 
   */
  public double forwardPrice(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double notional = derivative.getUnitAmount();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdPrice = BlackFormulaRepository.price(forward, strike, expiry, blackVol, derivative.isCall());
    return notional * fwdPrice;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return Current DiscountBond or ZeroBond price for payment at the settlement date 
   */
  public double discountToSettlement(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double df = marketData.getDiscountCurve().getDiscountFactor(derivative.getTimeToSettlement());
    return df;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity 
   */
  public double forwardIndexValue(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    return forward;
  }

  /** 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>spot</b> value of the index, i.e. the current market value 
   */
  public double spotIndexValue(final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double spot = marketData.getForwardCurve().getSpot();
    return spot;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The present value of the option 
   */
  public double presentValue(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double fwdPrice = forwardPrice(derivative, marketData);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdPrice;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An YieldCurveBundle, which won't work
   * @return OpenGammaRuntimeException
   */
  public double presentValue(final EquityOption derivative, final YieldCurveBundle marketData) {
    ArgumentChecker.notNull(derivative, "The derivative, EquityOption, was null.");
    ArgumentChecker.notNull(marketData, "DataBundle was null. Expecting an EquityOptionDataBundle");
    throw new OpenGammaRuntimeException("EquityOptionBlackMethod requires a data bundle of type EquityOptionDataBundle. Found a YieldCurveBundle.");
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the discounting rate 
   */
  public double rho(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double ttm = derivative.getTimeToSettlement();
    final double pv = presentValue(derivative, marketData);
    return -ttm * pv;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward delta wrt the forward underlying, ie the sensitivity of the undiscounted price to the forward value of the underlying, d(PV/Z)/d(fwdUnderlying)
   */
  public double forwardDelta(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final boolean isCall = derivative.isCall();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double notional = derivative.getUnitAmount();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double undiscountDelta = notional * BlackFormulaRepository.delta(forward, strike, expiry, blackVol, isCall);
    return undiscountDelta;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the forward value of the underlying, d(PV)/d(fwdUnderlying) 
   */
  public double deltaWrtForward(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardDelta = forwardDelta(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardDelta * zeroBond;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot delta wrt the underlying, d(PV)/d(spotUnderlying) 
   */
  public double deltaWrtSpot(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double deltaWrtForward = deltaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return deltaWrtForward * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward gamma wrt the forward underlying, ie the 2nd order sensitivity of the undiscounted price to the forward value of the underlying,
   *          $\frac{\partial^2 (PV/Z)}{\partial F^2}$
   */
  public double forwardGamma(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double notional = derivative.getUnitAmount();
    final double forwardGamma = notional * BlackFormulaRepository.gamma(forward, strike, expiry, blackVol);
    return forwardGamma;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the forward delta wrt the forward value of the underlying, $\frac{\partial^2 (PV)}{\partial F^2}$ 
   */
  public double gammaWrtForward(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardGamma = forwardGamma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardGamma * zeroBond;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot gamma wrt the spot underlying, ie the 2nd order sensitivity of the present value to the spot value of the underlying,
   *          $\frac{\partial^2 (PV)}{\partial S^2}$
   */
  public double gammaWrtSpot(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double gammaWrtForward = gammaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return gammaWrtForward * dForwardDSpot * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The simple vega, d(PV)/d(blackVol) 
   */
  public double vega(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdVega = BlackFormulaRepository.vega(forward, strike, expiry, blackVol);
    final double df = discountToSettlement(derivative, marketData);
    final double notional = derivative.getUnitAmount();
    return df * fwdVega * notional;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The lognormal Black Volatility 
   */
  public double impliedVol(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    return blackVol;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward Vomma, ie the 2nd order sensitivity of the undiscounted price to the implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial \sigma^2}$
   */
  public double forwardVomma(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double notional = derivative.getUnitAmount();
    final double forwardVomma = BlackFormulaRepository.vomma(forward, strike, expiry, blackVol);
    return notional * forwardVomma;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot Vomma, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double vomma(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardVomma = forwardVomma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVomma * zeroBond;
  }

  /** 
   * Synonym for Vomma
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot Volga, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double spotVolga(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    return vomma(derivative, marketData);
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the undiscounted price to the forward and implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial F \partial \sigma}$
   */
  public double forwardVanna(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardVanna = BlackFormulaRepository.vanna(forward, strike, expiry, blackVol);
    final double notional = derivative.getUnitAmount();
    return notional * forwardVanna;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the present value to the forward and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial F \partial \sigma}$
   */
  public double vannaWrtForward(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardVanna = forwardVanna(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVanna * zeroBond;
  }

  /** 
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot vanna wrt the spot underlying, ie the 2nd order cross-sensitivity of the present value to the spot and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
   */
  public double vannaWrtSpot(final EquityOption derivative, final StaticReplicationDataBundle marketData) {
    final double vannaWrtForward = vannaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return vannaWrtForward * dForwardDSpot;
  }

}
