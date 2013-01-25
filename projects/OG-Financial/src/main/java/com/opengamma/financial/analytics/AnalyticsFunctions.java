/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.cashflow.CashFlowFunctions;
import com.opengamma.financial.analytics.ircurve.IRCurveFunctions;
import com.opengamma.financial.analytics.model.ModelFunctions;
import com.opengamma.financial.analytics.model.riskfactor.option.OptionGreekToValueGreekConverterFunction;
import com.opengamma.financial.analytics.timeseries.TimeSeriesFunctions;
import com.opengamma.financial.analytics.volatility.VolatilityFunctions;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class AnalyticsFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new AnalyticsFunctions().getObjectCreating();
  }

  /**
   * Adds an aggregation function for the given requirement name that produces the sum of the child position values.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(FilteringSummingFunction.class, requirementName));
    functions.add(functionConfiguration(SummingFunction.class, requirementName));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, requirementName, MissingInputsFunction.AGGREGATION_STYLE_FULL,
        FilteringSummingFunction.AGGREGATION_STYLE_FILTERED));
  }

  public static void addValueGreekAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(OptionGreekToValueGreekConverterFunction.class, requirementName));
    addScalingAndSummingFunction(functions, requirementName);
  }

  /**
   * Adds a unit scaling function to deliver the value from position's underlying security or trade at the position level. This is normally used for positions in OTC instruments that are stored with a
   * quantity of 1 in OpenGamma.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addUnitScalingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(UnitPositionScalingFunction.class, requirementName));
    functions.add(functionConfiguration(UnitPositionTradeScalingFunction.class, requirementName));
  }

  public static void addUnitScalingAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addUnitScalingFunction(functions, requirementName);
    addSummingFunction(functions, requirementName);
  }

  /**
   * Adds a scaling function to deliver the value from a position's underlying security or trade multiplied by the quantity at the position level. This is used for positions in exchange traded
   * instruments.
   *
   * @param functions the function configuration list to update, not null
   * @param requirementName the requirement name, not null
   */
  public static void addScalingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    functions.add(functionConfiguration(PositionScalingFunction.class, requirementName));
    functions.add(functionConfiguration(PositionTradeScalingFunction.class, requirementName));
  }

  public static void addScalingAndSummingFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addScalingFunction(functions, requirementName);
    addSummingFunction(functions, requirementName);
  }

  public static void addLastHistoricalValueFunction(final List<FunctionConfiguration> functions, final String requirementName) {
    addUnitScalingFunction(functions, requirementName);
    functions.add(functionConfiguration(LastHistoricalValueFunction.class, requirementName));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(AttributesFunction.class));
    functions.add(functionConfiguration(CurrencyPairsFunction.class));
    functions.add(functionConfiguration(DV01Function.class));
    functions.add(functionConfiguration(NotionalFunction.class));
    functions.add(functionConfiguration(PortfolioNodeWeightFunction.class));
    functions.add(functionConfiguration(PositionWeightFunction.class));
    addUnitScalingFunction(functions, ValueRequirementNames.ATTRIBUTES);
    addUnitScalingFunction(functions, ValueRequirementNames.BLACK_VOLATILITY_GRID_PRICE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);
    addUnitScalingFunction(functions, ValueRequirementNames.BOND_TENOR);
    addUnitScalingFunction(functions, ValueRequirementNames.CARRY_RHO);
    addSummingFunction(functions, ValueRequirementNames.CREDIT_SENSITIVITIES);
    addUnitScalingFunction(functions, ValueRequirementNames.CLEAN_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.CONVEXITY);
    addSummingFunction(functions, ValueRequirementNames.CS01);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_APPLIED_BETA);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_MARKET_CAP);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_PRICE);
    addLastHistoricalValueFunction(functions, ValueRequirementNames.DAILY_VOLUME);
    addUnitScalingFunction(functions, ValueRequirementNames.DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.DELTA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.DIRTY_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.DRIFTLESS_THETA);
    addUnitScalingFunction(functions, ValueRequirementNames.DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.DUAL_GAMMA);
    addSummingFunction(functions, ValueRequirementNames.DV01);
    addUnitScalingFunction(functions, ValueRequirementNames.DVANNA_DVOL);
    addUnitScalingFunction(functions, ValueRequirementNames.DZETA_DVOL);
    addUnitScalingFunction(functions, ValueRequirementNames.ELASTICITY);
    addSummingFunction(functions, ValueRequirementNames.EXTERNAL_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FAIR_VALUE);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FIXED_PAY_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FLOATING_PAY_CASH_FLOWS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.FLOATING_RECEIVE_CASH_FLOWS);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.FORWARD_VOMMA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addUnitScalingFunction(functions, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.FX_PRESENT_VALUE);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_P);
    addUnitScalingFunction(functions, ValueRequirementNames.GAMMA_P_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_DUAL_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_FORWARD_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.GRID_PRESENT_VALUE);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.GROSS_BASIS);
    addUnitScalingFunction(functions, ValueRequirementNames.IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DUAL_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_DUAL_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_FOREX_PV_QUOTES);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_FULL_PDE_GRID);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GRID_IMPLIED_VOL);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_GRID_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_PDE_BUCKETED_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_PDE_GREEKS);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.LOCAL_VOLATILITY_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.MACAULAY_DURATION);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_CLEAN_PRICE);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addSummingFunction(functions, ValueRequirementNames.MTM_PNL);
    addUnitScalingFunction(functions, ValueRequirementNames.MTM_PNL);
    addUnitScalingFunction(functions, ValueRequirementNames.MARKET_YTM);
    addUnitScalingFunction(functions, ValueRequirementNames.MODIFIED_DURATION);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.NET_BASIS);
    addUnitScalingAndSummingFunction(functions, ValueRequirementNames.NETTED_FIXED_CASH_FLOWS);
    addUnitScalingFunction(functions, ValueRequirementNames.NOTIONAL);
    addUnitScalingFunction(functions, ValueRequirementNames.PHI);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY);
    addUnitScalingFunction(functions, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
    addUnitScalingFunction(functions, ValueRequirementNames.PIECEWISE_SABR_VOL_SURFACE);
    addSummingFunction(functions, ValueRequirementNames.PNL_SERIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY);
    addScalingFunction(functions, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY);
    addSummingFunction(functions, ValueRequirementNames.PRICE_SERIES);
    addScalingAndSummingFunction(functions, ValueRequirementNames.PV01);
    addUnitScalingFunction(functions, ValueRequirementNames.RHO);
    addUnitScalingFunction(functions, ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY);
    addUnitScalingFunction(functions, ValueRequirementNames.SPEED);
    addUnitScalingFunction(functions, ValueRequirementNames.SPEED_P);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT_FX_PERCENTAGE_CHANGE);
    addUnitScalingFunction(functions, ValueRequirementNames.SPOT_RATE_FOR_SECURITY);
    addUnitScalingFunction(functions, ValueRequirementNames.STRIKE_DELTA);
    addUnitScalingFunction(functions, ValueRequirementNames.STRIKE_GAMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.THETA);
    addUnitScalingFunction(functions, ValueRequirementNames.ULTIMA);
    addSummingFunction(functions, ValueRequirementNames.VALUE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_CARRY_RHO);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_DELTA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_DUAL_DELTA);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_GAMMA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_PHI);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_RHO);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_SPEED);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_THETA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_VANNA);
    addValueGreekAndSummingFunction(functions, ValueRequirementNames.VALUE_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VALUE_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_ULTIMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VANNA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.VARIANCE_VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA_BLEED);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_MATRIX);
    addUnitScalingFunction(functions, ValueRequirementNames.VEGA_P);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_QUOTE_CUBE);
    addScalingAndSummingFunction(functions, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addUnitScalingFunction(functions, ValueRequirementNames.VOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.VOMMA_P);
    addUnitScalingFunction(functions, ValueRequirementNames.WEIGHTED_VEGA);
    addSummingFunction(functions, ValueRequirementNames.WEIGHTED_VEGA);
    addScalingAndSummingFunction(functions, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addUnitScalingFunction(functions, ValueRequirementNames.YTM);
    addUnitScalingFunction(functions, ValueRequirementNames.ZETA);
    addUnitScalingFunction(functions, ValueRequirementNames.ZETA_BLEED);
    addUnitScalingFunction(functions, ValueRequirementNames.Z_SPREAD);
    addUnitScalingFunction(functions, ValueRequirementNames.ZOMMA);
    addUnitScalingFunction(functions, ValueRequirementNames.ZOMMA_P);
  }

  protected RepositoryConfigurationSource cashFlowFunctionConfiguration() {
    return CashFlowFunctions.instance();
  }

  protected RepositoryConfigurationSource irCurveFunctionConfiguration() {
    return IRCurveFunctions.instance();
  }

  protected RepositoryConfigurationSource modelFunctionConfiguration() {
    return ModelFunctions.instance();
  }

  protected RepositoryConfigurationSource timeSeriesFunctionConfiguration() {
    return TimeSeriesFunctions.instance();
  }

  protected RepositoryConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), cashFlowFunctionConfiguration(), irCurveFunctionConfiguration(),
        modelFunctionConfiguration(), timeSeriesFunctionConfiguration(), volatilityFunctionConfiguration());
  }

}
