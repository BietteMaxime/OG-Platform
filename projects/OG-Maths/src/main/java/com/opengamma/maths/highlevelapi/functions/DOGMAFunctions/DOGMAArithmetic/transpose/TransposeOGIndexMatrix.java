/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.transpose;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Transpose;
import com.opengamma.maths.highlevelapi.datatypes.OGIndexMatrix;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;

/**
 * Transposes an OGIndexArray
 * TODO: At some point consider COW or at least state propagated permutations for things like this?!
 */
@DOGMAMethodHook(provides = Transpose.class)
public final class TransposeOGIndexMatrix implements Transpose<OGIndexMatrix, OGIndexMatrix> {

  @Override
  public OGIndexMatrix eval(OGIndexMatrix array1) {
    Catchers.catchNullFromArgList(array1, 1);
    int rowsArray1 = array1.getNumberOfRows();
    int columnsArray1 = array1.getNumberOfColumns();
    int retRows = columnsArray1, retCols = rowsArray1;
    int[] data = array1.getIntData();
    int[] tmp = new int[rowsArray1 * columnsArray1];

    int ir;
    for (int i = 0; i < columnsArray1; i++) {
      ir = i * rowsArray1;
      for (int j = 0; j < rowsArray1; j++) {
        tmp[j * columnsArray1 + i] = data[ir + j];
      }
    }
    return new OGIndexMatrix(tmp, retRows, retCols);
  }

}
