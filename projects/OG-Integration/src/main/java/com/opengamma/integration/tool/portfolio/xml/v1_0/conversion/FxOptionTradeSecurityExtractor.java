/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxOptionTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SettlementType;
import com.opengamma.master.security.ManageableSecurity;

public class FxOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxOptionTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(FxOptionTrade fxOptionTrade) {

    FxOptionCalculator calculator = new FxOptionCalculator(fxOptionTrade, fxOptionTrade.getNotional(), fxOptionTrade.getNotionalCurrency());

    ExerciseType exerciseType = fxOptionTrade.getExerciseType() == com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ExerciseType.AMERICAN ?
        new AmericanExerciseType() : new EuropeanExerciseType();

    ManageableSecurity security = fxOptionTrade.getSettlementType() == SettlementType.PHYSICAL ?
        new FXOptionSecurity(calculator.getPutCurrency(),
                             calculator.getCallCurrency(),
                             calculator.getPutAmount(),
                             calculator.getCallAmount(),
                             calculator.getExpiry(),
                             calculator.getSettlementDate(),
                             calculator.isLong(),
                             exerciseType) :
        new NonDeliverableFXOptionSecurity(calculator.getPutCurrency(),
                                           calculator.getCallCurrency(),
                                           calculator.getPutAmount(),
                                           calculator.getCallAmount(),
                                           calculator.getExpiry(),
                                           calculator.getSettlementDate(),
                                           calculator.isLong(),
                                           exerciseType,
                                           fxOptionTrade.getSettlementCurrency().equals(calculator.getCallCurrency().getCode()));

    // Generate the loader SECURITY_ID (should be uniquely identifying)
    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(calculator.isLong())
            .append(calculator.getCallCurrency())
            .append(calculator.getCallAmount())
            .append(calculator.getPutCurrency())
            .append(calculator.getPutAmount())
            .append(calculator.getExpiry())
            .append(exerciseType).toHashCode()
    )));

    return securityArray(security);
  }

}
