/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMATrigonometry.sin;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sin;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGSparseMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.EasyIZY;
import com.opengamma.maths.lowlevelapi.functions.memory.OGTypesMalloc;

/**
 * Sin() on OGSparse
 */
@DOGMAMethodHook(provides = Sin.class)
public class SinOGSparseMatrix implements Sin<OGSparseMatrix, OGSparseMatrix> {

  @Override
  public OGSparseMatrix eval(OGSparseMatrix array1) {
    int n = array1.getData().length;
    double[] tmp = new double[n];
    EasyIZY.vd_sin(array1.getData(), tmp);
    return OGTypesMalloc.OGSparseMatrixBasedOnStructureOf(array1, tmp);
  }
}
