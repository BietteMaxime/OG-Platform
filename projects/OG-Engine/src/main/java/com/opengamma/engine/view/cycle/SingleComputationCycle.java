/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.cache.MissingMarketDataSentinel;
import com.opengamma.engine.cache.NotCalculatedSentinel;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.exec.DependencyGraphExecutor;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResult;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResultCache;
import com.opengamma.engine.exec.ExecutionResult;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.resource.EngineResource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.tuple.Pair;

/**
 * Holds all data and actions for a single computation pass. The view cycle may be executed at most once.
 * <p>
 * The cycle is thread-safe for readers, for example obtaining the current state or the result, but is only designed for a single executor.
 */
public class SingleComputationCycle implements ViewCycle, EngineResource {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);

  /**
   * The default property used to manipulate all root market data prior to cycle execution.
   */
  public static final String MARKET_DATA_SHIFT_PROPERTY = "MARKET_DATA_SHIFT";

  private enum NodeStateFlag {
    /**
     * Node was executed successfully.
     */
    EXECUTED,
    /**
     * Node was executed but failed.
     */
    FAILED,
    /**
     * Node was not executed because of function blacklist suppression.
     */
    SUPPRESSED;
  };

  // Injected inputs
  private final UniqueId _cycleId;
  private final ViewProcessContext _viewProcessContext;
  private final CompiledViewDefinitionWithGraphs _compiledViewDefinition;
  private final ViewCycleExecutionOptions _executionOptions;
  private final VersionCorrection _versionCorrection;

  private final ComputationResultListener _cycleFragmentResultListener;
  private final DependencyGraphExecutor<?> _dependencyGraphExecutor;
  private final GraphExecutorStatisticsGatherer _statisticsGatherer;

  private volatile ViewCycleState _state = ViewCycleState.AWAITING_EXECUTION;

  private volatile Instant _startTime;
  private volatile Instant _endTime;

  private final Map<DependencyNode, NodeStateFlag> _nodeStates = new ConcurrentHashMap<DependencyNode, NodeStateFlag>();
  private final Map<String, DependencyNodeJobExecutionResultCache> _jobResultCachesByCalculationConfiguration = new ConcurrentHashMap<String, DependencyNodeJobExecutionResultCache>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();

  // Output
  private final InMemoryViewComputationResultModel _resultModel;

  public SingleComputationCycle(final UniqueId cycleId, final ComputationResultListener cycleFragmentResultListener, final ViewProcessContext viewProcessContext,
      final CompiledViewDefinitionWithGraphs compiledViewDefinition, final ViewCycleExecutionOptions executionOptions,
      final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(cycleId, "cycleId");
    ArgumentChecker.notNull(cycleFragmentResultListener, "cycleFragmentResultListener");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.isFalse(versionCorrection.containsLatest(), "versionCorrection must be fully-resolved");
    _cycleId = cycleId;
    _viewProcessContext = viewProcessContext;
    _compiledViewDefinition = compiledViewDefinition;
    _cycleFragmentResultListener = cycleFragmentResultListener;
    _executionOptions = executionOptions;
    _versionCorrection = versionCorrection;
    _resultModel = constructTemplateResultModel();
    _dependencyGraphExecutor = getViewProcessContext().getDependencyGraphExecutorFactory().createExecutor(this);
    _statisticsGatherer = getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(getViewProcessId());
  }

  protected InMemoryViewComputationResultModel constructTemplateResultModel() {
    final InMemoryViewComputationResultModel result = new InMemoryViewComputationResultModel();
    result.setViewCycleId(getCycleId());
    result.setViewProcessId(getViewProcessId());
    result.setViewCycleExecutionOptions(getExecutionOptions());
    result.setVersionCorrection(getVersionCorrection());
    return result;
  }

  //-------------------------------------------------------------------------
  public Instant getValuationTime() {
    return getExecutionOptions().getValuationTime();
  }

  public ViewCycleExecutionOptions getViewCycleExecutionOptions() {
    return _executionOptions;
  }

  /**
   * @return the function initialization identifier
   * @deprecated this needs to go
   */
  @Deprecated
  public long getFunctionInitId() {
    // The cast is only temporary until we've got rid of the function initialisation id
    return ((CompiledViewDefinitionWithGraphsImpl) getCompiledViewDefinition()).getFunctionInitId();
  }

  /**
   * Gets the start time
   * 
   * @return the start time
   */
  public Instant getStartTime() {
    return _startTime;
  }

  /**
   * Gets the end time.
   * 
   * @return the end time
   */
  public Instant getEndTime() {
    return _endTime;
  }

  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return getCompiledViewDefinition().getViewDefinition();
  }

  public DependencyGraphExecutor<?> getDependencyGraphExecutor() {
    return _dependencyGraphExecutor;
  }

  public GraphExecutorStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
  }

  public Map<String, ViewComputationCache> getCachesByCalculationConfiguration() {
    return Collections.unmodifiableMap(_cachesByCalculationConfiguration);
  }

  public ViewProcessContext getViewProcessContext() {
    return _viewProcessContext;
  }

  public Set<String> getAllCalculationConfigurationNames() {
    return new HashSet<String>(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
  }

  //-------------------------------------------------------------------------
  private UniqueId getCycleId() {
    return _cycleId;
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  private ViewCycleExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  protected ExecutionLogModeSource getLogModeSource() {
    return _viewProcessContext.getExecutionLogModeSource();
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _cycleId;
  }

  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessContext.getProcessId();
  }

  @Override
  public ViewCycleState getState() {
    return _state;
  }

  @Override
  public Duration getDuration() {
    final ViewCycleState state = getState();
    if (state == ViewCycleState.AWAITING_EXECUTION || state == ViewCycleState.EXECUTION_INTERRUPTED) {
      return null;
    }
    return Duration.between(getStartTime(), getEndTime() == null ? Instant.now() : getEndTime());
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }

  @Override
  public InMemoryViewComputationResultModel getResultModel() {
    return _resultModel;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(final ComputationCycleQuery query) {
    ArgumentChecker.notNull(query, "query");
    ArgumentChecker.notNull(query.getCalculationConfigurationName(), "calculationConfigurationName");
    ArgumentChecker.notNull(query.getValueSpecifications(), "valueSpecifications");
    final ViewComputationCache cache = getComputationCache(query.getCalculationConfigurationName());
    if (cache == null) {
      throw new DataNotFoundException("No computation cache for calculation configuration '" + query.getCalculationConfigurationName()
          + "' was found.");
    }

    final Collection<Pair<ValueSpecification, Object>> result = cache.getValues(query.getValueSpecifications());
    final ComputationCacheResponse response = new ComputationCacheResponse();
    response.setResults(result);
    return response;
  }

  @Override
  public ComputationResultsResponse queryResults(final ComputationCycleQuery query) {
    final DependencyNodeJobExecutionResultCache jobExecutionResultCache = getJobExecutionResultCache(query.getCalculationConfigurationName());
    if (jobExecutionResultCache == null) {
      return null;
    }
    final ComputationCacheResponse cacheResponse = queryComputationCaches(query);
    final Map<ValueSpecification, ComputedValueResult> resultMap = new HashMap<ValueSpecification, ComputedValueResult>();
    for (final Pair<ValueSpecification, Object> cacheEntry : cacheResponse.getResults()) {
      final ValueSpecification valueSpec = cacheEntry.getFirst();
      final Object cachedValue = cacheEntry.getSecond();
      final Object value = cachedValue != null ? cachedValue : NotCalculatedSentinel.EVALUATION_ERROR;
      resultMap.put(valueSpec, createComputedValueResult(valueSpec, value, jobExecutionResultCache.get(valueSpec)));
    }
    final ComputationResultsResponse response = new ComputationResultsResponse();
    response.setResults(resultMap);
    return response;
  }

  protected static ComputedValueResult createComputedValueResult(final ValueSpecification valueSpec, final Object calculatedValue, final DependencyNodeJobExecutionResult jobExecutionResult) {
    if (jobExecutionResult == null) {
      return new ComputedValueResult(valueSpec, calculatedValue, AggregatedExecutionLog.EMPTY, null, null, null);
    } else {
      final CalculationJobResultItem jobResultItem = jobExecutionResult.getJobResultItem();
      return new ComputedValueResult(valueSpec,
          calculatedValue,
          jobExecutionResult.getAggregatedExecutionLog(),
          jobExecutionResult.getComputeNodeId(),
          jobResultItem.getMissingInputs(),
          jobResultItem.getResult());
    }
  }

  /**
   * Prepares the cycle for execution, organising the caches and copying any values salvaged from a previous cycle.
   * 
   * @param previousCycle the previous cycle from which a delta cycle should be performed, or null to perform a full cycle
   * @param marketDataSnapshot the market data snapshot with which to execute the cycle, not null
   */
  public void preExecute(final SingleComputationCycle previousCycle, final MarketDataSnapshot marketDataSnapshot) {
    if (_state != ViewCycleState.AWAITING_EXECUTION) {
      throw new IllegalStateException("State must be " + ViewCycleState.AWAITING_EXECUTION);
    }
    _startTime = Instant.now();
    _state = ViewCycleState.EXECUTING;

    createAllCaches();
    prepareInputs(marketDataSnapshot);

    if (previousCycle != null) {
      computeDelta(previousCycle);
    }
  }

  /**
   * Completes the execution cycle.
   */
  public void postExecute() {
    completeResultModel();
    _state = ViewCycleState.EXECUTED;
  }

  // REVIEW jonathan 2011-03-18 -- The following comment should be given some sort of 'listed' status for preservation :-)
  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.

  /**
   * Synchronously runs the cycle.
   * 
   * @param calcJobResultExecutorService the executor to use for streaming calculation job result consumption, not null
   * @throws InterruptedException if the thread is interrupted while waiting for the computation cycle to complete. Execution of any outstanding jobs will be cancelled, but {@link #release()} still
   *           must be called.
   */
  public void execute(final ExecutorService calcJobResultExecutorService) throws InterruptedException {
    final BlockingQueue<ExecutionResult> calcJobResultQueue = new LinkedBlockingQueue<ExecutionResult>();
    final CalculationJobResultStreamConsumer calculationJobResultStreamConsumer = new CalculationJobResultStreamConsumer(calcJobResultQueue, this);
    Future<?> resultStreamConsumerJobInProgress;
    try {
      resultStreamConsumerJobInProgress = calcJobResultExecutorService.submit(calculationJobResultStreamConsumer);
      final LinkedList<Future<?>> futures = new LinkedList<Future<?>>();
      for (final String calcConfigurationName : getAllCalculationConfigurationNames()) {
        s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
        final DependencyGraph depGraph;
        depGraph = createExecutableDependencyGraph(calcConfigurationName);
        s_logger.info("Submitting {} for execution by {}", depGraph, getDependencyGraphExecutor());
        final Future<?> future = getDependencyGraphExecutor().execute(depGraph, calcJobResultQueue, _statisticsGatherer, getLogModeSource());
        futures.add(future);
      }
      while (!futures.isEmpty()) {
        final Future<?> future = futures.poll();
        try {
          future.get(5, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
          s_logger.info("Waiting for " + future);
          futures.add(future);
        } catch (final InterruptedException e) {
          Thread.interrupted();
          // Cancel all outstanding jobs to free up resources
          future.cancel(true);
          for (final Future<?> incompleteFuture : futures) {
            incompleteFuture.cancel(true);
          }
          _state = ViewCycleState.EXECUTION_INTERRUPTED;
          s_logger.info("Execution interrupted before completion.");
          throw e;
        } catch (final ExecutionException e) {
          s_logger.error("Unable to execute dependency graph", e);
          // Should we be swallowing this or not?
          throw new OpenGammaRuntimeException("Unable to execute dependency graph", e);
        }
      }
      _endTime = Instant.now();
    } finally {
      calculationJobResultStreamConsumer.terminate();
    }
    // Wait for calculationJobResultStreamConsumer to finish
    try {
      if (resultStreamConsumerJobInProgress != null) {
        resultStreamConsumerJobInProgress.get();
      }
    } catch (final ExecutionException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Fetches the override operation to apply to market data in the given configuration. If there is no operation to apply, returns null.
   * 
   * @return the shift operation, or null for non
   */
  private OverrideOperation getCacheMarketDataOperation(ViewCalculationConfiguration calcConfig) {
    OverrideOperationCompiler compiler = null;
    ComputationTargetResolver.AtVersionCorrection resolver = null;
    final Set<String> marketDataShift = calcConfig.getDefaultProperties().getValues(MARKET_DATA_SHIFT_PROPERTY);
    OverrideOperation operation = null;
    if (marketDataShift != null) {
      if (marketDataShift.size() != 1) {
        // This doesn't really mean much
        s_logger.error("Market data shift for {} not valid - {}", calcConfig.getName(), marketDataShift);
      } else {
        if (compiler == null) {
          compiler = getViewProcessContext().getOverrideOperationCompiler();
          resolver = getViewProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().atVersionCorrection(getVersionCorrection());
        }
        final String shiftExpr = marketDataShift.iterator().next();
        try {
          operation = compiler.compile(shiftExpr, resolver);
        } catch (final IllegalArgumentException e) {
          s_logger.error("Market data shift for  {} not valid - {}", calcConfig.getName(), shiftExpr);
          s_logger.info("Invalid market data shift", e);
        }
      }
    }
    return operation;
  }

  private static final DefaultAggregatedExecutionLog MARKET_DATA_LOG = DefaultAggregatedExecutionLog.indicatorLogMode(EnumSet.of(LogLevel.WARN));

  private void prepareInputs(final MarketDataSnapshot snapshot) {
    int missingMarketData = 0;
    final Set<ValueSpecification> allRequiredMarketData = getCompiledViewDefinition().getMarketDataRequirements();
    s_logger.debug("Populating {} market data items using snapshot {}", allRequiredMarketData.size(), snapshot);
    final InMemoryViewComputationResultModel fragmentResultModel = constructTemplateResultModel();
    final InMemoryViewComputationResultModel fullResultModel = getResultModel();
    final Map<ValueSpecification, Object> marketDataValues = snapshot.query(allRequiredMarketData);
    final ResultModelDefinition resultModel = getViewDefinition().getResultModelDefinition();
    for (CompiledViewCalculationConfiguration calcConfig : getCompiledViewDefinition().getCompiledCalculationConfigurations()) {
      final OverrideOperation operation = getCacheMarketDataOperation(getViewDefinition().getCalculationConfiguration(calcConfig.getName()));
      final ViewComputationCache cache = getComputationCache(calcConfig.getName());
      final Collection<ValueSpecification> marketDataRequirements = calcConfig.getMarketDataRequirements();
      final Set<ValueSpecification> terminalOutputs = calcConfig.getTerminalOutputSpecifications().keySet();
      final Collection<ComputedValueResult> valuesToLoad = new ArrayList<ComputedValueResult>(marketDataRequirements.size());
      for (ValueSpecification marketDataSpec : marketDataRequirements) {
        Object marketDataValue = marketDataValues.get(marketDataSpec);
        ComputedValueResult computedValueResult;
        if (operation != null) {
          if (marketDataValue != null) {
            marketDataValue = operation.apply(marketDataSpec.toRequirementSpecification(), marketDataValue);
            if (marketDataValue == null) {
              s_logger.debug("Market data {} discarded by override operation", marketDataSpec);
            }
          }
        }
        if (marketDataValue == null) {
          s_logger.debug("Unable to load market data value for {} from snapshot {}", marketDataSpec, getValuationTime());
          missingMarketData++;
          // TODO provide elevated logs if requested from market data providers
          computedValueResult = new ComputedValueResult(marketDataSpec, MissingMarketDataSentinel.getInstance(), MARKET_DATA_LOG);
        } else {
          computedValueResult = new ComputedValueResult(marketDataSpec, marketDataValue, AggregatedExecutionLog.EMPTY);
          fragmentResultModel.addMarketData(computedValueResult);
          fullResultModel.addMarketData(computedValueResult);
        }
        if (terminalOutputs.contains(marketDataSpec) && (resultModel.getOutputMode(marketDataSpec.getTargetSpecification().getType()) != ResultOutputMode.NONE)) {
          fragmentResultModel.addValue(calcConfig.getName(), computedValueResult);
          fullResultModel.addValue(calcConfig.getName(), computedValueResult);
        }
        valuesToLoad.add(computedValueResult);
      }
      if (!valuesToLoad.isEmpty()) {
        cache.putSharedValues(valuesToLoad);
      }
    }
    if (missingMarketData > 0) {
      // REVIEW jonathan 2012-11-01 -- probably need a cycle-level execution log for things like this
      s_logger.info("Missing {} market data elements", missingMarketData);
    }
    notifyFragmentCompleted(fragmentResultModel);
  }

  /**
   * Ensures that a computation cache exists for for each calculation configuration.
   */
  private void createAllCaches() {
    for (final String calcConfigurationName : getAllCalculationConfigurationNames()) {
      final ViewComputationCache cache = getViewProcessContext().getComputationCacheSource()
          .getCache(getUniqueId(), calcConfigurationName);
      _cachesByCalculationConfiguration.put(calcConfigurationName, cache);
      _jobResultCachesByCalculationConfiguration.put(calcConfigurationName, new DependencyNodeJobExecutionResultCache());
    }
  }

  protected ViewComputationCache getComputationCache(final String calcConfigName) {
    return _cachesByCalculationConfiguration.get(calcConfigName);
  }

  /**
   * Determine which live data inputs have changed between iterations, and:
   * <ul>
   * <li>Copy over all values that can be demonstrated to be the same from the previous iteration (because no input has changed)
   * <li>Only recompute the values that could have changed based on live data inputs
   * </ul>
   * 
   * @param previousCycle Previous iteration. It must not have been cleaned yet ({@link #releaseResources()}).
   */
  private void computeDelta(final SingleComputationCycle previousCycle) {
    if (previousCycle.getState() != ViewCycleState.EXECUTED) {
      throw new IllegalArgumentException("State of previous cycle must be " + ViewCycleState.EXECUTED);
    }
    final InMemoryViewComputationResultModel fragmentResultModel = constructTemplateResultModel();
    final InMemoryViewComputationResultModel fullResultModel = getResultModel();
    for (final DependencyGraphExplorer depGraphExplorer : getCompiledViewDefinition().getDependencyGraphExplorers()) {
      final DependencyGraph depGraph = depGraphExplorer.getWholeGraph();
      final ViewComputationCache cache = getComputationCache(depGraph.getCalculationConfigurationName());
      final ViewComputationCache previousCache = previousCycle.getComputationCache(depGraph.getCalculationConfigurationName());
      final DependencyNodeJobExecutionResultCache jobExecutionResultCache = getJobExecutionResultCache(depGraph.getCalculationConfigurationName());
      final DependencyNodeJobExecutionResultCache previousJobExecutionResultCache = previousCycle.getJobExecutionResultCache(depGraph.getCalculationConfigurationName());
      final LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph, cache, previousCache);
      deltaCalculator.computeDelta();
      s_logger.info("Computed delta for calculation configuration '{}'. {} nodes out of {} require recomputation.",
          new Object[] {depGraph.getCalculationConfigurationName(), deltaCalculator.getChangedNodes().size(), depGraph.getSize() });
      final Collection<ValueSpecification> specsToCopy = new LinkedList<ValueSpecification>();
      final Collection<ComputedValue> errors = new LinkedList<ComputedValue>();
      for (final DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        if (isMarketDataNode(unchangedNode)) {
          // Market data is already in the cache, so don't need to copy it across again
          continue;
        }
        final DependencyNodeJobExecutionResult previousExecutionResult = previousJobExecutionResultCache.find(unchangedNode.getOutputValues());
        if (getLogModeSource().getLogMode(unchangedNode) == ExecutionLogMode.FULL
            && (previousExecutionResult == null || previousExecutionResult.getJobResultItem().getExecutionLog().getEvents() == null)) {
          // Need to rerun calculation to collect logs, so cannot reuse
          continue;
        }
        final NodeStateFlag nodeState = previousCycle.getNodeState(unchangedNode);
        if (nodeState != null) {
          setNodeState(unchangedNode, nodeState);
          if (nodeState == NodeStateFlag.EXECUTED) {
            specsToCopy.addAll(unchangedNode.getOutputValues());
          } else {
            for (final ValueSpecification outputValue : unchangedNode.getOutputValues()) {
              errors.add(new ComputedValue(outputValue, NotCalculatedSentinel.SUPPRESSED));
            }
          }
        }
      }
      if (!specsToCopy.isEmpty()) {
        final ComputationCycleQuery reusableResultsQuery = new ComputationCycleQuery();
        reusableResultsQuery.setCalculationConfigurationName(depGraph.getCalculationConfigurationName());
        reusableResultsQuery.setValueSpecifications(specsToCopy);
        final ComputationResultsResponse reusableResultsQueryResponse = previousCycle.queryResults(reusableResultsQuery);
        final Map<ValueSpecification, ComputedValueResult> resultsToReuse = reusableResultsQueryResponse.getResults();
        final Collection<ComputedValue> newValues = new ArrayList<ComputedValue>(resultsToReuse.size());
        for (final ComputedValueResult computedValueResult : resultsToReuse.values()) {
          final ValueSpecification valueSpec = computedValueResult.getSpecification();
          if (depGraph.getTerminalOutputSpecifications().contains(valueSpec)
              && getViewDefinition().getResultModelDefinition().shouldOutputResult(valueSpec, depGraph)) {
            fragmentResultModel.addValue(depGraph.getCalculationConfigurationName(), computedValueResult);
            fullResultModel.addValue(depGraph.getCalculationConfigurationName(), computedValueResult);
          }
          final Object previousValue = computedValueResult.getValue() != null ? computedValueResult.getValue() : NotCalculatedSentinel.EVALUATION_ERROR;
          newValues.add(new ComputedValue(valueSpec, previousValue));
          final DependencyNodeJobExecutionResult previousDependencyNodeJobExecutionResult = previousJobExecutionResultCache.get(valueSpec);
          if (previousDependencyNodeJobExecutionResult != null) {
            jobExecutionResultCache.put(valueSpec, previousDependencyNodeJobExecutionResult);
          }
        }
        cache.putSharedValues(newValues);
      }
      if (!errors.isEmpty()) {
        cache.putSharedValues(errors);
      }
    }
    if (!fragmentResultModel.getAllResults().isEmpty()) {
      notifyFragmentCompleted(fragmentResultModel);
    }
  }

  private void completeResultModel() {
    getResultModel().setCalculationTime(Instant.now());
    getResultModel().setCalculationDuration(getDuration());
  }

  protected void notifyFragmentCompleted(final ViewComputationResultModel fragmentResult) {
    try {
      _cycleFragmentResultListener.resultAvailable(fragmentResult);
    } catch (final Exception e) {
      s_logger.warn("Error notifying listener of cycle fragment completion", e);
    }
  }

  /**
   * Returns the dependency graph used by this cycle for the given calculation configuration.
   * 
   * @param calcConfName calculation configuration name
   * @return the dependency graph
   */
  protected DependencyGraph getDependencyGraph(final String calcConfName) {
    return getCompiledViewDefinition().getDependencyGraphExplorer(calcConfName).getWholeGraph();
  }

  private boolean isMarketDataNode(final DependencyNode node) {
    return node.getFunction().getFunction() instanceof MarketDataSourcingFunction;
  }

  /**
   * Creates a subset of the dependency graph for execution. This will only include nodes that do are not dummy ones to source market data, have been considered executed by a delta from the previous
   * cycle, or are being suppressed by the execution blacklist. Note that this will update the cache with synthetic output values from suppressed nodes and alter the execution state of any nodes not
   * in the resultant subgraph.
   * 
   * @param calcConfName calculation configuration name
   * @return a dependency graph with any nodes which have already been satisfied filtered out, not null See {@link #computeDelta} and how it calls {@link #markExecuted}.
   */
  private DependencyGraph createExecutableDependencyGraph(final String calcConfName) {
    final FunctionBlacklistQuery blacklist = getViewProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getGraphExecutionBlacklist();
    return getDependencyGraph(calcConfName).subGraph(new DependencyNodeFilter() {
      @Override
      public boolean accept(final DependencyNode node) {
        // Market data functions must not be executed
        if (isMarketDataNode(node)) {
          markExecuted(node);
          return false;
        }
        // Everything else should be executed unless it was copied from a previous cycle or matched by the blacklist
        final NodeStateFlag state = getNodeState(node);
        if (state != null) {
          return false;
        }
        if (blacklist.isBlacklisted(node)) {
          markSuppressed(node);
          // If the node is suppressed, put values into the cache to indicate this
          final Set<ValueSpecification> outputs = node.getOutputValues();
          final ViewComputationCache cache = getComputationCache(calcConfName);
          if (outputs.size() == 1) {
            cache.putSharedValue(new ComputedValue(outputs.iterator().next(), NotCalculatedSentinel.SUPPRESSED));
          } else {
            final Collection<ComputedValue> errors = new ArrayList<ComputedValue>(outputs.size());
            for (final ValueSpecification output : outputs) {
              errors.add(new ComputedValue(output, NotCalculatedSentinel.SUPPRESSED));
            }
            cache.putSharedValues(errors);
          }
          return false;
        }
        return true;
      }
    });
  }

  //--------------------------------------------------------------------------
  @Override
  public void release() {
    if (getState() == ViewCycleState.DESTROYED) {
      throw new IllegalStateException("View cycle " + getUniqueId() + " has already been released");
    }
    if (getViewDefinition().isDumpComputationCacheToDisk()) {
      dumpComputationCachesToDisk();
    }
    getViewProcessContext().getComputationCacheSource().releaseCaches(getUniqueId());
    _state = ViewCycleState.DESTROYED;
  }

  public void dumpComputationCachesToDisk() {
    for (final String calcConfigurationName : getAllCalculationConfigurationNames()) {
      final DependencyGraph depGraph = getDependencyGraph(calcConfigurationName);
      final ViewComputationCache computationCache = getComputationCache(calcConfigurationName);

      final TreeMap<String, Object> key2Value = new TreeMap<String, Object>();
      for (final ValueSpecification outputSpec : depGraph.getOutputSpecifications()) {
        final Object value = computationCache.getValue(outputSpec);
        key2Value.put(outputSpec.toString(), value);
      }

      try {
        final File file = File.createTempFile("computation-cache-" + calcConfigurationName + "-", ".txt");
        s_logger.info("Dumping cache for calc conf " + calcConfigurationName + " to " + file.getAbsolutePath());
        final FileWriter writer = new FileWriter(file);
        writer.write(key2Value.toString());
        writer.close();
      } catch (final IOException e) {
        throw new RuntimeException("Writing cache to file failed", e);
      }
    }
  }

  private NodeStateFlag getNodeState(final DependencyNode node) {
    return _nodeStates.get(node);
  }

  private void setNodeState(final DependencyNode node, final NodeStateFlag state) {
    _nodeStates.put(node, state);
  }

  public void markExecuted(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.EXECUTED);
  }

  public void markFailed(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.FAILED);
  }

  private void markSuppressed(final DependencyNode node) {
    setNodeState(node, NodeStateFlag.SUPPRESSED);
  }

  protected DependencyNodeJobExecutionResultCache getJobExecutionResultCache(final String calcConfigName) {
    return _jobResultCachesByCalculationConfiguration.get(calcConfigName);
  }

}
