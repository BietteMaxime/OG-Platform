/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation caplet/floorlet were the inflation figure are interpolated between monthly inflation figures.
 * The notional is positive for long the option and negative for short the option.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is [(Index_End / Index_Start - 1)-((1+strike)^T-1)]^{+} 
 **/
public class CapFloorInflationZeroCouponInterpolationDefinition extends CouponInflationDefinition implements CapFloor {

  /**
   * The fixing date (always the first of a month) of the last known fixing.
   */
  private final ZonedDateTime _lastKnownFixingDate;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference dates for the index at the coupon end. Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime[] _referenceEndDate;
  /**
   * The weight on the first month index in the interpolation.
   */
  private final double _weight;
  /**
   * The lag in month between the index validity and the coupon dates.
   */
  private final int _monthLag;

  /**
   * The cap/floor maturity in years.
   */

  private final int _maturity;

  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Constructor from all the cap/floor details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period; used for the payment.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param maturity The cap/floor maturity in years.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationZeroCouponInterpolationDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int monthLag, final int maturity,
      final ZonedDateTime lastKnownFixingDate, final double indexStartValue, final ZonedDateTime[] referenceEndDate,
      final double strike, final boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(lastKnownFixingDate, "Last known fixing date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    _lastKnownFixingDate = lastKnownFixingDate;
    _indexStartValue = indexStartValue;
    _referenceEndDate = referenceEndDate;
    _weight = 1.0 - (getPaymentDate().getDayOfMonth() - 1.0) / getPaymentDate().toLocalDate().lengthOfMonth();
    _monthLag = monthLag;
    _maturity = maturity;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder from all the cap/floor details.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate Coupon payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param maturity The cap/floor maturity in years.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationZeroCouponInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int monthLag, final int maturity, final ZonedDateTime lastKnownFixingDate, final double indexStartValue, final ZonedDateTime[] referenceEndDate,
      final double strike, final boolean isCap) {
    Validate.notNull(priceIndex, "Price index");
    return new CapFloorInflationZeroCouponInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0,
        notional, priceIndex, monthLag, maturity, lastKnownFixingDate, indexStartValue, referenceEndDate, strike, isCap);
  }

  /**
   * Builder from a zero-coupon inflation interpolation coupon the cap/floor strike and isCap flag.
   * @param couponInflation The underlying inflation coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param maturity The cap/floor maturity in years.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor
   */
  public static CapFloorInflationZeroCouponInterpolationDefinition from(final CouponInflationZeroCouponInterpolationDefinition couponInflation, final ZonedDateTime lastKnownFixingDate,
      final int maturity, final double strike, final boolean isCap) {
    Validate.notNull(couponInflation, "coupon Ibor");
    return new CapFloorInflationZeroCouponInterpolationDefinition(couponInflation.getCurrency(), couponInflation.getPaymentDate(), couponInflation.getAccrualStartDate(),
        couponInflation.getAccrualEndDate(), couponInflation.getPaymentYearFraction(), couponInflation.getNotional(), couponInflation.getPriceIndex(),
        couponInflation.getMonthLag(), maturity, lastKnownFixingDate, couponInflation.getIndexStartValue(), couponInflation.getReferenceEndDate(),
        strike, isCap);
  }

  public ZonedDateTime getLastKnownFixingDate() {
    return _lastKnownFixingDate;
  }

  public double getIndexStartValue() {
    return _indexStartValue;
  }

  public ZonedDateTime[] getReferenceEndDate() {
    return _referenceEndDate;
  }

  public double getWeight() {
    return _weight;
  }

  public int getMonthLag() {
    return _monthLag;
  }

  public int getMaturity() {
    return _maturity;
  }

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public CouponInflationDefinition with(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double notional) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - Math.pow(1 + _strike, _maturity)), 0);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, getLastKnownFixingDate());
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[1]);
    return new CapFloorInflationZeroCouponInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime, _indexStartValue, referenceEndTime,
        _maturity, _weight, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, getLastKnownFixingDate());
    final LocalDate dayConversion = date.toLocalDate();
    final String discountingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(getReferenceEndDate()[1]);

      if (fixedEndIndex1 != null) {
        final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(getReferenceEndDate()[0]);
        final Double fixedEndIndex = getWeight() * fixedEndIndex0 + (1 - getWeight()) * fixedEndIndex1;
        final Double fixedRate = (fixedEndIndex / getIndexStartValue() - 1.0);
        return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
      }
    }
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDate[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDate[1]);
    return new CapFloorInflationZeroCouponInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime, _indexStartValue, referenceEndTime,
        _maturity, _weight, _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationZeroCouponInterpolationDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationZeroCouponInterpolationDefinition(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", IsCap = " + _isCap + ", Strike = " + _strike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CapFloorInflationZeroCouponInterpolationDefinition other = (CapFloorInflationZeroCouponInterpolationDefinition) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
