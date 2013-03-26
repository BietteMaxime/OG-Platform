/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Simple argument checker throwing {@code IllegalArgumentException}.
 */
public class TimeSeriesUtils {

  /**
   * An empty array.
   */
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  /**
   * An empty array.
   */
  public static final long[] EMPTY_LONG_ARRAY = new long[0];
  /**
   * An empty array.
   */
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
  /**
   * An empty array.
   */
  public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
  /**
   * An empty array.
   */
  public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
  /**
   * An empty array.
   */
  public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code Integer} array to an {@code int} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   * @throws NullPointerException if array contains null
   */
  public static int[] toPrimitive(Integer[] array) {
    if (array.length == 0) {
      return EMPTY_INT_ARRAY;
    }
    int[] result = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  /**
   * Converts a {@code Long} array to a {@code long} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   * @throws NullPointerException if array contains null
   */
  public static long[] toPrimitive(Long[] array) {
    if (array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  /**
   * Converts a {@code Double} array to a {@code double} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   * @throws NullPointerException if array contains null
   */
  public static double[] toPrimitive(Double[] array) {
    if (array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code int} array to a {@code Integer} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   */
  public static Integer[] toObject(int[] array) {
    if (array.length == 0) {
      return EMPTY_INTEGER_OBJECT_ARRAY;
    }
    Integer[] result = new Integer[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  /**
   * Converts a {@code long} array to a {@code Long} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   */
  public static Long[] toObject(long[] array) {
    if (array.length == 0) {
      return EMPTY_LONG_OBJECT_ARRAY;
    }
    Long[] result = new Long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  /**
   * Converts a {@code double} array to a {@code Double} array.
   *
   * @param array  the input array, not null
   * @return the output array, not null
   */
  public static Double[] toObject(double[] array) {
    if (array.length == 0) {
      return EMPTY_DOUBLE_OBJECT_ARRAY;
    }
    Double[] result = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Compare two doubles to see if they're 'closely' equal.
   * <p>
   * This handles rounding errors which can mean the results of double precision computations
   * lead to small differences in results.
   * The definition 'close' is that the difference is less than 10^-15 (1E-15).
   * If a different maximum allowed difference is required, use the other version of this method.
   * 
   * @param a  the first value
   * @param b  the second value
   * @return true, if a and b are equal to within 10^-15, false otherwise
   */
  public static boolean closeEquals(double a, double b) {
    if (Double.isInfinite(a)) {
      return (a == b);
    }
    return (Math.abs(a - b) < 1E-15);
  }

}
