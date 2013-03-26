/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class FXForwardYieldCurveNodePnLFunction extends AbstractFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardYieldCurveNodePnLFunction.class);
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  /**
   * The compiled form.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final CurrencyPairs _currencyPairs;

    public Compiled(final CurrencyPairs currencyPairs) {
      _currencyPairs = currencyPairs;
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.POSITION;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final Position position = target.getPosition();
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      final Currency currencyBase = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency).getBase();
      final ValueProperties properties = createValueProperties()
          .withAny(ValuePropertyNames.PAY_CURVE)
          .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .withAny(ValuePropertyNames.RECEIVE_CURVE)
          .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
          .withAny(ValuePropertyNames.SAMPLING_PERIOD)
          .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
          .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
          .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES)
          .get();
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
      if (payCurveNames == null || payCurveNames.size() != 1) {
        return null;
      }
      final Set<String> payCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      if (payCurveCalculationConfigNames == null || payCurveCalculationConfigNames.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
      if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
      if (receiveCurveCalculationConfigNames == null || receiveCurveCalculationConfigNames.size() != 1) {
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

      final String payCurveCalculationConfigName = Iterables.getOnlyElement(payCurveCalculationConfigNames);
      final String receiveCurveCalculationConfigName = Iterables.getOnlyElement(receiveCurveCalculationConfigNames);
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
      final MultiCurveCalculationConfig payCurveCalculationConfig = curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
      if (payCurveCalculationConfig == null) {
        s_logger.error("Could not find curve calculation configuration named " + payCurveCalculationConfigName);
        return null;
      }
      final MultiCurveCalculationConfig receiveCurveCalculationConfig = curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
      if (receiveCurveCalculationConfig == null) {
        s_logger.error("Could not find curve calculation configuration named " + receiveCurveCalculationConfigName);
        return null;
      }
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final String payCurveName = Iterables.getOnlyElement(payCurveNames);
      final String receiveCurveName = Iterables.getOnlyElement(receiveCurveNames);
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      final String payCurrencyName = payCurrency.getCode();
      final String receiveCurrencyName = receiveCurrency.getCode();
      final ValueRequirement payYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
          payCurrencyName, payCurveName, security);
      final ValueRequirement receiveYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
          receiveCurrencyName, receiveCurveName, security);
      final String samplingPeriod = Iterables.getOnlyElement(samplingPeriods);
      final ValueRequirement payYCHTSRequirement = getYCHTSRequirement(payCurrency, payCurveName, samplingPeriod);
      final ValueRequirement receiveYCHTSRequirement = getYCHTSRequirement(receiveCurrency, receiveCurveName, samplingPeriod);
      final ValueRequirement fxSpotRequirement = ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(payCurrency, receiveCurrency);
      final Set<ValueRequirement> requirements = new HashSet<>();
      requirements.add(payYCNSRequirement);
      requirements.add(payYCHTSRequirement);
      requirements.add(receiveYCNSRequirement);
      requirements.add(receiveYCHTSRequirement);
      requirements.add(fxSpotRequirement);
      if (!payCurveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
        requirements.add(getCurveSpecRequirement(payCurrency, payCurveName));
      }
      if (!receiveCurveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
        requirements.add(getCurveSpecRequirement(receiveCurrency, receiveCurveName));
      }
      return requirements;
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final Position position = target.getPosition();
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
      final Clock snapshotClock = executionContext.getValuationClock();
      final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final String payCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      final String receiveCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
      final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
      final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
      final Period samplingPeriod = getSamplingPeriod(desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD));
      final LocalDate startDate = now.minus(samplingPeriod);
      final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
      final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
      final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
      final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
      final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
      final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
      final MultiCurveCalculationConfig payCurveCalculationConfig = curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
      final MultiCurveCalculationConfig receiveCurveCalculationConfig = curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
      final ValueRequirement payYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
          payCurrency.getCode(), payCurveName, security);
      final ValueRequirement receiveYCNSRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
          receiveCurrency.getCode(), receiveCurveName, security);
      final Object payYCNSObject = inputs.getValue(payYCNSRequirement);
      if (payYCNSObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve node sensitivities; " + payYCNSRequirement);
      }
      final Object receiveYCNSObject = inputs.getValue(receiveYCNSRequirement);
      if (receiveYCNSObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve node sensitivities; " + receiveYCNSRequirement);
      }
      final DoubleLabelledMatrix1D payYCNS = (DoubleLabelledMatrix1D) payYCNSObject;
      final DoubleLabelledMatrix1D receiveYCNS = (DoubleLabelledMatrix1D) receiveYCNSObject;
      final String samplingPeriodName = samplingPeriod.toString();
      final ValueRequirement payYCHTSRequirement = getYCHTSRequirement(payCurrency, payCurveName, samplingPeriodName);
      final ValueRequirement receiveYCHTSRequirement = getYCHTSRequirement(receiveCurrency, receiveCurveName, samplingPeriodName);
      final Object payYCHTSObject = inputs.getValue(payYCHTSRequirement);
      if (payYCHTSObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve historical time series; " + payYCHTSRequirement);
      }
      final Object receiveYCHTSObject = inputs.getValue(receiveYCHTSRequirement);
      if (receiveYCHTSObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve historical time series; " + receiveYCHTSRequirement);
      }
      final HistoricalTimeSeriesBundle payYCHTS = (HistoricalTimeSeriesBundle) payYCHTSObject;
      final HistoricalTimeSeriesBundle receiveYCHTS = (HistoricalTimeSeriesBundle) receiveYCHTSObject;
      DoubleTimeSeries<?> payResult = null;
      payResult = getPnLForCurve(inputs, position, payCurrency, payCurveName, samplingFunction, schedule, payResult, payCurveCalculationConfig, payYCNS, payYCHTS);
      DoubleTimeSeries<?> receiveResult = null;
      receiveResult = getPnLForCurve(inputs, position, receiveCurrency, receiveCurveName, samplingFunction, schedule, receiveResult, receiveCurveCalculationConfig,
          receiveYCNS, receiveYCHTS);
      final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency currencyBase = currencyPair.getBase();
      final Object fxSpotTSObject = inputs.getValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
      if (fxSpotTSObject == null) {
        throw new OpenGammaRuntimeException("Could not get spot FX time series");
      }
      final DoubleTimeSeries<?> fxSpotTS = (DoubleTimeSeries<?>) fxSpotTSObject;
      DoubleTimeSeries<?> result;
      if (payCurrency.equals(currencyBase)) {
        result = payResult;
        result = result.add(receiveResult.multiply(fxSpotTS));
      } else {
        result = receiveResult;
        result = result.add(payResult.multiply(fxSpotTS));
      }
      final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(resultSpec, payResult));
    }

  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String yieldCurveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private ValueRequirement getYCHTSRequirement(final Currency currency, final String yieldCurveName, final String samplingPeriod) {
    return HistoricalTimeSeriesFunctionUtils.createYCHTSRequirement(currency, yieldCurveName, MarketDataRequirementNames.MARKET_VALUE, null, DateConstraint.VALUATION_TIME.minus(samplingPeriod), true,
        DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getYCNSRequirement(final String payCurveName, final String payCurveCalculationConfigName, final String receiveCurveName,
      final String receiveCurveCalculationConfigName, final String currencyName, final String curveName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ComputationTargetType.SECURITY, security.getUniqueId(), properties);
  }

  private DoubleTimeSeries<?> getPnLForCurve(final FunctionInputs inputs, final Position position, final Currency currency, final String curveName,
      final TimeSeriesSamplingFunction samplingFunction, final LocalDate[] schedule, DoubleTimeSeries<?> result,
      final MultiCurveCalculationConfig curveCalculationConfig, final DoubleLabelledMatrix1D ycns, final HistoricalTimeSeriesBundle ychts) {
    final DoubleTimeSeries<?> pnLSeries;
    if (curveCalculationConfig.getCalculationMethod().equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      pnLSeries = getPnLSeries(ycns, ychts, schedule, samplingFunction);
    } else {
      final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
      final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
      if (curveSpecObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve specification; " + curveSpecRequirement);
      }
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
      pnLSeries = getPnLSeries(curveSpec, ycns, ychts, schedule, samplingFunction);
    }
    if (result == null) {
      result = pnLSeries;
    } else {
      result = result.add(pnLSeries);
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Could not get any values for security " + position.getSecurity());
    }
    result = result.multiply(position.getQuantity().doubleValue());
    return result;
  }

  private DoubleTimeSeries<?> getPnLSeries(final InterpolatedYieldCurveSpecificationWithSecurities spec, final DoubleLabelledMatrix1D curveSensitivities,
      final HistoricalTimeSeriesBundle timeSeriesBundle, final LocalDate[] schedule, final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final int n = curveSensitivities.size();
    final Object[] labels = curveSensitivities.getLabels();
    final List<Object> labelsList = Arrays.asList(labels);
    final double[] values = curveSensitivities.getValues();
    final SortedSet<FixedIncomeStripWithSecurity> strips = (SortedSet<FixedIncomeStripWithSecurity>) spec.getStrips();
    final FixedIncomeStripWithSecurity[] stripsArray = strips.toArray(new FixedIncomeStripWithSecurity[] {});
    final List<StripInstrumentType> stripList = new ArrayList<StripInstrumentType>(n);
    int stripCount = 0;
    for (final FixedIncomeStripWithSecurity strip : strips) {
      final int index = stripCount++; //labelsList.indexOf(strip.getSecurityIdentifier());
      if (index < 0) {
        throw new OpenGammaRuntimeException("Could not get index for " + strip);
      }
      stripList.add(index, strip.getInstrumentType());
    }
    for (int i = 0; i < n; i++) {
      final ExternalId id = stripsArray[i].getSecurityIdentifier();
      double sensitivity = values[i];
      if (stripList.get(i) == StripInstrumentType.FUTURE) {
        // TODO Temporary fix as sensitivity is to rate, but historical time series is to price (= 1 - rate)
        sensitivity *= -1;
      }
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id);
      }
      if (dbNodeTimeSeries.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Time series " + id + " is empty");
      }
      DoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(sensitivity);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(sensitivity));
      }
    }
    return pnlSeries;
  }

  private DoubleTimeSeries<?> getPnLSeries(final DoubleLabelledMatrix1D curveSensitivities, final HistoricalTimeSeriesBundle timeSeriesBundle, final LocalDate[] schedule,
      final TimeSeriesSamplingFunction samplingFunction) {
    DoubleTimeSeries<?> pnlSeries = null;
    final Object[] labels = curveSensitivities.getLabels();
    final double[] values = curveSensitivities.getValues();
    for (int i = 0; i < labels.length; i++) {
      final ExternalId id = (ExternalId) labels[i];
      final HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not identifier / price series pair for " + id);
      }
      DoubleTimeSeries<?> nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      nodeTimeSeries = DIFFERENCE.evaluate(nodeTimeSeries);
      if (pnlSeries == null) {
        pnlSeries = nodeTimeSeries.multiply(values[i]);
      } else {
        pnlSeries = pnlSeries.add(nodeTimeSeries.multiply(values[i]));
      }
    }
    return pnlSeries;
  }

  private Period getSamplingPeriod(final String samplingPeriodName) {
    return Period.parse(samplingPeriodName);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

}
