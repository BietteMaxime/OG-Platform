/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.DeprecatedFunctions;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FixedIncomeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new FixedIncomeFunctions().getObjectCreating();
  }

  public static FunctionConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveCalculationConfig;

      public void setCurveCalculationConfig(final String curveCalculationConfig) {
        _curveCalculationConfig = curveCalculationConfig;
      }

      public String getCurveCalculationConfig() {
        return _curveCalculationConfig;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveCalculationConfig(), "curveCalculationConfig");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
    private boolean _includeIRFutures;

    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

    public void setIncludeIRFutures(final boolean includeIRFutures) {
      _includeIRFutures = includeIRFutures;
    }

    public boolean isIncludeIRFutures() {
      return _includeIRFutures;
    }

    protected void addInterestRateInstrumentDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[1 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = Boolean.toString(isIncludeIRFutures());
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveCalculationConfig();
      }
      functions.add(functionConfiguration(InterestRateInstrumentDefaultPropertiesFunction.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addInterestRateInstrumentDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentPresentValueFunction.class));
    functions.add(functionConfiguration(InterestRateInstrumentPV01Function.class));
    functions.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(CrossCurrencySwapPVFunction.class));
  }

}
