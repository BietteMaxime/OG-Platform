/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.future.FutureFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class CurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource interestRateFunctionConfiguration() {
    return InterestRateFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), forwardFunctionConfiguration(), futureFunctionConfiguration(), interestRateFunctionConfiguration());
  }

}
