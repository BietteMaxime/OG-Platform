/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Encapsulates a set of transformations to apply to market data when a calculation cycle is run.
 */
public class Scenario {

  /** Manipulators keyed by the selectors for the items they apply to. */
  private final ListMultimap<DistinctMarketDataSelector, StructureManipulator<?>> _manipulations = ArrayListMultimap.create();

  /** This scenario's name. */
  private final String _name;
  /** Calc configs to which this scenario will be applied, null will match any config. */
  private Set<String> _calcConfigNames;
  /** Valuation time of this scenario's calculation cycle. */
  private Instant _valuationTime = Instant.now();
  /** Version correction used by the resolver. */
  private VersionCorrection _resolverVersionCorrection = VersionCorrection.LATEST;

  /**
   * Creates a new scenario with a calcuation configuration name of "Default", valuation time of {@code Instant.now()}
   * and resolver version correction of {@link VersionCorrection#LATEST}.
   * @param name The scenario name, not null
   */
  public Scenario(String name) {
    ArgumentChecker.notEmpty(name, "name"); // should this be allowed to be null? should there be a no-arg constructor?
    _name = name;
  }

  /* package */ Scenario(String name,
                         Set<String> calcConfigNames,
                         Instant valuationTime,
                         VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _name = name;
    _calcConfigNames = calcConfigNames;
    _valuationTime = valuationTime;
    _resolverVersionCorrection = resolverVersionCorrection;
  }

  /**
   * @return A object for specifying which curves should be transformed
   */
  public YieldCurveSelector.Builder curve() {
    return new YieldCurveSelector.Builder(this);
  }

  /**
   * @return An object for specifying which market data points should be transformed
   */
  public PointSelector.Builder marketDataPoint() {
    return new PointSelector.Builder(this);
  }

  /**
   * @return An object for specifying which volatility surfaces should be transformed
   */
  public VolatilitySurfaceSelector.Builder surface() {
    return new VolatilitySurfaceSelector.Builder(this);
  }

  /**
   * Updates this scenario to apply to the specified calculation configuration.
   * @param configNames The calculation configuration name
   * @return The modified scenario
   */
  public Scenario calculationConfigurations(String... configNames) {
    ArgumentChecker.notEmpty(configNames, "configName");
    _calcConfigNames = ImmutableSet.copyOf(configNames);
    return this;
  }

  /**
   * Updates this scenario to use the specified valuation time.
   * @param valuationTime The valuation time
   * @return The modified scenario
   */
  public Scenario valuationTime(Instant valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _valuationTime = valuationTime;
    return this;
  }

  /**
   * Updates this scenario to use the specified version correction in the resolver.
   * @param resolverVersionCorrection The resolver version correction
   * @return The modified scenario
   */
  public Scenario resolverVersionCorrection(VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    _resolverVersionCorrection = resolverVersionCorrection;
    return this;
  }

  /**
   * @return A {@link ScenarioDefinition} created from this scenario's selectors and manipulators
   */
  @SuppressWarnings("unchecked")
  public ScenarioDefinition createDefinition() {
    Map<DistinctMarketDataSelector, FunctionParameters> params = Maps.newHashMapWithExpectedSize(_manipulations.size());
    for (Map.Entry<DistinctMarketDataSelector, Collection<StructureManipulator<?>>> entry : _manipulations.asMap().entrySet()) {
      DistinctMarketDataSelector selector = entry.getKey();
      // ListMultimap always has Lists as entries even if the signature doesn't say so
      List<StructureManipulator<?>> manipulators = (List<StructureManipulator<?>>) entry.getValue();
      CompositeStructureManipulator compositeManipulator = new CompositeStructureManipulator(Object.class, manipulators);
      SimpleFunctionParameters functionParameters = new SimpleFunctionParameters();
      functionParameters.setValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME, compositeManipulator);
      params.put(selector, functionParameters);
    }
    return new ScenarioDefinition(_name, params);
  }

  /* package */ void add(DistinctMarketDataSelector selector, StructureManipulator<?> manipulator) {
    _manipulations.put(selector, manipulator);
  }

  /* package */ Instant getValuationTime() {
    return _valuationTime;
  }

  /* package */ VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  /* package */ Set<String> getCalcConfigNames() {
    return _calcConfigNames;
  }

  /**
   * @return The scenario name, not null
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return "Scenario [" +
        "_name='" + _name + "'" +
        ", _calcConfigNames=" + _calcConfigNames +
        ", _valuationTime=" + _valuationTime +
        ", _resolverVersionCorrection=" + _resolverVersionCorrection +
        ", _manipulations=" + _manipulations +
        "]";
  }
}
