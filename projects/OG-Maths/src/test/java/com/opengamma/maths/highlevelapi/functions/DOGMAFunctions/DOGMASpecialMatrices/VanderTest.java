/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices;

import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionIllegalArgument;
import com.opengamma.maths.commonapi.exceptions.MathsExceptionNullPointer;
import com.opengamma.maths.highlevelapi.datatypes.OGMatrix;
import com.opengamma.maths.lowlevelapi.functions.D1mach;

/**
 * Tests Vandermonde matrix generation 
 */
public class VanderTest {
  private static double[] _data3 = new double[] {2, 3, 4 };
  private static OGMatrix tmp3 = new OGMatrix(_data3, 3, 1);
  private static double[] answer3_pick1 = new double[] {1, 1, 1 };
  private static double[] answer3_pick2 = new double[] {2, 3, 4, 1, 1, 1 };
  private static double[] answer3_pick3 = new double[] {4, 9, 16, 2, 3, 4, 1, 1, 1 };
  private static double[] answer3_pick4 = new double[] {8, 27, 64, 4, 9, 16, 2, 3, 4, 1, 1, 1 };
  private static double[] answer3_pick10 = new double[] {512, 19683, 262144, 256, 6561, 65536, 128, 2187, 16384, 64, 729, 4096, 32, 243, 1024, 16, 81, 256, 8, 27, 64, 4, 9, 16, 2, 3, 4, 1, 1, 1 };

  
  private static double[] _data5 = new double[] {1, 2, 3, 5, 8 };
  private static OGMatrix tmp5 = new OGMatrix(_data5, 1, 5);  
  private static double[] answer5_pick6 = new double[]{1,32,243,3125,32768,1,16,81,625,4096,1,8,27,125,512,1,4,9,25,64,1,2,3,5,8,1,1,1,1,1}; 
  
  @Test(expectedExceptions = MathsExceptionNullPointer.class)
  public static void nullMatrix() {
    OGMatrix tmp = null;
    VanderFunction.vander(tmp, 1);
  }

  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public static void okMatrixBadPick() {
    VanderFunction.vander(tmp3, 0);
  }
  
  @Test(expectedExceptions = MathsExceptionIllegalArgument.class)
  public static void badMatrixOKPick() {
    VanderFunction.vander(new OGMatrix(new double[][] {{1,2},{3,4}}) , 1);
  }
  
  @Test
  public static void simple3_pick1Test() {
    int n = 1;
    OGMatrix tmp = VanderFunction.vander(tmp3, n);
    assertArrayEquals(tmp.getData(), answer3_pick1, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  @Test
  public static void simple3_pick2Test() {
    int n = 2;
    OGMatrix tmp = VanderFunction.vander(tmp3, n);
    assertArrayEquals(tmp.getData(), answer3_pick2, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  @Test
  public static void simple3_pick3Test() {
    int n = 3;
    OGMatrix tmp = VanderFunction.vander(tmp3, n);
    assertArrayEquals(tmp.getData(), answer3_pick3, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  @Test
  public static void simple3_pick4Test() {
    int n = 4;
    OGMatrix tmp = VanderFunction.vander(tmp3, n);
    assertArrayEquals(tmp.getData(), answer3_pick4, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  @Test
  public static void simple3_pick10Test() {
    int n = 10;
    OGMatrix tmp = VanderFunction.vander(tmp3, n);
    assertArrayEquals(tmp.getData(), answer3_pick10, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  @Test
  public static void simple5_pick6Test() {
    int n = 6;
    OGMatrix tmp = VanderFunction.vander(tmp5, n);
    assertArrayEquals(tmp.getData(), answer5_pick6, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 5);
  }  
  
  // test object clatter through from different interface
 
  @Test
  public static void simple3_pickBasedonVectorTest() {
    int n = 3;
    OGMatrix tmp = VanderFunction.vander(tmp3);
    assertArrayEquals(tmp.getData(), answer3_pick3, 10 * D1mach.four());
    assertTrue(tmp.getNumberOfColumns() == n);
    assertTrue(tmp.getNumberOfRows() == 3);
  }

  
}
