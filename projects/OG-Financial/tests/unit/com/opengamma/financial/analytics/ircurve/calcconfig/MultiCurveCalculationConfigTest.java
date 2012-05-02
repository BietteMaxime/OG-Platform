/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class MultiCurveCalculationConfigTest extends FinancialTestBase {
  private static final String DEFAULT_USD_CONFIG_NAME = "Default";
  private static final String[] DEFAULT_USD_YIELD_CURVE_NAMES = new String[] {"FUNDING", "FORWARD_3M"};
  private static final UniqueIdentifiable[] DEFAULT_USD_IDS = new UniqueIdentifiable[] {Currency.USD, Currency.USD};
  private static final String[] DEFAULT_USD_CALCULATION_METHODS = new String[] {"PresentValue", "PresentValue"};
  private static final String EXTRA_USD_CONFIG_NAME = "Extra";
  private static final String[] EXTRA_USD_YIELD_CURVE_NAMES = new String[] {"FORWARD_6M", "FORWARD_12M", "FORWARD_1M"};
  private static final UniqueIdentifiable[] EXTRA_USD_IDS = new UniqueIdentifiable[] {Currency.USD, Currency.USD, Currency.USD};
  private static final String[] EXTRA_USD_CALCULATION_METHODS = new String[] {"ParRate", "ParRate", "ParRate"};
  private static final String DEFAULT_INR_CONFIG_NAME = "Default";
  private static final String[] DEFAULT_INR_YIELD_CURVE_NAMES = new String[] {"FUNDING"};
  private static final UniqueIdentifiable[] DEFAULT_INR_IDS = new UniqueIdentifiable[] {Currency.of("INR")};
  private static final String DEFAULT_INR_CALCULATION_METHOD = "FXImplied";
  private static final MultiCurveCalculationConfig DEFAULT_USD_CONFIG = new MultiCurveCalculationConfig(DEFAULT_USD_CONFIG_NAME, DEFAULT_USD_YIELD_CURVE_NAMES, DEFAULT_USD_IDS,
      DEFAULT_USD_CALCULATION_METHODS);
  private static final MultiCurveCalculationConfig EXTRA_USD_CONFIG = new MultiCurveCalculationConfig(EXTRA_USD_CONFIG_NAME, EXTRA_USD_YIELD_CURVE_NAMES, EXTRA_USD_IDS, EXTRA_USD_CALCULATION_METHODS,
      new String[] {DEFAULT_USD_CONFIG_NAME});
  private static final MultiCurveCalculationConfig DEFAULT_INR_CONFIG = new MultiCurveCalculationConfig(DEFAULT_INR_CONFIG_NAME, DEFAULT_INR_YIELD_CURVE_NAMES, DEFAULT_INR_IDS, DEFAULT_INR_CALCULATION_METHOD,
      new String[] {DEFAULT_USD_CONFIG_NAME});

  @Test
  public void test() {
    assertFalse(DEFAULT_USD_CONFIG.equals(EXTRA_USD_CONFIG));
    assertFalse(DEFAULT_USD_CONFIG.equals(DEFAULT_INR_CONFIG));
    final String name = "Extra";
    final String[] curveNames = new String[] {"FORWARD_6M", "FORWARD_12M", "FORWARD_1M"};
    final UniqueIdentifiable[] ids = new UniqueIdentifiable[] {Currency.USD, Currency.USD, Currency.USD};
    final String[] methods = new String[] {"ParRate", "ParRate", "ParRate"};
    final MultiCurveCalculationConfig config = new MultiCurveCalculationConfig(name, curveNames, ids, methods, new String[] {DEFAULT_USD_CONFIG_NAME});
    assertEquals(EXTRA_USD_CONFIG, config);
    ArrayAsserts.assertArrayEquals(config.getCalculationMethods(), methods);
    assertEquals(config.getCalculationConfigName(), name);
    ArrayAsserts.assertArrayEquals(config.getUniqueIds(), ids);
    ArrayAsserts.assertArrayEquals(config.getYieldCurveNames(), curveNames);
    ArrayAsserts.assertArrayEquals(config.getExogenousConfigNames(), new String[] {DEFAULT_USD_CONFIG_NAME});
  }

  @Test
  public void testCycle() {
    assertEquals(DEFAULT_USD_CONFIG, cycleObject(MultiCurveCalculationConfig.class, DEFAULT_USD_CONFIG));
    assertEquals(EXTRA_USD_CONFIG, cycleObject(MultiCurveCalculationConfig.class, EXTRA_USD_CONFIG));
    assertEquals(DEFAULT_INR_CONFIG, cycleObject(MultiCurveCalculationConfig.class, DEFAULT_INR_CONFIG));
  }
}
