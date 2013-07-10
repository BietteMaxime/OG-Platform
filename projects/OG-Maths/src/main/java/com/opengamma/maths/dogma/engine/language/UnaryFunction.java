/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.dogma.engine.language;

import com.opengamma.maths.highlevelapi.datatypes.OGArray;

/**
 * @param <R> ret
 * @param <S> arg1
 */
public interface UnaryFunction<R extends OGArray<? extends Number>, S extends OGArray<? extends Number>> extends Function {

  /**
   * @param array1 first
   * @return ret
   */
  R eval(S array1);
}
