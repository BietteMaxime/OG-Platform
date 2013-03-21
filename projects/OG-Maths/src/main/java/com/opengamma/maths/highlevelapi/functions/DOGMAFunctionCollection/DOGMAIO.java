/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctionCollection;

import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArray;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAIO.SmartImport;
import com.opengamma.maths.highlevelapi.functions.DOGMAinterfaces.DOGMAIOAPI;

/**
 * Does IO for DOGMA
 */
public class DOGMAIO implements DOGMAIOAPI {

  private SmartImport _si = new SmartImport();

  /**
   * describes the orientation of a vector on import
   */
  public enum orientation {
    /** row vector */
    row,
    /** column vector */
    column
  }

  @Override
  public OGArray<Double> smartImport(double[][] aMatrix) {
    return _si.fromNativeArrayOfArrays(aMatrix);
  }

  @Override
  public OGArray<Double> smartImport(double[] aMatrix, orientation o) {
    return _si.fromNativeArray(aMatrix, o);
  }

  public OGArray<Double> smartImport(double[] aMatrix) {   
    return _si.fromNativeArray(aMatrix, orientation.column);
  }  
  
}
