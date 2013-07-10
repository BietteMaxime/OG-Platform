/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.ctranspose;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Ctranspose;
import com.opengamma.maths.highlevelapi.datatypes.OGPermutationMatrix;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;

/**
 * Transpose for {@link OGPermutationMatrix}
 */
@DOGMAMethodHook(provides = Ctranspose.class)
public final class CtransposeOGPermutationMatrix implements Ctranspose<OGPermutationMatrix, OGPermutationMatrix> {

  @Override
  public OGPermutationMatrix eval(OGPermutationMatrix array1) {
    Catchers.catchNullFromArgList(array1, 1);
    // transpose of a permutation matrix is just looking up its own indices in itself a(i,j)=a(j,i) but we walk just once with compressed canonical vectors
    // so if you have permutation vector P, range=1:length(P), the range(P) gives the transpose permutation
    int[] data = array1.getIntData();
    int dim = array1.getNumberOfRows();

    int[] tmp = new int[dim];
    for (int i = 0; i < dim; i++) {
      tmp[data[i]] = i;
    }
    return new OGPermutationMatrix(tmp);
  }
}
