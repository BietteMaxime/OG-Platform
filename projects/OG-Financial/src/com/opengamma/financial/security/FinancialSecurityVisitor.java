/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.*;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * General visitor for top level asset classes.
 * 
 * @param <T> Return type for visitor.
 */
public interface FinancialSecurityVisitor<T> {

  T visitBondSecurity(BondSecurity security);

  T visitCashSecurity(CashSecurity security);

  T visitEquitySecurity(EquitySecurity security);

  T visitFRASecurity(FRASecurity security);

  T visitFutureSecurity(FutureSecurity security);

  T visitSwapSecurity(SwapSecurity security);

  T visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security);

  T visitEquityOptionSecurity(EquityOptionSecurity security);

  T visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security);

  T visitFXOptionSecurity(FXOptionSecurity security);

  T visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security);

  T visitSwaptionSecurity(SwaptionSecurity security);

  T visitIRFutureOptionSecurity(IRFutureOptionSecurity security);

  T visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security);

  T visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security);

  T visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security);

  T visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security);

  T visitFXForwardSecurity(FXForwardSecurity security);

  T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security);

  T visitCapFloorSecurity(CapFloorSecurity security);

  T visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security);

  T visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security);

  T visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security);

  T visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security);

  T visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security);

  T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity commodityFutureOptionSecurity);
}
