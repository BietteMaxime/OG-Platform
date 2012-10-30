/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.id.ExternalId;

public class CurveInstrumentProviderFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final CurveInstrumentProvider cip = new StaticCurveInstrumentProvider(ExternalId.of("JIM", "BO"));
    final CurveInstrumentProvider cip2 = new BloombergFutureCurveInstrumentProvider("ED", "Curncy");
    assertEquals(cip, cycleObject(CurveInstrumentProvider.class, cip));
    assertEquals(cip2, cycleObject(CurveInstrumentProvider.class, cip2));
  }

}
