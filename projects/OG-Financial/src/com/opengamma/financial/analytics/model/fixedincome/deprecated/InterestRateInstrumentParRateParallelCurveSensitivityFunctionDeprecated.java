/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome.deprecated;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.ParRateParallelSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateParallelCurveSensitivityFunction;

/**
 * @deprecated Use the function that does not take the funding and forward curve
 * @see InterestRateInstrumentParRateParallelCurveSensitivityFunction
 */
@Deprecated
public class InterestRateInstrumentParRateParallelCurveSensitivityFunctionDeprecated extends InterestRateInstrumentCurveSpecificFunctionDeprecated {
  private static final ParRateParallelSensitivityCalculator CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();

  public InterestRateInstrumentParRateParallelCurveSensitivityFunctionDeprecated() {
    super(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec) {
    final Map<String, Double> sensitivities = CALCULATOR.visit(derivative, curves);
    if (!sensitivities.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get par rate parallel curve shift sensitivity for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, sensitivities.get(curveName)));
  }

}
