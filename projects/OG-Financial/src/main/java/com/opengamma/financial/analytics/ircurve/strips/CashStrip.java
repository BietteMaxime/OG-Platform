/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Cash strip for use in curves containing sufficient information to construct a {@link CashSecurity}
 */
public class CashStrip {
  /** The start tenor of the strip */
  private final Period _startTenor;
  /** The maturity tenor of the strip */
  private final Period _maturityTenor;
  /** The convention to use in constructing a {@link CashSecurity} */
  private final String _conventionName;
  /** The curve specification name */
  private final String _curveSpecificationName;

  /**
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param conventionName The convention, not null
   * @param curveSpecificationName The name of the curve specification, not null
   */
  public CashStrip(final Period startTenor, final Period maturityTenor, final String conventionName, final String curveSpecificationName) {
    ArgumentChecker.notNull(startTenor, "start tenor");
    ArgumentChecker.notNull(maturityTenor, "maturity tenor");
    ArgumentChecker.notNull(conventionName, "convention name");
    ArgumentChecker.notNull(curveSpecificationName, "curve specification name");
    _startTenor = startTenor;
    _maturityTenor = maturityTenor;
    _conventionName = conventionName;
    _curveSpecificationName = curveSpecificationName;
  }

  /**
   * Gets the start tenor.
   * @return the start tenor
   */
  public Period getStartTenor() {
    return _startTenor;
  }

  /**
   * Gets the maturity tenor.
   * @return the maturity tenor
   */
  public Period getMaturityTenor() {
    return _maturityTenor;
  }

  /**
   * Gets the convention name.
   * @return the convention name
   */
  public String getConventionName() {
    return _conventionName;
  }

  /**
   * Gets the curve specification name.
   * @return the curve specification name
   */
  public String getCurveSpecificationName() {
    return _curveSpecificationName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _conventionName.hashCode();
    result = prime * result + _curveSpecificationName.hashCode();
    result = prime * result + _maturityTenor.hashCode();
    result = prime * result + _startTenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CashStrip)) {
      return false;
    }
    final CashStrip other = (CashStrip) obj;
    if (!ObjectUtils.equals(_maturityTenor, other._maturityTenor)) {
      return false;
    }
    if (!ObjectUtils.equals(_conventionName, other._conventionName)) {
      return false;
    }
    if (!ObjectUtils.equals(_startTenor, other._startTenor)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveSpecificationName, other._curveSpecificationName)) {
      return false;
    }
    return true;
  }


}
