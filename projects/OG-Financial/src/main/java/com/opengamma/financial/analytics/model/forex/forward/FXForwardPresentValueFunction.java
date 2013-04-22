/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyMatrixSpotSourcingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Calculates Present Value on FX Forward instruments.
 */
public class FXForwardPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING)
        .get();
    final ValueRequirement fxPvRequirement = new ValueRequirement(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = getPayCurrency(security);
    final Currency receiveCurrency = getReceiveCurrency(security);
    final ValueRequirement spotRateRequirement = CurrencyMatrixSpotSourcingFunction.getConversionRequirement(payCurrency, receiveCurrency);
    return ImmutableSet.of(fxPvRequirement, spotRateRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    ValueProperties properties = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      if (entry.getKey().getValueName().equals(ValueRequirementNames.FX_PRESENT_VALUE)) {
        properties = entry.getKey().getProperties();
        break;
      }
    }
    if (properties == null) {
      return null;
    }
    final Currency currency = getPayCurrency((FinancialSecurity) target.getSecurity());
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), getResultProperties(currency, properties.copy())));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = getPayCurrency(security);
    final Currency receiveCurrency = getReceiveCurrency(security);
    final ComputedValue input = inputs.getComputedValue(ValueRequirementNames.FX_PRESENT_VALUE);
    final ValueSpecification inputSpec = input.getSpecification();
    final CurrencyLabelledMatrix1D fxPresentValue = (CurrencyLabelledMatrix1D) input.getValue();
    if (fxPresentValue.size() != 2) {
      throw new OpenGammaRuntimeException("Expected " + ValueRequirementNames.FX_PRESENT_VALUE + " input to contain 2 currency values, but found " + fxPresentValue.size());
    }
    int payIndex = -1;
    int receiveIndex = -1;
    for (int i = 0; i < 2; i++) {
      final Currency currency = fxPresentValue.getKeys()[i];
      if (payCurrency.equals(currency)) {
        payIndex = i;
      } else if (receiveCurrency.equals(currency)) {
        receiveIndex = i;
      } else {
        throw new OpenGammaRuntimeException(ValueRequirementNames.FX_PRESENT_VALUE + " contains unexpected currency " + currency + ". Expected " + payCurrency + " or " + receiveCurrency + ".");
      }
    }

    final double payValue = fxPresentValue.getValues()[payIndex];
    final double receiveValue = fxPresentValue.getValues()[receiveIndex];

    final double spot = (Double) inputs.getValue(ValueRequirementNames.SPOT_RATE);
    final double pv = payValue + spot * receiveValue;
    return ImmutableSet.of(new ComputedValue(getResultSpec(target, inputSpec.getProperties().copy()), pv));
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final ValueProperties.Builder fxPresentValueProperties) {
    final Currency currency = getPayCurrency((FinancialSecurity) target.getSecurity());
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), getResultProperties(currency, fxPresentValueProperties));
  }

  protected ValueProperties getResultProperties(final Currency currency, final ValueProperties.Builder fxPresentValueProperties) {
    return fxPresentValueProperties.withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .get();
  }

  protected Currency getPayCurrency(final FinancialSecurity security) {
    return security.accept(ForexVisitors.getPayCurrencyVisitor());
  }

  protected Currency getReceiveCurrency(final FinancialSecurity security) {
    return security.accept(ForexVisitors.getReceiveCurrencyVisitor());
  }

}
