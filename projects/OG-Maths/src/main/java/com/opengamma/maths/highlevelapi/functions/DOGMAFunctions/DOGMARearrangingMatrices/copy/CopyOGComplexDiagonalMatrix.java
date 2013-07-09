/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMARearrangingMatrices.copy;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Copy;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGComplexDiagonalMatrix;

/**
 * Copies OGComplexDiagonalMatrix 
 */
@DOGMAMethodHook(provides = Copy.class)
public final class CopyOGComplexDiagonalMatrix implements Copy<OGComplexDiagonalMatrix, OGComplexDiagonalMatrix> {
  @Override
  public OGComplexDiagonalMatrix eval(OGComplexDiagonalMatrix array1) {
    final int n = array1.getData().length;
    final double[] tmp = new double[n];
    final int rows = array1.getNumberOfRows();
    final int cols = array1.getNumberOfColumns();
    System.arraycopy(array1.getData(), 0, tmp, 0, n);
    return new OGComplexDiagonalMatrix(tmp, rows, cols);
  }

}
