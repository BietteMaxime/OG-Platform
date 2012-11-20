/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.money.Currency;

/**
 * Checks the wiring of the EquityFuturesPresentValueCalculator
 * FIXME Case - presentValue needs discounting.. 
 */
public class EquityFuturesPresentValueCalculatorTest {

  private static final EquityFuturesPresentValueCalculator PVC = EquityFuturesPresentValueCalculator.getInstance();

  @Test
  public void testEquityIndexDividendFuture() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final EquityIndexDividendFuture eidf = new EquityIndexDividendFuture(fixing, settlement, 95., Currency.JPY, 10);

    final double currentPrice = 100.0;
    SimpleFutureDataBundle dataBundle = new SimpleFutureDataBundle(null, currentPrice, null, null, null);
    double pv = PVC.visitEquityIndexDividendFuture(eidf, dataBundle);
    assertEquals(50.0, pv, 1e-12);

  }
}
