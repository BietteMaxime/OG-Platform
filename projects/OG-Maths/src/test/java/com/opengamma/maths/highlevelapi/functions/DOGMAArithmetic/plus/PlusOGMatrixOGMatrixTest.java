/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAArithmetic.plus;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionNonConformance;
import com.opengamma.maths.highlevelapi.datatypes.OGMatrix;
import com.opengamma.maths.highlevelapi.functions.DOGMAArithmetic.plus.PlusOGMatrixOGMatrix;
import com.opengamma.util.test.TestGroup;

/**
 * Test plus on OGMatrix/OGMatrix combo
 */
@Test(groups = TestGroup.UNIT)
public class PlusOGMatrixOGMatrixTest {
  static double[] _data4x3Scale1 = new double[] {1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12 };
  static double[] _data4x3Scale2 = new double[] {10, 40, 70, 100, 20, 50, 80, 110, 30, 60, 90, 120 };
  static OGMatrix F4x3Scale1 = new OGMatrix(_data4x3Scale1, 4, 3);
  static OGMatrix F4x3Scale2 = new OGMatrix(_data4x3Scale2, 4, 3);
  static OGMatrix F1x1Scale1 = new OGMatrix(1);
  static OGMatrix F1x1Scale2 = new OGMatrix(10);

  // null ptr etc is caught by the function pointer code

  private static PlusOGMatrixOGMatrix plus = new PlusOGMatrixOGMatrix();
  
  @Test
  public static void scalarFullPlusFull() {
    OGMatrix answer = new OGMatrix(new double[][] { {11.00, 12.00, 13.00 }, {14.00, 15.00, 16.00 }, {17.00, 18.00, 19.00 }, {20.00, 21.00, 22.00 } });
    assertTrue(answer.equals(plus.eval(F1x1Scale2, F4x3Scale1)));
  }

  @Test
  public static void FullPlusScalarFull() {
    OGMatrix answer = new OGMatrix(new double[][] { {11.00, 12.00, 13.00 }, {14.00, 15.00, 16.00 }, {17.00, 18.00, 19.00 }, {20.00, 21.00, 22.00 } });
    assertTrue(answer.equals(plus.eval(F4x3Scale1, F1x1Scale2)));
  }

  @Test(expectedExceptions = MathsExceptionNonConformance.class)
  public static void BadCommuteRows() {
    plus.eval(new OGMatrix(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3, 3), F4x3Scale1);
  }

  @Test(expectedExceptions = MathsExceptionNonConformance.class)
  public static void BadCommuteCols() {
    plus.eval(new OGMatrix(new double[] {1, 2, 3, 4, 5, 6, 7, 8}, 4, 2), F4x3Scale1);
  }

  @Test
  public static void FullPlusFull() {
    OGMatrix answer = new OGMatrix(new double[][] { {11.00, 22.00, 33.00 }, {44.00, 55.00, 66.00 }, {77.00, 88.00, 99.00 }, {110.00, 121.00, 132.00 } });
    assertTrue(answer.equals(plus.eval(F4x3Scale1, F4x3Scale2)));
  }


}
