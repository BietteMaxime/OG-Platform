/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.cds.CDSFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.horizon.HorizonFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleInstrumentFunctions;
import com.opengamma.financial.analytics.model.swaption.SwaptionFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class ModelFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new ModelFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    // Nothing in this package, just the sub-packages
  }

  protected FunctionConfigurationSource bondFunctionConfiguration() {
    return BondFunctions.instance();
  }

  protected FunctionConfigurationSource bondFutureOptionFunctionConfiguration() {
    return BondFutureOptionFunctions.instance();
  }

  protected FunctionConfigurationSource cdsFunctionConfiguration() {
    return CDSFunctions.instance();
  }

  protected FunctionConfigurationSource creditFunctionConfiguration() {
    return CreditFunctions.instance();
  }

  protected FunctionConfigurationSource curveFunctionConfiguration() {
    return CurveFunctions.instance();
  }

  protected FunctionConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.instance();
  }

  protected FunctionConfigurationSource fixedIncomeFunctionConfiguration() {
    return FixedIncomeFunctions.instance();
  }

  protected FunctionConfigurationSource forexFunctionConfiguration() {
    return ForexFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource futureOptionFunctionConfiguration() {
    return FutureOptionFunctions.instance();
  }

  protected FunctionConfigurationSource horizonFunctionConfiguration() {
    return HorizonFunctions.instance();
  }

  protected FunctionConfigurationSource irFutureOptionFunctionConfiguration() {
    return IRFutureOptionFunctions.instance();
  }

  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected FunctionConfigurationSource pnlFunctionConfiguration() {
    return PNLFunctions.instance();
  }

  protected FunctionConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration>emptyList()));
  }

  protected FunctionConfigurationSource sabrCubeFunctionConfiguration() {
    return SABRCubeFunctions.instance();
  }

  protected FunctionConfigurationSource sensitivitiesFunctionConfiguration() {
    return SensitivitiesFunctions.instance();
  }

  protected FunctionConfigurationSource simpleInstrumentFunctionConfiguration() {
    return SimpleInstrumentFunctions.instance();
  }

  protected FunctionConfigurationSource swaptionFunctionConfiguration() {
    return SwaptionFunctions.instance();
  }

  protected FunctionConfigurationSource varFunctionConfiguration() {
    return VaRFunctions.instance();
  }

  protected FunctionConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), bondFunctionConfiguration(), bondFutureOptionFunctionConfiguration(), cdsFunctionConfiguration(),
        creditFunctionConfiguration(), curveFunctionConfiguration(), equityFunctionConfiguration(), fixedIncomeFunctionConfiguration(), forexFunctionConfiguration(),
        futureFunctionConfiguration(), futureOptionFunctionConfiguration(), horizonFunctionConfiguration(), irFutureOptionFunctionConfiguration(), optionFunctionConfiguration(),
        pnlFunctionConfiguration(), riskFactorFunctionConfiguration(), sabrCubeFunctionConfiguration(), sensitivitiesFunctionConfiguration(), simpleInstrumentFunctionConfiguration(),
        swaptionFunctionConfiguration(), varFunctionConfiguration(), volatilityFunctionConfiguration());
  }

}
