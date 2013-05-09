/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.ForwardTickerConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SpotTickerConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ConventionBuildersTest extends AnalyticsTestBase {

  @Test
  public void testCMSLegConvention() {
    final CMSLegConvention convention = new CMSLegConvention("EUR CMS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR CMS")),
        ExternalId.of("Test", "EUR 6m Swap Index"), Tenor.SIX_MONTHS, true);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(convention, cycleObject(CMSLegConvention.class, convention));
  }

  @Test
  public void testCompoundingIborLegConvention() {
    final CompoundingIborLegConvention convention = new CompoundingIborLegConvention("EUR CMS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR CMS")),
        ExternalId.of("Test", "EUR 6m Swap Index"), Tenor.SIX_MONTHS, CompoundingType.FLAT_COMPOUNDING);
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEquals(convention, cycleObject(CompoundingIborLegConvention.class, convention));
  }

  @Test
  public void testDepositConvention() {
    final DepositConvention convention = new DepositConvention("EUR Deposit", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Deposit")),
        DayCountFactory.INSTANCE.getDayCount("Act/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), 2, true,
        Currency.EUR, ExternalId.of("Test", "EU"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(DepositConvention.class, convention));
  }

  @Test
  public void testFXForwardAndSwapConvention() {
    final FXForwardAndSwapConvention convention = new FXForwardAndSwapConvention("USD/CAD", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("USD/CAD")),
        ExternalId.of("Test", "FX"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true, ExternalId.of("Test", "US"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(FXForwardAndSwapConvention.class, convention));
  }

  @Test
  public void testFXSpotConvention() {
    final FXSpotConvention convention = new FXSpotConvention("USD/CAD", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("USD/CAD")),
        1, ExternalId.of("Test", "US"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(FXSpotConvention.class, convention));
  }

  @Test
  public void testIborIndexConvention() {
    final IborIndexConvention convention = new IborIndexConvention("EUR Deposit", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Deposit")),
        DayCountFactory.INSTANCE.getDayCount("Act/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), 2, true,
        Currency.EUR, LocalTime.of(11, 0), ExternalId.of("Test", "EU"), ExternalId.of("Test", "EU"), "Page");
    convention.setUniqueId(UniqueId.of("Test", "1234567"));
    assertEquals(convention, cycleObject(IborIndexConvention.class, convention));
  }

  @Test
  public void testInterestRateFutureConvention() {
    final InterestRateFutureConvention convention = new InterestRateFutureConvention("ER", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("ER")),
        ExternalId.of("Test", "3rd Wednesday"), ExternalId.of("Test", "EUX"), ExternalId.of("Test", "3m Euribor"));
    convention.setUniqueId(UniqueId.of("Test", "123456"));
    assertEquals(convention, cycleObject(InterestRateFutureConvention.class, convention));
  }

  @Test
  public void testOISLegConvention() {
    final OISLegConvention convention = new OISLegConvention("EUR OIS", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR OIS")),
        ExternalId.of("Test", "EONIA"), Tenor.SIX_MONTHS, 0);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(convention, cycleObject(OISLegConvention.class, convention));
  }

  @Test
  public void testOvernightIndexConvention() {
    final OvernightIndexConvention convention = new OvernightIndexConvention("EONIA", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EONIA")),
        DayCountFactory.INSTANCE.getDayCount("Act/360"), 2, Currency.EUR, ExternalId.of("Test", "EU"));
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(OvernightIndexConvention.class, convention));
  }

  @Test
  public void testSwapConvention() {
    final SwapConvention convention = new SwapConvention("EUR Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Swap")),
        ExternalId.of("Test", "EUR Pay Leg"), ExternalId.of("Test", "EUR Receive Leg"));
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(convention, cycleObject(SwapConvention.class, convention));
  }

  @Test
  public void testSwapFixedLegConvention() {
    final SwapFixedLegConvention convention = new SwapFixedLegConvention("EUR Fixed Leg", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR Fixed Leg")),
        Tenor.THREE_MONTHS, DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), 2, false,
        Currency.EUR, ExternalId.of("Test", "EU"), StubType.LONG_END);
    convention.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(convention, cycleObject(SwapFixedLegConvention.class, convention));
  }

  @Test
  public void testSwapIndexConvention() {
    final SwapIndexConvention convention = new SwapIndexConvention("EUR 3m Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR 3m Swap")),
        LocalTime.of(11, 0), ExternalId.of("Test", "EUR 3m Swap"));
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEquals(convention, cycleObject(SwapIndexConvention.class, convention));
  }

  @Test
  public void testVanillaIborLegConvention() {
    final VanillaIborLegConvention convention = new VanillaIborLegConvention("EUR 3m Swap", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("EUR 3m Swap")),
        ExternalId.of("Test", "3m Euribor"), true, StubType.LONG_START, Interpolator1DFactory.LINEAR);
    convention.setUniqueId(UniqueId.of("Test", "12345"));
    assertEquals(convention, cycleObject(VanillaIborLegConvention.class, convention));
  }

  @Test
  public void testSpotTickerConvention() {
    final SpotTickerConvention convention = new SpotTickerConvention("3m Libor ticker", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("3m Libor ticker")),
        ExternalId.of("Test", "3m Libor"), Tenor.THREE_MONTHS);
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(SpotTickerConvention.class, convention));
  }

  @Test
  public void testForwardTickerConvention() {
    final ForwardTickerConvention convention = new ForwardTickerConvention("3mx6m swap ticker", ExternalIdBundle.of(InMemoryConventionBundleMaster.simpleNameSecurityId("3mx6m swap ticker")),
        ExternalId.of("Test", "USD Swap"), Tenor.THREE_MONTHS, Tenor.SIX_MONTHS);
    convention.setUniqueId(UniqueId.of("Test", "1234"));
    assertEquals(convention, cycleObject(ForwardTickerConvention.class, convention));
  }
}
