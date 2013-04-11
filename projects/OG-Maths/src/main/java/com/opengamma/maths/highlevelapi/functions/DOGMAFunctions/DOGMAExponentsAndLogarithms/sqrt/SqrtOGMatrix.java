/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAExponentsAndLogarithms.sqrt;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sqrt;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArray;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGComplexMatrix;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.EasyIZY;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;
import com.opengamma.maths.lowlevelapi.functions.memory.DenseMemoryManipulation;

/**
 * does sqrt
 */
@DOGMAMethodHook(provides = Sqrt.class)
public final class SqrtOGMatrix implements Sqrt<OGArray<? extends Number>, OGMatrix> {

  @Override
  public OGArray<? extends Number> eval(OGMatrix array1) {
    Catchers.catchNullFromArgList(array1, 1);

    final int rowsArray1 = array1.getNumberOfRows();
    final int columnsArray1 = array1.getNumberOfColumns();
    final double[] dataArray1 = array1.getData();
    final int n = dataArray1.length;

    double[] sqrts = new double[n];
    
    // we are in real space so we can just carry out sqrt(abs(X)) and then shove it in the right place later if needs 
    // be opposed to parsing the data and then having to work out if a complex sqrt call should be made 
    EasyIZY.vd_abs(dataArray1, sqrts);
    EasyIZY.vd_sqrt(sqrts, sqrts);
    
    boolean isCmplx = false;
    int i;
    for (i = 0; i < n; i++) {
      if (dataArray1[i] < 0) {
        isCmplx = true;
        break;
      }
    }
    if (isCmplx) {
      double[] cmplxTmp;
      int twoj;
      cmplxTmp = DenseMemoryManipulation.convertSinglePointerToZeroInterleavedSinglePointer(sqrts);
      for (int j = i; j < n; j++) {
        twoj = 2 * j;
        if (dataArray1[j] < 0) { // move real value to complex
          cmplxTmp[twoj + 1] = cmplxTmp[twoj];
          cmplxTmp[twoj] = 0;
        }
      }
      return new OGComplexMatrix(cmplxTmp, rowsArray1, columnsArray1);
    } else {
      return new OGMatrix(sqrts, rowsArray1, columnsArray1);
    }

  }
}
