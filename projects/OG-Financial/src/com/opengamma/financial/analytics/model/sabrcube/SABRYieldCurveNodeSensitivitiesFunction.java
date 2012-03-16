/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CapFloorCMSSpreadSecurityConverter;
import com.opengamma.financial.analytics.conversion.CapFloorSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunctionHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
      .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final double CUT_OFF = 0.1;
  private static final double MU = 5;
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private final PresentValueNodeSensitivityCalculator _nodeSensitivityCalculator;
  private SecuritySource _securitySource;
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _securityVisitor;
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final VolatilityCubeFunctionHelper _helper;
  private final boolean _useSABRExtrapolation;
  private FixedIncomeConverterDataProvider _definitionConverter;

  public SABRYieldCurveNodeSensitivitiesFunction(final String currency, final String definitionName, final String useSABRExtrapolation, final String forwardCurveName, final String fundingCurveName) {
    this(Currency.of(currency), definitionName, Boolean.parseBoolean(useSABRExtrapolation), forwardCurveName, fundingCurveName);
  }

  public SABRYieldCurveNodeSensitivitiesFunction(final Currency currency, final String definitionName, final boolean useSABRExtrapolation, final String forwardCurveName, 
      final String fundingCurveName) {
    _nodeSensitivityCalculator = useSABRExtrapolation ? PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRExtrapolationCalculator.getInstance())
        : PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
    _fundingCurveName = fundingCurveName;
    _forwardCurveName = forwardCurveName;
    _useSABRExtrapolation = useSABRExtrapolation;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(_securitySource, conventionSource, swapConverter);
    final CapFloorSecurityConverter capFloorConverter = new CapFloorSecurityConverter(holidaySource, conventionSource, regionSource);
    final CapFloorCMSSpreadSecurityConverter capFloorCMSSpreadSecurityVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource, regionSource);
    _securityVisitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).swaptionVisitor(swaptionConverter).capFloorVisitor(capFloorConverter)
        .capFloorCMSSpreadVisitor(capFloorCMSSpreadSecurityVisitor).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(_securityVisitor);
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, new String[] {_fundingCurveName, _forwardCurveName}, dataSource);
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Object forwardCurveObject = inputs.getValue(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object couponSensitivitiesObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (couponSensitivitiesObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve instrument coupon sensitivities");
    }
    final Object jacobianObject = inputs.getValue(getJacobianRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get jacobian");
    }
    final Object forwardCurveSpecObject = inputs.getValue(getForwardCurveSpecRequirement(currency, _forwardCurveName));
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve spec");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    if (_forwardCurveName.equals(_fundingCurveName)) {
      final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
      final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
      interpolatedCurves.put(_forwardCurveName, forwardCurve);
      final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivitiesObject;
      final SABRInterestRateDataBundle data = getModelParameters(target, inputs, bundle);
      final DoubleMatrix2D jacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(jacobianObject));
      final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(derivative, null, data, couponSensitivity, jacobian, _nodeSensitivityCalculator);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(_forwardCurveName, bundle, result, forwardCurveSpec, getForwardResultSpec(target, currency));
    }
    final Object fundingCurveObject = inputs.getValue(getFundingCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final Object fundingCurveSpecObject = inputs.getValue(getFundingCurveSpecRequirement(currency, _fundingCurveName));
    if (fundingCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve spec");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    interpolatedCurves.put(_fundingCurveName, fundingCurve);
    interpolatedCurves.put(_forwardCurveName, forwardCurve);
    final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivitiesObject;
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(FunctionUtils.decodeJacobian(jacobianObject));
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, bundle);
    final DoubleMatrix1D result = CALCULATOR.calculateFromPresentValue(derivative, null, data, couponSensitivity, jacobian, _nodeSensitivityCalculator);
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(_fundingCurveName, fundingCurveSpec);
    curveSpecs.put(_forwardCurveName, forwardCurveSpec);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(_forwardCurveName, _fundingCurveName, getForwardResultSpec(target, currency), getFundingResultSpec(target, currency),
        bundle, result, curveSpecs);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    return security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS 
            || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (_forwardCurveName.equals(_fundingCurveName)) {
      result.add(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getForwardCurveSpecRequirement(currency, _forwardCurveName));
    } else {
      result.add(getForwardCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getFundingCurveRequirement(currency, _forwardCurveName, _fundingCurveName));
      result.add(getForwardCurveSpecRequirement(currency, _forwardCurveName));
      result.add(getFundingCurveSpecRequirement(currency, _fundingCurveName));
    }
    result.add(getCouponSensitivitiesRequirement(currency, _forwardCurveName, _fundingCurveName));
    result.add(getJacobianRequirement(currency, _forwardCurveName, _fundingCurveName));
    result.add(getCubeRequirement(target));
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    return Sets.newHashSet(getForwardResultSpec(target, currency), getFundingResultSpec(target, currency));
  }

  protected boolean isUseSABRExtrapolation() {
    return _useSABRExtrapolation;
  }

  private ValueRequirement getForwardCurveRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    final ValueRequirement forwardCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, forwardCurveDefinitionName, forwardCurveDefinitionName, fundingCurveDefinitionName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return forwardCurveRequirement;
  }

  private ValueRequirement getForwardCurveSpecRequirement(final Currency currency, final String forwardCurveDefinitionName) {
    final ValueRequirement forwardCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveDefinitionName).get());
    return forwardCurveRequirement;
  }

  private ValueRequirement getFundingCurveRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    final ValueRequirement fundingCurveRequirement = YieldCurveFunction.getCurveRequirement(currency, fundingCurveDefinitionName, forwardCurveDefinitionName, fundingCurveDefinitionName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return fundingCurveRequirement;
  }

  private ValueRequirement getFundingCurveSpecRequirement(final Currency currency, final String fundingCurveDefinitionName) {
    final ValueRequirement fundingCurveRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, fundingCurveDefinitionName).get());
    return fundingCurveRequirement;
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveDefinitionName, fundingCurveDefinitionName);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String forwardCurveDefinitionName, final String fundingCurveDefinitionName) {
    return YieldCurveFunction.getJacobianRequirement(currency, forwardCurveDefinitionName, fundingCurveDefinitionName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
  }

  protected ValueRequirement getCubeRequirement(final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, FinancialSecurityUtils.getCurrency(target.getSecurity()), properties);
  }

  private ValueSpecification getForwardResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE, _forwardCurveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, _useSABRExtrapolation ? SABRFunction.SABR_RIGHT_EXTRAPOLATION : SABRFunction.SABR_NO_EXTRAPOLATION).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), result);
  }

  private ValueSpecification getFundingResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE, _fundingCurveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, _useSABRExtrapolation ? SABRFunction.SABR_RIGHT_EXTRAPOLATION : SABRFunction.SABR_NO_EXTRAPOLATION).get();
    return new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), result);
  }

  protected SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final YieldCurveBundle bundle) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement surfacesRequirement = getCubeRequirement(target);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return _useSABRExtrapolation ? new SABRInterestRateDataBundle(new SABRInterestRateExtrapolationParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, CUT_OFF, MU), bundle)
        : new SABRInterestRateDataBundle(new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION), bundle);
  }

}
