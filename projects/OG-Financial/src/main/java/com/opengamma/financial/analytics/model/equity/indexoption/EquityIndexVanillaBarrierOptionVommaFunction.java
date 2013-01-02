/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the vomma (the second order sensitivity of the price w.r.t. implied volatility) for vanilla barrier options
 * using the Black formula.
 */
public class EquityIndexVanillaBarrierOptionVommaFunction extends EquityIndexVanillaBarrierOptionFunction {

  /**
   * Default constructor
   */
  public EquityIndexVanillaBarrierOptionVommaFunction() {
    super(ValueRequirementNames.VALUE_VOMMA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ValueSpecification resultSpec) {
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    double sum = 0.0;
    for (final EquityIndexOption derivative : vanillaOptions) {
      sum += model.vomma(derivative, market);
    }
    return Collections.singleton(new ComputedValue(resultSpec, sum));
  }

}
