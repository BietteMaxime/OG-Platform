/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.RealPolynomialFunction1D;

/**
 * 
 * @param <T> Type of the polynomial function
 */
public interface Polynomial1DRootFinder<T> {

  T[] getRoots(RealPolynomialFunction1D function);
}
