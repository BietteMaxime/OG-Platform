/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.mtimes;

import java.util.Arrays;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Mtimes;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGSparseMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.BLAS;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;

/**
 * Does matrix * matrix in a mathematical sense
 */
@DOGMAMethodHook(provides = Mtimes.class)
public final class MtimesOGSparseMatrixOGSparseMatrix implements Mtimes<OGSparseMatrix, OGSparseMatrix, OGSparseMatrix> {
  private BLAS _localblas = new BLAS();

  @Override
  public OGSparseMatrix eval(OGSparseMatrix array1, OGSparseMatrix array2) {
    Catchers.catchNullFromArgList(array1, 1);
    Catchers.catchNullFromArgList(array2, 2);
    final int colsArray1 = array1.getNumberOfColumns();
    final int colsArray2 = array2.getNumberOfColumns();
    final int rowsArray1 = array1.getNumberOfRows();
    final int rowsArray2 = array2.getNumberOfRows();
    final double[] data1 = array1.getData();
    final double[] data2 = array2.getData();
    final int[] colPtr1 = array1.getColumnPtr();
    final int[] colPtr2 = array2.getColumnPtr();
    final int[] rowIdx1 = array1.getRowIndex();
    final int[] rowIdx2 = array2.getRowIndex();
    int ptr = 0;
    double[] tmp = null;
    int n = 0;
    OGSparseMatrix ret = null;

    if (colsArray1 == 1 && rowsArray1 == 1) { // We have scalar * sparse matrix
      final double deref = data1[0];
      n = data2.length;
      tmp = new double[n];
      System.arraycopy(data2, 0, tmp, 0, n);
      _localblas.dscal(n, deref, tmp, 1);
      ret = new OGSparseMatrix(colPtr2, rowIdx2, tmp, rowsArray2, colsArray2);
    } else if (colsArray2 == 1 && rowsArray2 == 1) { // We have sparse matrix * scalar
      final double deref = data2[0];
      n = data1.length;
      tmp = new double[n];
      System.arraycopy(data1, 0, tmp, 0, n);
      _localblas.dscal(n, deref, tmp, 1);
      ret = new OGSparseMatrix(colPtr1, rowIdx1, tmp, rowsArray1, colsArray1);
    } else {
      Catchers.catchBadCommute(colsArray1, "Columns in first array", rowsArray2, "Rows in second array");
      // TODO: refactor these calls into a SparseBLAS.

      // TODO: there is probably a better way of doing this, check papers
      // walk in col space
      double[] rhs = new double[rowsArray2];
      int[] newColPtr = new int[colsArray2 + 1];
      int[] newRowIdx = new int[data1.length * data2.length]; // max collisions, think so?
      double[] newData = new double[data1.length * data2.length]; // max collisions, think so?
      int newPtr = 0, array2ptr = 0;
      for (int icol = 0; icol < colsArray2; icol++) {
        newColPtr[icol] = newPtr;
        // broadcast col in array2 to vector
        Arrays.fill(rhs, 0);
        for (int i = colPtr2[icol]; i < colPtr2[icol + 1]; i++) {
          rhs[rowIdx2[array2ptr]] = data2[array2ptr];
          array2ptr++;
        }

        // sparse dgemv
        tmp = new double[rowsArray1];
        ptr = 0;
        for (int i = 0; i < colsArray1; i++) {
          for (int j = colPtr1[i]; j < colPtr1[i + 1]; j++) {
            tmp[rowIdx1[ptr]] += data1[ptr] * rhs[i];
            ptr++;
          }
        }

        // compress dgemv result and update pointers
        for (int i = 0; i < rowsArray1; i++) {
          if (tmp[i] != 0.e0) {
            newData[newPtr] = tmp[i];
            newRowIdx[newPtr] = i;
            newPtr++;
          }
        }

      }
      
      // branch so column vectors don't end up with a negative colptr index on the final element i.e. [0,-1] 
      if (colsArray2 > 1) {
        newColPtr[colsArray2] = newPtr - 1;
      } else {
        newColPtr[colsArray2] = newPtr; 
      }

      System.out.println("new col ptr=" + Arrays.toString(newColPtr));

      ret = new OGSparseMatrix(Arrays.copyOf(newColPtr, colsArray2 + 1), Arrays.copyOf(newRowIdx, newPtr), Arrays.copyOf(newData, newPtr), rowsArray1, colsArray2);
    }
    return ret;
  }
}
