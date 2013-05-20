/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardForwardPointsDefaults;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.DeprecatedFunctions;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForwardFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new ForwardFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardPV01Function.class));
    functions.add(functionConfiguration(FXForwardFXPresentValueFunction.class));
    functions.add(functionConfiguration(FXForwardPresentValueFunction.class));
    functions.add(functionConfiguration(FXForwardCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXForwardYCNSFunction.class));
    functions.add(functionConfiguration(FXForwardFXImpliedYCNSFunction.class));
    functions.add(functionConfiguration(FXForwardPresentValueCurveSensitivityFunction.class));
    functions.add(functionConfiguration(FXForwardPointsMethodPresentValueFunction.class));
    functions.add(functionConfiguration(FXForwardPointsMethodCurrencyExposureFunction.class));
    functions.add(functionConfiguration(FXForwardPointsMethodFCNSFunction.class));
    functions.add(functionConfiguration(FXForwardForwardPointsDefaults.class, "USD", "EUR", "DEFAULT"));
  }

}
