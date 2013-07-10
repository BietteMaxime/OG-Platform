/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.dogma.engine.matrixinfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionConfigProblem;
import com.opengamma.maths.highlevelapi.datatypes.OGComplexDiagonalMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGComplexMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGComplexScalar;
import com.opengamma.maths.highlevelapi.datatypes.OGComplexSparseMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGDiagonalMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGIndexMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGPermutationMatrix;
import com.opengamma.maths.highlevelapi.datatypes.OGRealScalar;
import com.opengamma.maths.highlevelapi.datatypes.OGSparseMatrix;

/**
 * Provides a map between Matrix Classes and their address in jump tables
 */
public class MatrixTypeToIndexMap {

  private static MatrixTypeToIndexMap s_instance;

  MatrixTypeToIndexMap() {
  }

  public static MatrixTypeToIndexMap getInstance() {
    return s_instance;
  }

  private static Map<Class<?>, Integer> s_classToIntMap = new HashMap<Class<?>, Integer>();
  static {
    s_classToIntMap.put(OGRealScalar.class, 0);
    s_classToIntMap.put(OGComplexScalar.class, 1); // this is bad Complex should be a superset of Number then this mangle wouldn't have to happen
    s_classToIntMap.put(OGDiagonalMatrix.class, 2);
    s_classToIntMap.put(OGComplexDiagonalMatrix.class, 3);
    s_classToIntMap.put(OGPermutationMatrix.class, 4);
    s_classToIntMap.put(OGIndexMatrix.class, 5);
    s_classToIntMap.put(OGSparseMatrix.class, 6);
    s_classToIntMap.put(OGComplexSparseMatrix.class, 7);
    s_classToIntMap.put(OGMatrix.class, 8);
    s_classToIntMap.put(OGComplexMatrix.class, 9);
  }

  public static int getIndexFromClass(Class<?> array) {
    Integer ret = s_classToIntMap.get(array);
    if (ret != null) {
      return ret;
    } else {
      throw new MathsExceptionConfigProblem("Unknown maths type encountered. Offending class is : " + array.getCanonicalName() + "\nThis is usually due to a concrete implementation of a method " +
          "not having concrete maths types in it's argument list, which in turn means it can't be looked up and therefore can't be reached, hence the code stops!");
    }
  }

  public static Class<?> getClassFromIndex(int idx) {
    Set<Class<?>> keys = s_classToIntMap.keySet();
    Iterator<Class<?>> it = keys.iterator();
    while (it.hasNext()) {
      Class<?> next = it.next();
      if (s_classToIntMap.get(next) == idx) {
        return next;
      }
    }
    throw new MathsExceptionConfigProblem("Unknown index requested!");
  }
}
