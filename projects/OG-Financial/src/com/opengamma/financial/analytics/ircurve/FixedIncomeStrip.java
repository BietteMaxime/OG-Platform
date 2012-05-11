/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.fudgemsg.FixedIncomeStripFudgeBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * A fixed income strip. <b>Note that the futures are assumed to be quarterly.</b>
 */
public class FixedIncomeStrip implements Serializable, Comparable<FixedIncomeStrip> {

  private static final long serialVersionUID = 1L;

  private final StripInstrumentType _instrumentType;
  private final Tenor _curveNodePointTime;
  private final String _conventionName;
  private final int _nthFutureFromTenor;
  private final int _periodsPerYear;

  /**
   * Creates a strip for non-future, non-FRA and non-swap instruments.
   * 
   * @param instrumentType  the instrument type
   * @param curveNodePointTime  the time of the curve node point
   * @param conventionName  the name of the yield curve specification builder configuration
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final String conventionName) {
    ArgumentChecker.notNull(instrumentType, "InstrumentType");
    ArgumentChecker.isTrue(instrumentType != StripInstrumentType.FUTURE, "Cannot handle futures in this constructor");
    ArgumentChecker.isTrue(instrumentType != StripInstrumentType.PERIODIC_ZERO_DEPOSIT, "Cannot handle periodic zero deposits in this constructor");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _nthFutureFromTenor = 0;
    _periodsPerYear = 0;
    _conventionName = conventionName;
  }

  /**
   * Creates a future strip.
   * 
   * @param instrumentType  the instrument type
   * @param curveNodePointTime  the time of the curve node point
   * @param conventionName  the name of the convention to use to resolve the strip into a security
   * @param nthFutureFromTenor  how many futures to step through from the curveDate + the tenor. 1-based, must be >0.
   *   e.g. 3 (tenor = 1YR) => 3rd quarterly future after curveDate +  1YR.
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final int nthFutureFromTenor, final String conventionName) {
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.FUTURE, "Strip type for this constructor must be a future");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.isTrue(nthFutureFromTenor > 0, "Number of future must be greater than zero");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _nthFutureFromTenor = nthFutureFromTenor;
    _conventionName = conventionName;
    _periodsPerYear = 0;
  }

  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final int periodsPerYear, final boolean isPeriodicZeroDepositStrip,
      final String conventionName) {
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.PERIODIC_ZERO_DEPOSIT, "Strip type for this constructor must be a periodic zero deposit");
    ArgumentChecker.isTrue(isPeriodicZeroDepositStrip, "Must have flag indicating periodic zero deposit set to true");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.isTrue(periodsPerYear > 0, "Number of periods per year must be greater than zero");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _periodsPerYear = periodsPerYear;
    _nthFutureFromTenor = 0;
    _conventionName = conventionName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instrument type used to construct this strip.
   * 
   * @return the instrument type, not null
   */
  public StripInstrumentType getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Gets the curve node point in time.
   * 
   * @return a tenor representing the time of the curve node point, not null
   */
  public Tenor getCurveNodePointTime() {
    return _curveNodePointTime;
  }

  /**
   * Get the number of the quarterly IR futures after the tenor to choose.
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   * 
   * @return the number of futures after the tenor
   * @throws IllegalStateException if called on a non-future strip
   */
  public int getNumberOfFuturesAfterTenor() {
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot get number of futures after tenor for a non-future strip " + toString());
    }
    return _nthFutureFromTenor;
  }

  /**
   * Get the periods per year of a periodic zero deposit security
   * 
   * @return the number of periods per year
   * @throws IllegalStateException if called on a non-periodic zero deposit strip
   */
  public int getPeriodsPerYear() {
    if (_instrumentType != StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      throw new IllegalStateException("Cannot get number of periods per year for a non-periodic zero deposit strip " + toString());
    }
    return _periodsPerYear;
  }

  /**
   * Gets the name of the convention used to resolve this strip definition into a security.
   * 
   * @return the name, not null
   */
  public String getConventionName() {
    return _conventionName;
  }

  /**
   * Calculates the tenor of a strip. For all instruments except futures, this is the same as that entered on construction.
   * For futures, this is the start tenor + (3 * future number)
   * @return The effective tenor of the strip
   */
  public Tenor getEffectiveTenor() {
    return new Tenor(getInstrumentType() == StripInstrumentType.FUTURE ?
        getCurveNodePointTime().getPeriod().plusMonths(3 * getNumberOfFuturesAfterTenor()) : getCurveNodePointTime().getPeriod());
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(final FixedIncomeStrip other) {
    int result = getEffectiveTenor().getPeriod().toPeriodFields().toEstimatedDuration().compareTo(other.getEffectiveTenor().getPeriod().toPeriodFields().toEstimatedDuration());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - other.getInstrumentType().ordinal();
    if (result != 0) {
      return result;
    }
    if (getInstrumentType() == StripInstrumentType.FUTURE) {
      result = getNumberOfFuturesAfterTenor() - other.getNumberOfFuturesAfterTenor();
    }
    if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      result = getPeriodsPerYear() - other.getPeriodsPerYear();
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStrip) {
      final FixedIncomeStrip other = (FixedIncomeStrip) obj;
      final boolean result = ObjectUtils.equals(_curveNodePointTime, other._curveNodePointTime) && ObjectUtils.equals(_conventionName, other._conventionName)
          && _instrumentType == other._instrumentType;
      if (getInstrumentType() == StripInstrumentType.FUTURE) {
        return result && _nthFutureFromTenor == other._nthFutureFromTenor;
      }
      if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
        return result && _periodsPerYear == other._periodsPerYear;
      }
      return result;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _curveNodePointTime.hashCode();
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("FixedIncomeStrip[");
    sb.append("instrument type=");
    sb.append(getInstrumentType());
    sb.append(", ");
    sb.append("tenor=");
    sb.append(getCurveNodePointTime());
    sb.append(", ");
    if (getInstrumentType() == StripInstrumentType.FUTURE) {
      sb.append("future number after tenor=");
      sb.append(getNumberOfFuturesAfterTenor());
      sb.append(", ");
    }
    if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      sb.append("periods per year=");
      sb.append(getPeriodsPerYear());
      sb.append(", ");
    }
    sb.append("convention name=");
    sb.append(getConventionName());
    sb.append("]");
    return sb.toString();
  }

  //-------------------------------------------------------------------------
  // REVIEW: jim 22-Aug-2010 -- get rid of these and use the builder directly
  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    final FixedIncomeStripFudgeBuilder builder = new FixedIncomeStripFudgeBuilder();
    final MutableFudgeMsg container = builder.buildMessage(serializer, this);
    for (final FudgeField field : container.getAllFields()) {
      message.add(field);
    }
  }

  // REVIEW: jim 22-Aug-2010 -- get rid of these and use the builder directly
  public static FixedIncomeStrip fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FixedIncomeStripFudgeBuilder builder = new FixedIncomeStripFudgeBuilder();
    return builder.buildObject(deserializer, message);
  }

}
