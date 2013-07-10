/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.dogma.engine.converters.convertOGMatrix;

import com.opengamma.maths.dogma.engine.DOGMAConverterHook;
import com.opengamma.maths.dogma.engine.converters.Converter;
import com.opengamma.maths.highlevelapi.datatypes.OGArray;
import com.opengamma.maths.highlevelapi.datatypes.OGMatrix;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASparseUtilities.sparse.SparseOGMatrix;

/**
 * 
 */
@DOGMAConverterHook(provides = Converter.class)
public final class ConvertOGMatrixToOGSparseMatrix implements Converter<OGMatrix> {
  private static SparseOGMatrix s_sparse = new SparseOGMatrix();

  @Override
  public OGArray<? extends Number> convert(OGMatrix array1) {
    return s_sparse.eval(array1);
  }
}
