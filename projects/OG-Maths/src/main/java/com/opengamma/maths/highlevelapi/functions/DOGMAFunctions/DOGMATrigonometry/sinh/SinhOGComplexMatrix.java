/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMATrigonometry.sinh;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sinh;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGComplexMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.EasyIZY;
import com.opengamma.maths.lowlevelapi.functions.memory.OGTypesMalloc;

/**
 * Sinh() of an OGComplexMatrix
 */
@DOGMAMethodHook(provides = Sinh.class)
public class SinhOGComplexMatrix implements Sinh<OGComplexMatrix, OGComplexMatrix> {

  @Override
  public OGComplexMatrix eval(OGComplexMatrix array1) {
    int n = array1.getData().length;
    double[] tmp = new double[n];
    EasyIZY.vz_sinh(array1.getData(), tmp);
    return OGTypesMalloc.OGComplexMatrixBasedOnStructureOf(array1, tmp);
  }
}
