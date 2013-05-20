/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class PerCurrencyConventionHelper {
  public static final String DEPOSIT = "Deposit";
  public static final String IBOR = "Ibor";
  public static final String FRA = "FRA";
  public static final String FIXED_SWAP_LEG = "Fixed Swap Leg";
  public static final String VANILLA_IBOR_LEG = "Vanilla Ibor Leg";
  public static final String ID_NAME = "Id name";

  public static ExternalIdBundle getIds(final Currency currency, final String instrumentName) {
    final String idName = getConventionName(currency, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static String getConventionName(final Currency currency, final String instrumentName) {
    return currency.getCode() + " " + instrumentName;
  }

  public static ExternalIdBundle getIds(final Currency currency, final String tenorString, final String instrumentName) {
    final String idName = getConventionName(currency, tenorString, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static String getConventionName(final Currency currency, final String tenorString, final String instrumentName) {
    return currency.getCode() + " " + tenorString + " " + instrumentName;
  }

  public static ExternalId simpleNameId(final String name) {
    return ExternalId.of(ID_NAME, name);
  }

}
