/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.exposedapi.LAPACKBacking;

import com.opengamma.maths.nativewrappers.OGLAPACKRawWrapper;

/**
 * Native library backed LAPACK
 * TODO: enumerate the char pointers
 */
public class LAPACKNativeBacked extends LAPACKAbstractSuper implements LAPACKAPIInterface {

  @Override
  public void dgesvd(char jobu, char jobvt, int m, int n, double[] A, int lda, double[] S, double[] U, int ldu, double[] VT, int ldvt, double[] WORK, int lwork, int[] info) { // CSIGNORE
    OGLAPACKRawWrapper.dgesvd(new char[] {jobu }, new char[] {jobvt }, new int[] {m }, new int[] {n }, A, new int[] {lda }, S, U, new int[] {ldu }, VT, new int[] {ldvt }, WORK, new int[] {lwork },
        info);
  }

  @Override
  public void zgesvd(char jobu, char jobvt, int m, int n, double[] A, int lda, double[] S, double[] U, int ldu, double[] VT, int ldvt, double[] WORK, int lwork, double[] RWORK, int[] info) { // CSIGNORE
    OGLAPACKRawWrapper.zgesvd(new char[] {jobu }, new char[] {jobvt }, new int[] {m }, new int[] {n }, A, new int[] {lda }, S, U, new int[] {ldu }, VT, new int[] {ldvt }, WORK, new int[] {lwork },
        RWORK, info);
  }

  @Override
  public void dgetrf(int m, int n, double[] A, int lda, int[] ipiv, int[] info) {//CSIGNORE
    OGLAPACKRawWrapper.dgetrf(new int[] {m }, new int[] {n }, A, new int[] {lda }, ipiv, info);
  }

  @Override
  public void dgetrs(char trans, int n, int nrhs, double[] a, int lda, int[] ipiv, double[] b, int ldb, int[] info) {
    OGLAPACKRawWrapper.dgetrs(new char[] {trans }, new int[] {n }, new int[] {nrhs }, a, new int[] {lda }, ipiv, b, new int[] {ldb }, info);
  }

  @Override
  public void dgelsd(int m, int n, int nrhs, double[] A, int lda, double[] b, int ldb, double[] s, double rcond, int[] rank, double[] work, int lwork, int[] iwork, int[] info) { // CSIGNORE
    OGLAPACKRawWrapper.dgelsd(new int[] {m }, new int[] {n }, new int[] {nrhs }, A, new int[] {lda }, b, new int[] {ldb }, s, new double[] {rcond }, rank, work, new int[] {lwork }, iwork, info);
  }

  @Override
  public void dtrtrs(char uplo, char trans, char diag, int n, int nrhs, double[] a, int lda, double[] b, int ldb, int[] info) {
    OGLAPACKRawWrapper.dtrtrs(new char[] {uplo }, new char[] {trans }, new char[] {diag }, new int[] {n }, new int[] {nrhs }, a, new int[] {lda }, b, new int[] {ldb }, info);
  }

  @Override
  public void dpotrf(char uplo, int n, double[] a, int lda, int[] info) {
    OGLAPACKRawWrapper.dpotrf(new char[] {uplo }, new int[] {n }, a, new int[] {lda }, info);
  }

  @Override
  public void dpotrs(char uplo, int n, int nrhs, double[] a, int lda, double[] b, int ldb, int[] info) {
    OGLAPACKRawWrapper.dpotrs(new char[] {uplo }, new int[] {n }, new int[] {nrhs }, a, new int[] {lda }, b, new int[] {ldb }, info);
  }

  @Override
  public int ilaenv(int ispec, char[] name, char[] opts, int n1, int n2, int n3, int n4) {
    return OGLAPACKRawWrapper.ilaenv(new int[] {ispec }, name, opts, new int[] {n1 }, new int[] {n2 }, new int[] {n3 }, new int[] {n4 });
  }

  @Override
  public void dgeev(char jobvl, char jobvr, int n, double[] a, int lda, double[] wr, double[] wi, double[] vl, int ldvl, double[] vr, int ldvr, double[] work, int lwork, int[] info) {
    OGLAPACKRawWrapper.dgeev(new char[] {jobvl }, new char[] {jobvr }, new int[] {n }, a, new int[] {lda }, wr, wi, vl, new int[] {ldvl }, vr, new int[] {ldvr }, work, new int[] {lwork }, info);
  }

  @Override
  public void dlansy(char norm, char uplo, int n, double[] ap, int lda, double[] work) {
    OGLAPACKRawWrapper.dlansy(new char[] {norm }, new char[] {uplo }, new int[] {n }, ap, new int[] {lda }, work);
  }

  @Override
  public void dlantr(char norm, char uplo, char diag, int m, int n, double[] a, int lda, double[] work) {
    OGLAPACKRawWrapper.dlantr(new char[] {norm }, new char[] {uplo }, new char[] {diag }, new int[] {m }, new int[] {n }, a, new int[] {lda }, work);
  }

  @Override
  public void dlange(char norm, int m, int n, double[] a, int lda, double[] work) {
    OGLAPACKRawWrapper.dlange(new char[] {norm }, new int[] {m }, new int[] {n }, a, new int[] {lda }, work);
  }

  @Override
  public void dpocon(char uplo, int n, double[] a, int lda, double anorm, double[] rcond, double[] work, int[] iwork, int[] info) {
    OGLAPACKRawWrapper.dpocon(new char[] {uplo }, new int[] {n }, a, new int[] {lda }, new double[] {anorm }, rcond, work, iwork, info);
  }

  @Override
  public void dtrcon(char norm, char uplo, char diag, int n, double[] a, int lda, double[] rcond, double[] work, int[] iwork, int[] info) {
    OGLAPACKRawWrapper.dtrcon(new char[] {norm }, new char[] {uplo }, new char[] {diag }, new int[] {n }, a, new int[] {lda }, rcond, work, iwork, info);
  }

  @Override
  public void dgecon(char norm, int n, double[] a, int lda, double anorm, double[] rcond, double[] work, int[] iwork, int[] info) {
    OGLAPACKRawWrapper.dgecon(new char[] {norm }, new int[] {n }, a, new int[] {lda }, new double[] {anorm }, rcond, work, iwork, info);
  }

}
