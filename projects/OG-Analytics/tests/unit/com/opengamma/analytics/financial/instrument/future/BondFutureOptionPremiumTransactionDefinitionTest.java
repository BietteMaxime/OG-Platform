/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.time.DateUtils;

public class BondFutureOptionPremiumTransactionDefinitionTest {

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final BondFutureDefinition FVU1_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  // Option security
  private static final double STRIKE = 1.20;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 8, 26);
  private static final BondFutureOptionPremiumSecurityDefinition FVU1_C120_SEC_DEFINITION = new BondFutureOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  // Option transaction
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2011, 6, 17);
  private static final double TRANSACTION_PRICE = 62.5 / 64d; // Prices for options quoted in 1/64.
  private static final int QUANTITY = -123;
  private static final double PREMIUM_AMOUNT = TRANSACTION_PRICE * QUANTITY * FVU1_C120_SEC_DEFINITION.getNotional();
  private static final BondFutureOptionPremiumTransactionDefinition FVU1_C120_TR_DEFINITION = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE,
      PREMIUM_AMOUNT);

  private static final String[] CURVE_NAMES = FutureInstrumentsDescriptionDataSet.curveNames();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new BondFutureOptionPremiumTransactionDefinition(null, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDate() {
    new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, null, PREMIUM_AMOUNT);
  }

  @Test
  public void fromTradePrice() {
    BondFutureOptionPremiumTransactionDefinition from = BondFutureOptionPremiumTransactionDefinition.fromTradePrice(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE, TRANSACTION_PRICE);
    assertEquals("Bond future option premium transaction definition", from.getPremium().getAmount(), PREMIUM_AMOUNT);
    assertEquals("Bond future option premium transaction definition", from, FVU1_C120_TR_DEFINITION);
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FVU1_C120_TR_DEFINITION.equals(FVU1_C120_TR_DEFINITION));
    BondFutureOptionPremiumTransactionDefinition other = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);
    assertTrue(FVU1_C120_TR_DEFINITION.equals(other));
    assertTrue(FVU1_C120_TR_DEFINITION.hashCode() == other.hashCode());
    BondFutureOptionPremiumTransactionDefinition modified;
    BondFutureOptionPremiumSecurityDefinition modifiedSec = new BondFutureOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE + 0.01, IS_CALL);
    modified = new BondFutureOptionPremiumTransactionDefinition(modifiedSec, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY + 1, PREMIUM_DATE, PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE.plusDays(1), PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT + 10.0);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    assertFalse(FVU1_C120_TR_DEFINITION.equals(EXPIRATION_DATE));
    assertFalse(FVU1_C120_TR_DEFINITION.equals(null));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeBeforeSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 16);
    BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate, CURVE_NAMES[1]);
    BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate, CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeOnSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 17);
    BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate, CURVE_NAMES[1]);
    BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate, CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 20);
    BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    PaymentFixed premium = new PaymentFixed(FVU1_C120_TR_DEFINITION.getCurrency(), 0.0, 0.0, CURVE_NAMES[1]);
    BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate, CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

}
