/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeToDefinitionConverter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MulticurveProviderDiscountingFunction extends AbstractFunction {
  private static final String CALCULATION_METHOD = "Discounting"; //TODO move me
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private final String _configurationName;
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  public MulticurveProviderDiscountingFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  //TODO need to deal with exogenous curves
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final CurveConstructionConfigurationSource curveConfigurationSource = new ConfigDBCurveConstructionConfigurationSource(configSource);
    final Instant versionTime = atZDT.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toInstant();
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration = curveConfigurationSource.getCurveConstructionConfiguration(_configurationName,
        VersionCorrection.of(versionTime, versionTime));
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final CurveNodeToDefinitionConverter curveNodeToDefinitionConverter = new CurveNodeToDefinitionConverter(conventionSource, holidaySource, regionSource);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        ValueProperties bundleProperties = null;
        for (final ValueRequirement desiredValue : desiredValues) {
          if (desiredValue.getValueName().equals(ValueRequirementNames.CURVE_BUNDLE)) {
            bundleProperties = desiredValue.getConstraints();
            break;
          } else if (desiredValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            bundleProperties = desiredValue.getConstraints()
                .withoutAny(CURVE);
            break;
          }
        }
        if (bundleProperties == null) {
          throw new OpenGammaRuntimeException("Could not get bundle properties from desired values");
        }
        final double absoluteTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)));
        final double relativeTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)));
        final int maxIterations = Integer.parseInt(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)));
        final MulticurveDiscountBuildingRepository builder = new MulticurveDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
        final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> pair = getCurves(curveConstructionConfiguration, inputs, now, builder);
        final ValueSpecification bundleSpec = new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, bundleProperties);
        final Set<ComputedValue> result = new HashSet<>();
        result.add(new ComputedValue(bundleSpec, pair.getFirst()));
        for (final String curveName : curveNames) {
          final ValueProperties curveProperties = bundleProperties.copy()
              .with(CURVE, curveName)
              .get();
          final ValueSpecification curveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, pair.getFirst().getCurve(curveName)));
        }
        return result;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Set<ValueSpecification> results = new HashSet<>();
        final ValueProperties properties = getBundleProperties();
        for (final String curveName : curveNames) {
          final ValueProperties curveProperties = getCurveProperties(curveName);
          results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
        }
        results.add(new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
        return results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> rootFinderAbsoluteTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
        if (rootFinderAbsoluteTolerance == null || rootFinderAbsoluteTolerance.size() != 1) {
          return null;
        }
        final Set<String> rootFinderRelativeTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
        if (rootFinderRelativeTolerance == null || rootFinderRelativeTolerance.size() != 1) {
          return null;
        }
        final Set<String> maxIterations = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
        if (maxIterations == null || maxIterations.size() != 1) {
          return null;
        }
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final String curveName : curveNames) {
          final ValueProperties properties = ValueProperties.builder()
              .with(CURVE, curveName)
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
        }
        return requirements;
      }

      @SuppressWarnings("synthetic-access")
      private ValueProperties getCurveProperties(final String curveName) {
        return createValueProperties()
            .with(CURVE, curveName)
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .get();
      }

      @SuppressWarnings("synthetic-access")
      private ValueProperties getBundleProperties() {
        return createValueProperties()
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .get();
      }

      @SuppressWarnings("synthetic-access")
      private Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurves(final CurveConstructionConfiguration constructionConfiguration,
          final FunctionInputs inputs, final ZonedDateTime now, final MulticurveDiscountBuildingRepository builder) {
        final int nGroups = constructionConfiguration.getCurveGroups().size();
        final InstrumentDerivative[][][] definitions = new InstrumentDerivative[nGroups][][];
        final GeneratorYDCurve[][] curveGenerators = new GeneratorYDCurve[nGroups][];
        final String[][] curves = new String[nGroups][];
        final double[][] parameterGuess = new double[nGroups][];
        final MulticurveProviderDiscount knownData = new MulticurveProviderDiscount(); //TODO handle exogenous curves
        final LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
        final LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
        final LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
        //TODO comparator to sort groups by order
        int i = 0;
        for (final CurveGroupConfiguration group : constructionConfiguration.getCurveGroups()) {
          int j = 0;
          final int nCurves = group.getCurveTypes().size();
          definitions[i] = new InstrumentDerivative[nCurves][];
          curveGenerators[i] = new GeneratorYDCurve[nCurves];
          curves[i] = new String[nCurves];
          parameterGuess[i] = new double[nCurves];
          final DoubleArrayList parameterGuessForCurves = new DoubleArrayList();
          for (final CurveTypeConfiguration type : group.getCurveTypes()) {
            final String curveName = type.getName();
            final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
            final Object snapshotObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
            if (snapshotObject == null) {
              throw new OpenGammaRuntimeException("Could not get market data for " + curveName);
            }
            final Object specificationObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
            if (specificationObject == null) {
              throw new OpenGammaRuntimeException("Could not get curve specification for " + curveName);
            }
            final Object definitionObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
            if (definitionObject == null) {
              throw new OpenGammaRuntimeException("Could not get curve definition for " + curveName);
            }
            final CurveSpecification specification = (CurveSpecification) specificationObject;
            final CurveDefinition definition = (CurveDefinition) definitionObject;
            final SnapshotDataBundle snapshot = (SnapshotDataBundle) snapshotObject;
            final int nNodes = specification.getNodes().size();
            final InstrumentDerivative[] derivativesForCurve = new InstrumentDerivative[nNodes];
            final double[] marketDataForCurve = new double[nNodes];
            int k = 0;
            final Set<IborIndex> uniqueIborIndices = new HashSet<>();
            final Set<IndexON> uniqueOvernightIndices = new HashSet<>();
            for (final CurveNodeWithIdentifier node : specification.getNodes()) {
              final Double marketData = snapshot.getDataPoint(node.getIdentifier());
              if (marketData == null) {
                throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
              }
              marketDataForCurve[k] = marketData;
              parameterGuessForCurves.add(marketData);
              final InstrumentDefinition<?> definitionForNode = curveNodeToDefinitionConverter.getDefinitionForNode(node.getCurveNode(), node.getIdentifier(), now, snapshot);
              uniqueIborIndices.addAll(definitionForNode.accept(IborIndexVisitor.getInstance()));
              uniqueOvernightIndices.addAll(definitionForNode.accept(OvernightIndexVisitor.getInstance()));
              derivativesForCurve[k++] = definitionForNode.toDerivative(now, new String[] {"", ""});
            }
            definitions[i][j] = derivativesForCurve;
            curveGenerators[i][j] = getGenerator(definition);
            curves[i][j] = curveName;
            parameterGuess[i] = parameterGuessForCurves.toDoubleArray();
            final IborIndex[] iborIndex = uniqueIborIndices.toArray(new IborIndex[uniqueIborIndices.size()]);
            final IndexON[] overnightIndex = uniqueOvernightIndices.toArray(new IndexON[uniqueOvernightIndices.size()]);
            forwardIborMap.put(curveName, iborIndex);
            forwardONMap.put(curveName, overnightIndex);
            if (type instanceof DiscountingCurveTypeConfiguration) {
              discountingMap.put(curveName, Currency.of(((DiscountingCurveTypeConfiguration) type).getCode()));
            }
            j++;
          }
          i++;
        }
        return builder.makeCurvesFromDerivatives(definitions, curveGenerators, curves, parameterGuess, knownData, discountingMap, forwardIborMap, forwardONMap, PSMQC, PSMQCSC);
      }

      private GeneratorYDCurve getGenerator(final CurveDefinition definition) {
        if (definition instanceof InterpolatedCurveDefinition) {
          final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
          final String interpolatorName = interpolatedDefinition.getInterpolatorName();
          final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
          final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
          final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
          return new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, interpolator);
        }
        throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
      }
    };
  }


}
