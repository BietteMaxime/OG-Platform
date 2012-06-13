/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.bloombergexample.tool.ExampleDatabasePopulator;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.money.Currency;

/**
 * Example code to create a set of example views.
 * <p>
 * It is designed to run against the HSQLDB example database.  
 */
public class ExampleViewsPopulator extends AbstractExampleTool {

  private static final String FORWARD_3M = "FORWARD_3M";
  private static final String FORWARD_6M = "FORWARD_6M";
  private static final String FUNDING = "FUNDING";
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleViewsPopulator().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    createEquityViewDefinition();
    createSwapViewDefinition();
    createFXViewDefinition();
    createMultiCurrencySwapViewDefinition();
    createEquityOptionViewDefinition();
    //createMixedPortfolioViewDefinition();
  }

  private void createEquityViewDefinition() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createSwapViewDefinition() {
    storeViewDefinition(getSwapViewDefinition(ExampleSwapPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createMultiCurrencySwapViewDefinition() {
    storeViewDefinition(getMultiCurrencySwapViewDefinition());
  }

  private void createMixedPortfolioViewDefinition() {
    storeViewDefinition(getMixedPortfolioViewDefinition());
  }

  private void createFXViewDefinition() {
    storeViewDefinition(getFXViewDefinition());
  }
  
  private void createEquityOptionViewDefinition() {
    storeViewDefinition(getEquityOptionViewDefinition(DemoEquityOptionCollarPortfolioLoader.PORTFOLIO_NAME));
  }
  
  private ViewDefinition getEquityOptionViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.setMinDeltaCalculationPeriod(500L);
    equityViewDefinition.setMaxDeltaCalculationPeriod(30000L);
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());    
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.HISTORICAL_VAR, ValueProperties.none());        
    
    return equityViewDefinition;
  }

  private ViewDefinition getMixedPortfolioViewDefinition() {
    UniqueId portfolioId = getPortfolioId(ExampleMixedPortfolioLoader.PORTFOLIO_NAME);
    ViewDefinition viewDefinition = new ViewDefinition(ExampleMixedPortfolioLoader.PORTFOLIO_NAME + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    
    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    
    final ValueProperties fundingProp = ValueProperties.with(ValuePropertyNames.CURVE, FUNDING).get();
    final ValueProperties forward3MProp = ValueProperties.with(ValuePropertyNames.CURVE, FORWARD_3M).get();
    final ValueProperties forward6MProp = ValueProperties.with(ValuePropertyNames.CURVE, FORWARD_6M).get();
    //BOND
    defaultCalc.addPortfolioRequirementName(BondSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.GBP.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //CAP-FLOOR
    defaultCalc.addPortfolioRequirementName(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE,
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "SABRNoExtrapolation").with("CubeInstrumentType", "SWAPTION_CUBE").get());
    //EQUITY VARIANCE SWAP
    defaultCalc.addPortfolioRequirementName(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with("InstrumentType", "EQUITY_OPTION").get());
    //FRA
    defaultCalc.addPortfolioRequirementName(FRASecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FRASecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FRASecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //FUTURE
    defaultCalc.addPortfolioRequirementName(FutureSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, fundingProp);
    //FX_FORWARD
    defaultCalc.addPortfolioRequirementName(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.CHF.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //FX_OPTION
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with("InstrumentType", "FX_VANILLA_OPTION").get());
    //IRFUTURE_OPTION
    defaultCalc.addPortfolioRequirementName(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with("InstrumentType", "IR_FUTURE_OPTION").get());
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //SWAP
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE, 
        ValueProperties.parse("{CalculationMethod=[SABRNoExtrapolation],CubeInstrumentType=[SWAPTION_CUBE]}"));
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.NZD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward6MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.CHF.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward6MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.DKK.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward6MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward6MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.GBP.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward6MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.JPY.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.CHF.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.DKK.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.GBP.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.JPY.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.NZD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //SWAPTION
    defaultCalc.addPortfolioRequirementName(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE, 
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "SABRNoExtrapolation").with("CubeInstrumentType", "SWAPTION_CUBE").get());
    defaultCalc.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getEquityViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.setMinDeltaCalculationPeriod(500L);
    equityViewDefinition.setMaxDeltaCalculationPeriod(30000L);
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    /*
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.CAPM_BETA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.HISTORICAL_VAR, ValueProperties.none());
    //    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.JENSENS_ALPHA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.SHARPE_RATIO, ValueProperties.none());
    //    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.TOTAL_RISK_ALPHA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.TREYNOR_RATIO, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.WEIGHT, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PNL, ValueProperties.none());
    */
    return equityViewDefinition;
  }

  private UniqueId getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio" + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  private ViewDefinition getSwapViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    ValueProperties defaultProperties = ValueProperties.with("ForwardCurve", "DEFAULT").with("FundingCurve", "DEFAULT").with("Currency", "USD").get();
    defaultCalc.setDefaultProperties(defaultProperties);
    ValueProperties fundingCurveSpecificProperties = ValueProperties.with("Curve", FUNDING).get();
    ValueProperties forwardCurveSpecificProperties = ValueProperties.with("Curve", FORWARD_3M).get();    
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, forwardCurveSpecificProperties);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PAR_RATE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, forwardCurveSpecificProperties);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, forwardCurveSpecificProperties);
    defaultCalc.addSpecificRequirement(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        UniqueId.of("CurrencyISO", "USD"),
        ValueProperties.with("Curve", FUNDING).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalc);

    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition() {
    UniqueId portfolioId = getPortfolioId(ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME);
    ViewDefinition viewDefinition = new ViewDefinition(ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, "Default");
    ValueProperties defaultProperties = ValueProperties.with("ForwardCurve", "DEFAULT").with("FundingCurve", "DEFAULT").with("Currency", "USD").get();
    defaultCalc.setDefaultProperties(defaultProperties);
    
    ValueProperties fundingCurveSpecificProperties = ValueProperties.with("Curve", FUNDING).get();
    ValueProperties forwardCurve3MSpecificProperties = ValueProperties.with("Curve", FORWARD_3M).get();
    ValueProperties forwardCurve6MSpecificProperties = ValueProperties.with("Curve", FORWARD_6M).get();
    
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, forwardCurve3MSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, forwardCurve6MSpecificProperties);

    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, forwardCurve3MSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, forwardCurve6MSpecificProperties);
    for (Currency ccy : ExampleMultiCurrencySwapPortfolioLoader.s_currencies) {
      defaultCalc.addSpecificRequirement(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        UniqueId.of("CurrencyISO", ccy.getCode()),
        ValueProperties.with("Curve", FUNDING).get()));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalc);

    return viewDefinition;
  }

  private ViewDefinition getFXViewDefinition() {
    UniqueId portfolioId = getPortfolioId(ExampleDatabasePopulator.EXAMPLE_FX_PORTFOLIO);
    ViewDefinition viewDefinition = new ViewDefinition(ExampleDatabasePopulator.EXAMPLE_FX_PORTFOLIO + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VALUE_VEGA);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VALUE_VEGA);

    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private void storeViewDefinition(ViewDefinition viewDefinition) {
    ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(viewDefinition.getName());
    configDocument.setValue(viewDefinition);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), configDocument);
  }

}
