/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FIXED_SWAP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.VANILLA_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.InMemoryConventionMaster;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class USConventions {
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  private static final Currency USD = Currency.of("USD");

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String fixedSwapLegConventionName = getConventionName(USD, FIXED_SWAP_LEG);
    final String vanillaIborLegConventionName = getConventionName(USD, VANILLA_IBOR_LEG);
    final String tenorString = "3m";
    final String libor3mConventionName = getConventionName(USD, tenorString, IBOR);
    final ExternalId libor3mConventionId = InMemoryConventionBundleMaster.simpleNameSecurityId(libor3mConventionName);
    final Convention fixedLegConvention = new SwapFixedLegConvention(fixedSwapLegConventionName, getIds(USD, FIXED_SWAP_LEG),
        Tenor.THREE_MONTHS, THIRTY_360, MODIFIED_FOLLOWING, 2, false, USD, NYLON, StubType.NONE);
    final Convention vanillaIborLegConvention = new VanillaIborLegConvention(vanillaIborLegConventionName, getIds(USD, VANILLA_IBOR_LEG),
        libor3mConventionId, true, StubType.NONE, Interpolator1DFactory.LINEAR);
    conventionMaster.add(fixedLegConvention);
    conventionMaster.add(vanillaIborLegConvention);
    addDepositConventions(conventionMaster);
    addLiborConventions(conventionMaster);
  }

  private static void addDepositConventions(final InMemoryConventionMaster conventionMaster) {
    for (int i = 1; i < 3; i++) {
      final String tenorString = i + "d";
      final Tenor tenor = new Tenor(Period.ofDays(i));
      final String depositConventionName = getConventionName(USD, tenorString, DEPOSIT);
      final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(USD, tenorString, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 0, false, USD, US,
          tenor);
      conventionMaster.add(depositConvention);
    }
    for (int i = 1; i < 4; i++) {
      final String tenorString = i + "w";
      final Tenor tenor = new Tenor(Period.ofDays(i * 7));
      final String depositConventionName = getConventionName(USD, tenorString, DEPOSIT);
      final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(USD, tenorString, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, false, USD, US,
          tenor);
      conventionMaster.add(depositConvention);
    }
    for (int i = 1; i < 24; i++) {
      final String tenorString = i + "m";
      final Tenor tenor = new Tenor(Period.ofMonths(i));
      final String depositConventionName = getConventionName(USD, tenorString, DEPOSIT);
      final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(USD, tenorString, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, false, USD, US,
          tenor);
      conventionMaster.add(depositConvention);
    }
    for (int i = 1; i < 20; i++) {
      final String tenorString = i + "y";
      final Tenor tenor = new Tenor(Period.ofYears(i));
      final String depositConventionName = getConventionName(USD, tenorString, DEPOSIT);
      final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(USD, tenorString, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, false, USD, US,
          tenor);
      conventionMaster.add(depositConvention);
    }
  }

  private static void addLiborConventions(final InMemoryConventionMaster conventionMaster) {
    for (int i = 1; i < 4; i++) {
      final String tenorString = i + "w";
      final Tenor tenor = new Tenor(Period.ofDays(i * 7));
      final String liborConventionName = getConventionName(USD, tenorString, IBOR);
      final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(USD, tenorString, IBOR), ACT_360, MODIFIED_FOLLOWING, 2, false, USD,
          LocalTime.of(11, 00), NYLON, US, "", tenor);
      conventionMaster.add(liborConvention);
    }
    for (int i = 1; i < 13; i++) {
      final String tenorString = i + "m";
      final Tenor tenor = new Tenor(Period.ofMonths(i));
      final String liborConventionName = getConventionName(USD, tenorString, IBOR);
      final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(USD, tenorString, IBOR), ACT_360, MODIFIED_FOLLOWING, 2, false, USD,
          LocalTime.of(11, 00), NYLON, US, "", tenor);
      conventionMaster.add(liborConvention);
    }
  }
}
