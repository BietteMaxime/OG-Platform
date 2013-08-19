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
  /** The convention scheme name string **/
  public static final String SCHEME_NAME = "CONVENTION";
  /** Overnight Index string **/
  public static final String OVERNIGHT = "Overnight";
  /** Ibor (interbank offered rate) index string **/
  public static final String IBOR = "Ibor";
  /** Libor (London interbank offered rate) index string **/
  public static final String LIBOR = "Libor";
  /** Jibar (Johannesburg interbank agreed rate) index string */
  public static final String JIBOR = "Jibar";
  /** Deposit convention string **/
  public static final String DEPOSIT = "Deposit";
  /** Deposit Overnight convention string **/
  public static final String DEPOSIT_ON = "DepositON";
  /** FRA convention string **/
  public static final String FRA = "FRA";
  /** OIS fixed leg convention string **/
  public static final String OIS_FIXED_LEG = "OIS Fixed Leg";
  /** OIS float leg convention string **/
  public static final String OIS_ON_LEG = "OIS Overnight Leg";
  /** IRS fixed leg convention string **/
  public static final String IRS_FIXED_LEG = "IRS Fixed Leg";
  /** IRS Ibor leg convention string **/
  public static final String IRS_IBOR_LEG = "IRS Ibor Leg";
  /** Quarterly Eurodollar futures string */
  public static final String EURODOLLAR_FUTURE = "Quarterly ED, 3M Libor";
  /** Fed fund futures string */
  public static final String FED_FUNDS_FUTURE = "Fed Funds Future";
  /** CME deliverable swap future string */
  public static final String CME_DELIVERABLE_SWAP_FUTURE = "CME Deliverable Swap Future";
  /** Inflation swap leg string */
  public static final String INFLATION_LEG = "Inflation Swap Leg";
  /** Price index string */
  public static final String PRICE_INDEX = "Price Index";

  public static ExternalIdBundle getIds(final Currency currency, final String instrumentName) {
    final String idName = getConventionName(currency, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static ExternalId getId(final Currency currency, final String instrumentName) {
    final String idName = getConventionName(currency, instrumentName);
    return simpleNameId(idName);
  }

  public static String getConventionName(final Currency currency, final String instrumentName) {
    return currency.getCode() + " " + instrumentName;
  }

  public static ExternalIdBundle getIds(final Currency currency, final String tenorString, final String instrumentName) {
    final String idName = getConventionName(currency, tenorString, instrumentName);
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static ExternalId getId(final Currency currency, final String tenorString, final String instrumentName) {
    final String idName = getConventionName(currency, tenorString, instrumentName);
    return simpleNameId(idName);
  }

  public static ExternalIdBundle getIds(final String idName) {
    return ExternalIdBundle.of(simpleNameId(idName));
  }

  public static String getConventionName(final Currency currency, final String tenorString, final String instrumentName) {
    return currency.getCode() + " " + tenorString + " " + instrumentName;
  }

  public static ExternalId simpleNameId(final String name) {
    return ExternalId.of(SCHEME_NAME, name);
  }

}
