/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 */
public class InflationSensitivityTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 10), new DoublesPair(2, 20), new DoublesPair(3, 30), new DoublesPair(4, 40)});
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 40), new DoublesPair(2, 30), new DoublesPair(3, 20), new DoublesPair(4, 10)});
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {new DoublesPair(11, 40), new DoublesPair(12, 30), new DoublesPair(13, 20), new DoublesPair(14, 10)});
  private static final List<ForwardSensitivity> SENSI_FWD_1 = new ArrayList<ForwardSensitivity>();
  static {
    SENSI_FWD_1.add(new ForwardSensitivity(0.5, 0.75, 0.26, 11));
    SENSI_FWD_1.add(new ForwardSensitivity(0.75, 1.00, 0.26, 12));
    SENSI_FWD_1.add(new ForwardSensitivity(1.00, 1.25, 0.24, 13));
  }
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";

  private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc1() {
    InflationSensitivity.of(null, new HashMap<String, List<ForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd1() {
    InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice1() {
    InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), new HashMap<String, List<ForwardSensitivity>>(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc2() {
    InflationSensitivity.ofYieldDiscountingAndPrice(null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice2() {
    InflationSensitivity.ofYieldDiscountingAndPrice(new HashMap<String, List<DoublesPair>>(), null);
  }

  @Test
  public void of() {
    Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    Map<String, List<DoublesPair>> mapIn = new HashMap<String, List<DoublesPair>>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    InflationSensitivity of = InflationSensitivity.of(mapDsc, mapFwd, mapIn);
    assertEquals("InflationSensitivity: of", mapDsc, of.getYieldDiscountingSensitivities());
    assertEquals("InflationSensitivity: of", mapFwd, of.getForwardSensitivities());
    assertEquals("InflationSensitivity: of", mapIn, of.getPriceCurveSensitivities());

    InflationSensitivity ofDscIn = InflationSensitivity.ofYieldDiscountingAndPrice(mapDsc, mapIn);
    assertEquals("InflationSensitivity: of", mapDsc, ofDscIn.getYieldDiscountingSensitivities());
    assertEquals("InflationSensitivity: of", mapIn, ofDscIn.getPriceCurveSensitivities());
    AssertSensivityObjects.assertEquals("InflationSensitivity: of", InflationSensitivity.of(mapDsc, new HashMap<String, List<ForwardSensitivity>>(), mapIn), ofDscIn, TOLERANCE);

    InflationSensitivity ofFwd = InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), mapFwd, new HashMap<String, List<DoublesPair>>());
    InflationSensitivity constructor = new InflationSensitivity();
    constructor = constructor.plus(ofDscIn);
    constructor = constructor.plus(ofFwd).cleaned();
    AssertSensivityObjects.assertEquals("InflationSensitivity: of", constructor, of.cleaned(), TOLERANCE);
  }

  @Test
  public void plusMultipliedByDsc() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi33 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    InflationSensitivity pvSensi_11 = InflationSensitivity.ofYieldDiscounting(sensi11);
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    InflationSensitivity pvSensi_22 = InflationSensitivity.ofYieldDiscounting(sensi22);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    InflationSensitivity pvSensi_12 = InflationSensitivity.ofYieldDiscounting(sensi12);
    sensi33.put(CURVE_NAME_3, SENSI_DATA_3);
    InflationSensitivity pvSensi_33 = InflationSensitivity.ofYieldDiscounting(sensi33);
    // Simple add
    Map<String, List<DoublesPair>> expectedSensi11add22 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add22.put(CURVE_NAME_1, SENSI_DATA_1);
    expectedSensi11add22.put(CURVE_NAME_2, SENSI_DATA_2);
    assertEquals(expectedSensi11add22, pvSensi_11.plus(pvSensi_22).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi11add22), pvSensi_11.plus(pvSensi_22));
    // Multiply
    List<DoublesPair> sensiData1Multiply050 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 5.0), new DoublesPair(2, 10.0), new DoublesPair(3, 15.0), new DoublesPair(4, 20.0)});
    Map<String, List<DoublesPair>> expectedSensi1Multiply05 = new HashMap<String, List<DoublesPair>>();
    expectedSensi1Multiply05.put(CURVE_NAME_1, sensiData1Multiply050);
    assertEquals(expectedSensi1Multiply05, pvSensi_11.multipliedBy(0.5).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi1Multiply05), pvSensi_11.multipliedBy(0.5));
    // Add on the same curve
    List<DoublesPair> expectedSensiData1add2 = new ArrayList<DoublesPair>();
    expectedSensiData1add2.addAll(SENSI_DATA_1);
    expectedSensiData1add2.addAll(SENSI_DATA_2);
    Map<String, List<DoublesPair>> expectedSensi11add12 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add12.put(CURVE_NAME_1, expectedSensiData1add2);
    assertEquals(expectedSensi11add12, pvSensi_11.plus(pvSensi_12).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi11add12), pvSensi_11.plus(pvSensi_12));
    // Add multi-curve
    Map<String, List<DoublesPair>> expectedSensiAddMulti = new HashMap<String, List<DoublesPair>>();
    expectedSensiAddMulti.put(CURVE_NAME_1, expectedSensiData1add2);
    expectedSensiAddMulti.put(CURVE_NAME_2, SENSI_DATA_2);
    expectedSensiAddMulti.put(CURVE_NAME_3, SENSI_DATA_3);
    assertEquals(expectedSensiAddMulti, pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensiAddMulti), pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))));
  }

  @Test
  public void plusMultipliedByDscIn() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<String, List<DoublesPair>>();
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    InflationSensitivity pvSensiDscIn = InflationSensitivity.ofYieldDiscountingAndPrice(sensi11, sensi22);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscIn.plus(pvSensiDscIn).cleaned(), pvSensiDscIn.multipliedBy(2.0).cleaned(), TOLERANCE);
  }

  @Test
  public void cleaned() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    InflationSensitivity pvSensi_11 = InflationSensitivity.ofYieldDiscounting(sensi11);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    InflationSensitivity pvSensi_12 = InflationSensitivity.ofYieldDiscounting(sensi12);
    List<DoublesPair> expectedSensiDataClean12 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 50), new DoublesPair(2, 50), new DoublesPair(3, 50), new DoublesPair(4, 50)});
    Map<String, List<DoublesPair>> expectedSensiClean12 = new HashMap<String, List<DoublesPair>>();
    expectedSensiClean12.put(CURVE_NAME_1, expectedSensiDataClean12);
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensiClean12).getYieldDiscountingSensitivities(), pvSensi_11.plus(pvSensi_12).cleaned().getYieldDiscountingSensitivities());
  }

  @Test
  public void equalHash() {
    Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    Map<String, List<DoublesPair>> mapIn = new HashMap<String, List<DoublesPair>>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    InflationSensitivity cs = InflationSensitivity.of(mapDsc, mapFwd, mapIn);
    assertEquals("ParameterSensitivity: equalHash", cs, cs);
    assertEquals("ParameterSensitivity: equalHash", cs.hashCode(), cs.hashCode());
    assertFalse("ParameterSensitivity: equalHash", cs.equals(mapDsc));
    InflationSensitivity modified;
    modified = InflationSensitivity.of(mapDsc, mapFwd, mapDsc);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    modified = InflationSensitivity.of(mapIn, mapFwd, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    Map<String, List<ForwardSensitivity>> mapFwd2 = new HashMap<String, List<ForwardSensitivity>>();
    mapFwd2.put(CURVE_NAME_3, SENSI_FWD_1);
    modified = InflationSensitivity.of(mapDsc, mapFwd2, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
  }

}
