/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.option.EquityOptionBlackMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Returns the spot gamma w.r.t. the spot underlying, i.e. the 2nd order sensitivity of the present value to the spot value of the underlying,
 * $\frac{\partial^2 (PV)}{\partial S^2}$
 */
public class EquityOptionBlackSpotGammaFunction extends EquityOptionBlackFunction {

  /**
   * Default constructor
   */
  public EquityOptionBlackSpotGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    //FIXME use the type system
    if (derivative instanceof EquityIndexOption) {
      final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
      return Collections.singleton(new ComputedValue(resultSpec, model.gammaWrtSpot((EquityIndexOption) derivative, market)));
    }
    final EquityOptionBlackMethod model = EquityOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.gammaWrtSpot((EquityOption) derivative, market)));
  }

}
