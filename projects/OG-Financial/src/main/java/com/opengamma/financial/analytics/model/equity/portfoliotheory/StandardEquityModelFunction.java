/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * The Standard Equity Model Function simply returns the market value for any cash Equity security.
 */
public class StandardEquityModelFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    final double price = (Double) inputs.getValue(
        new ValueRequirement(
            MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.SECURITY,
            equity.getUniqueId()));
    return Collections.<ComputedValue>singleton(
        new ComputedValue(
            new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY, equity.getCurrency().getCode()).get()),
                price));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, equity.getUniqueId()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    return Collections.<ValueSpecification>singleton(
        new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(),
            createValueProperties().with(ValuePropertyNames.CURRENCY, equity.getCurrency().getCode()).get()));
  }

  @Override
  public String getShortName() {
    return "StandardEquityModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_SECURITY;
  }

}
