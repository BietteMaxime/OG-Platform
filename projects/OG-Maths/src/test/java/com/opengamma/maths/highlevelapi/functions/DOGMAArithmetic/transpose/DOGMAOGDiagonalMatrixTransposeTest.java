/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAArithmetic.transpose;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionNullPointer;
import com.opengamma.maths.highlevelapi.datatypes.OGDiagonalMatrix;
import com.opengamma.maths.highlevelapi.functions.DOGMAArithmetic.transpose.TransposeOGDiagonalMatrix;

/**
 * Tests OGDoubleArray transpose
 */
public class DOGMAOGDiagonalMatrixTransposeTest {

  TransposeOGDiagonalMatrix t = new TransposeOGDiagonalMatrix();

  int normalRows = 4;
  int normalCols = 3;
  double[] _data = new double[] {1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12 };
  OGDiagonalMatrix array1 = new OGDiagonalMatrix(_data, normalRows, normalCols);


  int transposedRows = 3;
  int transposedCols = 4;
  OGDiagonalMatrix array1tranposed = new OGDiagonalMatrix(_data, transposedRows, transposedCols);
  
  @Test(expectedExceptions = MathsExceptionNullPointer.class)
  public void nullInTest() {
    OGDiagonalMatrix tmp = null;
    t.eval(tmp);
  }

  @Test
  public void testTranspose() {
    assertTrue(array1tranposed.equals(t.eval(array1)));
  }

}
