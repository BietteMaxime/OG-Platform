/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.plus;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.fnlib.D1MACH;
import com.opengamma.maths.commonapi.exceptions.MathsExceptionNonConformance;
import com.opengamma.maths.dogma.DOGMA;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGSparseMatrix;
import com.opengamma.util.test.TestGroup;

/**
 * Test minus on OGSparseMatrix/OGSparseMatrix combo
 */
@Test(groups = TestGroup.UNIT)
public class PlusOGSparseMatrixOGSparseMatrixTest {
  static double[][] _data4x3Scale1 = new double[][] { {1., 0., 3. }, {0., 5., 0. }, {0., 0., 9. }, {10., 0., 0. } };
  static double[][] _data4x3Scale2 = new double[][] { {0., 50., 0. }, {0., 0., 90. }, {100., 0., 0. }, {10., 0., 30. } };
  static OGSparseMatrix F4x3Scale1 = new OGSparseMatrix(_data4x3Scale1);
  static OGSparseMatrix F4x3Scale2 = new OGSparseMatrix(_data4x3Scale2);
  static OGSparseMatrix F1x1Scale1 = new OGSparseMatrix(new double[][] {{1. } });
  static OGSparseMatrix F1x1Scale2 = new OGSparseMatrix(new double[][] {{10. } });

  // null ptr etc is caught by the function pointer code

  private static PlusOGSparseMatrixOGSparseMatrix plus = new PlusOGSparseMatrixOGSparseMatrix();

  @Test
  public static void ScalarPlusSparse() {
    OGMatrix answer = new OGMatrix(new double[][] { {11., 10., 13. }, {10., 15., 10. }, {10., 10., 19. }, {20., 10., 10. } });
    assertTrue(answer.fuzzyequals(plus.eval(F1x1Scale2, F4x3Scale1), 10 * D1MACH.four()));
  }

  @Test
  public static void SparsePlusScalar() {
    OGMatrix answer = new OGMatrix(new double[][] { {11., 10., 13. }, {10., 15., 10. }, {10., 10., 19. }, {20., 10., 10. } });
    assertTrue(answer.fuzzyequals(DOGMA.full(plus.eval(F4x3Scale1, F1x1Scale2)), 10 * D1MACH.four()));
  }

  @Test(expectedExceptions = MathsExceptionNonConformance.class)
  public static void BadCommuteRows() {
    plus.eval(new OGSparseMatrix(new double[][] { {1, 2, 0 }, {3, 4, 0 }, {5, 6, 1 }, {7, 8, 0 }, {9, 10, 0 } }), F4x3Scale1);
  }

  @Test(expectedExceptions = MathsExceptionNonConformance.class)
  public static void BadCommuteCols() {
    plus.eval(new OGSparseMatrix(new double[][] { {1, 2 }, {3, 4 }, {5, 6 }, {7, 8 } }), F4x3Scale1);
  }

  @Test
  public static void FullPlusFull() {
    OGMatrix answer = new OGMatrix(new double[][] { {1., 50., 3. }, {0., 5., 90. }, {100., 0., 9. }, {20., 0., 30. } });
    assertTrue(answer.fuzzyequals(DOGMA.full(plus.eval(F4x3Scale1, F4x3Scale2)), 10 * D1MACH.four()));
  }

}
