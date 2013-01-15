/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.CalendricalException;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NettedFixedCashFlowFromDateCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class NettedFixedCashFlowFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the date field */
  public static final String PROPERTY_DATE = "Date";
  private static final Logger s_logger = LoggerFactory.getLogger(NettedFixedCashFlowFunction.class);
  private static final NettedFixedCashFlowFromDateCalculator NETTING_CASH_FLOW_CALCULATOR = NettedFixedCashFlowFromDateCalculator.getInstance();
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final ForexSecurityConverter fxConverter = new ForexSecurityConverter(baseQuotePairs);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter).fxForwardVisitor(fxConverter)
        .nonDeliverableFxForwardVisitor(fxConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final InstrumentDefinition<?> definition = ((FinancialSecurity) target.getSecurity()).accept(_visitor);
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String dateString = desiredValue.getConstraint(PROPERTY_DATE);
    final LocalDate date = LocalDate.parse(dateString);
    final Map<LocalDate, MultipleCurrencyAmount> cashFlows;
    if (inputs.getAllValues().isEmpty()) {
      cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, date);
    } else {
      final HistoricalTimeSeries fixingSeries = (HistoricalTimeSeries) Iterables.getOnlyElement(inputs.getAllValues()).getValue();
      if (fixingSeries == null) {
        cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, date);
      } else {
        cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, fixingSeries.getTimeSeries(), date);
      }
    }
    final ValueProperties properties = createValueProperties().with(PROPERTY_DATE, dateString).get();
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.NETTED_FIXED_CASH_FLOWS, target.toSpecification(), properties),
        new FixedPaymentMatrix(cashFlows)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(PROPERTY_DATE).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.NETTED_FIXED_CASH_FLOWS, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> dates = constraints.getValues(PROPERTY_DATE);
    if (dates == null || dates.size() != 1) {
      s_logger.error("Must supply a date from which to calculate the cash-flows");
      return null;
    }
    final String date = Iterables.getOnlyElement(dates);
    try {
      LocalDate.parse(date);
    } catch (final CalendricalException e) {
      s_logger.error("Could not parse date {} - must be in form YYYY-MM-DD", date);
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    return _definitionConverter.getConversionTimeSeriesRequirements(security, definition);
  }

}
