/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.datatypes.primitive;

/**
 * 
 * @param <T> a Number type
 */
public interface OGArrayInterface<T extends Number> {

  int getNumberOfRows();

  int getNumberOfColumns();

  T getEntry(int... indices);

  double[] getData();

}
