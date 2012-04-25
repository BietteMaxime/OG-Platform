/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 * 
 */
public class UnitPositionScalingFunction extends PropertyPreservingFunction {

  @Override
  protected Collection<String> getPreservedProperties() {
    return Arrays.asList(
        ValuePropertyNames.CURRENCY,
        ValuePropertyNames.CALCULATION_METHOD,
        ValuePropertyNames.CURVE,
        YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        YieldCurveFunction.PROPERTY_FUNDING_CURVE,
        ValuePropertyNames.PAY_CURVE,
        ValuePropertyNames.RECEIVE_CURVE,
        BondFunction.PROPERTY_CREDIT_CURVE,
        BondFunction.PROPERTY_RISK_FREE_CURVE,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_EXTERNAL_BETA,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_MODEL,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SABR_WEIGHTING_FUNCTION,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM,
        BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS,
        LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS,
        LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS_PARAMETERIZATION,
        PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION,
        PDEPropertyNamesAndValues.PROPERTY_CENTRE_MONEYNESS,
        PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME,
        PDEPropertyNamesAndValues.PROPERTY_MAX_MONEYNESS,
        PDEPropertyNamesAndValues.PROPERTY_MAX_PROXY_DELTA,
        PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS,
        PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS,
        PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR,
        PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING,
        PDEPropertyNamesAndValues.PROPERTY_THETA,
        PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING);
  }

  @Override
  protected Collection<String> getOptionalPreservedProperties() {
    return Collections.emptySet();
  }

  private final String _requirementName;

  public UnitPositionScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue value = inputs.getAllValues().iterator().next();
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(_requirementName, target.toSpecification()), getResultProperties(value.getSpecification()));
    return Sets.newHashSet(new ComputedValue(specification, value.getValue()));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue));
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet().iterator().next()));
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "UnitPositionScalingFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
