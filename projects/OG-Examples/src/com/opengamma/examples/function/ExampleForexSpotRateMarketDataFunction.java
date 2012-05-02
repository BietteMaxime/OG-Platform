/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.forex.AbstractForexSpotRateMarketDataFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class ExampleForexSpotRateMarketDataFunction extends AbstractForexSpotRateMarketDataFunction {
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(desiredValue.getTargetSpecification().getUniqueId());
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, 
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, currencyPair.getFirstCurrency().getCode() + currencyPair.getSecondCurrency().getCode()));
    return ImmutableSet.of(spotRequirement);
  }

}
