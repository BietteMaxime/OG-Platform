/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.forex.option.OptionFunctions;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class ForexFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   * 
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new ForexFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ConventionBasedFXRateFunction.class));
    functions.add(functionConfiguration(SecurityFXHistoricalTimeSeriesFunction.class));
  }

  protected RepositoryConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), forwardFunctionConfiguration(), optionFunctionConfiguration());
  }

}
