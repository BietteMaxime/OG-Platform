/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMASpecialMatrices;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.DOGMAMethodLiteral;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.Rosser;
import com.opengamma.maths.highlevelapi.datatypes.OGMatrix;

/**
 * Provides the Rosser matrix. A matrix designed to be particularly useful when testing symmetric eigenvalue routines.
 * It has largest eigenvalues of opposite signs, a ~zero to machine precision eigenvalue, a double eigenvalue, 
 * a small eigenvalue and a triple of eigenvalues that are different only at approx the 5th dp.
 */
@DOGMAMethodHook(provides = Rosser.class)
public class RosserFunction {

  
  private static double[] s_rosserData = new double[] {611, 196, -192, 407, -8, -52, -49, 29, 196, 899, 113, -192, -71, -43, -8, -44, -192, 113, 899, 196, 61, 49, 8, 52, 407, -192, 196, 611, 8, 44,
      59, -23, -8, -71, 61, 8, 411, -599, 208, 208, -52, -43, 49, 44, -599, 411, 208, 208, -49, -8, 8, 59, 208, 208, 99, -911, 29, -44, 52, -23, 208, 208, -911, 99 };//CSIGNORE

  /**
   * Returns the Rosser matrix
   * @return the Rosser matrix
   */
  @DOGMAMethodLiteral
  public static OGMatrix rosser() {
    return new OGMatrix(s_rosserData, 8, 8);
  }

}
