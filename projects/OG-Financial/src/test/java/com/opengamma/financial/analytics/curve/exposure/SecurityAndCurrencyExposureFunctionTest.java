/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class SecurityAndCurrencyExposureFunctionTest {
  private static final ExposureFunction EXPOSURE_FUNCTION = new SecurityAndCurrencyExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));

  @Test
  public void testAgriculturalFutureSecurity() {
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testBondFutureSecurity() {
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testCashSecurity() {
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final List<ExternalId> ids = cash.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "CASH_USD"), ids.get(0));
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final List<ExternalId> ids = security.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "CAP-FLOOR CMS SPREAD_EUR"), ids.get(0));
  }

  @Test
  public void testCapFloorSecurity() {
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final List<ExternalId> ids = security.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "CAP-FLOOR_USD"), ids.get(0));
  }

  @Test
  public void testContinuousZeroDepositSecurity() {
    final ContinuousZeroDepositSecurity security = ExposureFunctionTestHelper.getContinuousZeroDepositSecurity();
    final List<ExternalId> ids = security.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "CONTINUOUS_ZERO_DEPOSIT_EUR"), ids.get(0));
  }

  @Test
  public void testCorporateBondSecurity() {
    final CorporateBondSecurity security = ExposureFunctionTestHelper.getCorporateBondSecurity();
    final List<ExternalId> ids = security.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "BOND_USD"), ids.get(0));
  }

  @Test
  public void testEnergyFutureSecurity() {
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testEquityFutureSecurity() {
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testFRASecurity() {
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final List<ExternalId> ids = fra.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FRA_USD"), ids.get(0));
  }

  @Test
  public void testFXFutureSecurity() {
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testIndexFutureSecurity() {
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testStockFutureSecurity() {
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final List<ExternalId> ids = future.accept(EXPOSURE_FUNCTION);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ExposureFunction.SECURITY_IDENTIFIER, "FUTURE_USD"), ids.get(0));
  }
}
