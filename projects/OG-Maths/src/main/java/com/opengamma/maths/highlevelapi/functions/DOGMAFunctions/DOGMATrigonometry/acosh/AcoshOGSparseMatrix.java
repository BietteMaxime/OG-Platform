/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMATrigonometry.acosh;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Acosh;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArray;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGSparseMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.ComplexConstants;
import com.opengamma.maths.lowlevelapi.exposedapi.EasyIZY;
import com.opengamma.maths.lowlevelapi.functions.memory.DenseMemoryManipulation;
import com.opengamma.maths.lowlevelapi.functions.memory.SparseMemoryManipulation;

/**
 * Cosh() of an OGMatrix
 */
@DOGMAMethodHook(provides = Acosh.class)
public class AcoshOGSparseMatrix implements Acosh<OGArray<? extends Number>, OGSparseMatrix> {

  @Override
  public OGArray<? extends Number> eval(OGSparseMatrix array1) {
    double[] data = array1.getData();
    int n = data.length;
    double[] tmp;
    // check bounds
    boolean complex = false;
    for (int i = 0; i < n; i++) {
      if (data[i] < 1) {
        complex = true;
        break;
      }
    }
    OGArray<? extends Number> retarr;
    if (complex) {
      tmp = DenseMemoryManipulation.convertSinglePointerToZeroInterleavedSinglePointer(data);
      EasyIZY.vz_acosh(tmp, tmp);
      retarr = SparseMemoryManipulation.createFullComplexSparseMatrixWithNewFillValueInANDNewValuesBasedOnStructureOf(array1, tmp, ComplexConstants.i_times_half_pi());
    } else {
      tmp = new double[n];
      EasyIZY.vd_acosh(data, tmp);
      if (n == array1.getNumberOfNonZeroElements()) { // sparse, but fully populated
        retarr = SparseMemoryManipulation.createFullSparseMatrixWithNewFillValueInANDNewValuesBasedOnStructureOf(array1, tmp, 0);
      } else {
        tmp = DenseMemoryManipulation.convertSinglePointerToZeroInterleavedSinglePointer(tmp);
        retarr = SparseMemoryManipulation.createFullComplexSparseMatrixWithNewFillValueInANDNewValuesBasedOnStructureOf(array1, tmp, ComplexConstants.i_times_half_pi());
      }
    }
    return retarr;
  }
}
