/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyMatrixSeriesSourcingFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencySeriesConversionFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 *
 */
public class FXForwardCurrencyExposurePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final String scheduleCalculator = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunction = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
    final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    final DoubleTimeSeries<?> timeSeries = (DoubleTimeSeries<?>) inputs.getValue(CurrencySeriesConversionFunction.SPOT_RATE);
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    if (timeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get spot FX series for " + payCurrency + " / " + receiveCurrency);
    }
    final LocalDate startDate = now.toLocalDate().minus(Period.parse(samplingPeriod));
    final Schedule schedule = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculator);
    final TimeSeriesSamplingFunction sampling = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunction);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
    final double exposure = mca.getAmount(currencyNonBase);
    DoubleTimeSeries<?> result = getPnLSeries(startDate, now.toLocalDate(), schedule, sampling, timeSeries);
    result = result.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
    final Currency currencyBase = currencyPair.getBase(); // The base currency
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveConfig)
        .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), resultProperties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Currency currencyBase = currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency()).getBase();
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
        .get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveCalculationConfigs == null || payCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveCalculationConfigs == null || receiveCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculatorSet = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorSet == null || scheduleCalculatorSet.size() != 1) {
      return null;
    }
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final ValueRequirement fxCurrencyExposureRequirement = new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(target.getPosition().getSecurity()),
        ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigs.iterator().next())
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigs.iterator().next()).get());
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final ValueRequirement fxSpotRequirement;
    if (currencyPair.getBase().equals(payCurrency)) {
      fxSpotRequirement = CurrencyMatrixSeriesSourcingFunction.getConversionRequirement(payCurrency, receiveCurrency);
    } else {
      fxSpotRequirement = CurrencyMatrixSeriesSourcingFunction.getConversionRequirement(receiveCurrency, payCurrency);
    }
    return ImmutableSet.of(fxCurrencyExposureRequirement, fxSpotRequirement);
  }

  private DoubleTimeSeries<?> getPnLSeries(final LocalDate startDate, final LocalDate now, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction, final DoubleTimeSeries<?> dbTimeSeries) {
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    DoubleTimeSeries<?> result = samplingFunction.getSampledTimeSeries(dbTimeSeries, dates);
    result = result.reciprocal(); // Implementation note: to obtain the P/L for one unit of non-base currency expressed in base currency.
    return DIFFERENCE.evaluate(result);
  }
}
