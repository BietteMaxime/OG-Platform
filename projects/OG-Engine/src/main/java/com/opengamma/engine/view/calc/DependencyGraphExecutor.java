/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Queue;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.ExecutionLogModeSource;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;

/**
 * Evaluates a dependency graph.
 * 
 * @param <T> Type of return information from the executor
 */
public interface DependencyGraphExecutor<T> {

  /**
   * Evaluates a dependency graph.
   * 
   * @param graph  a full graph or a subgraph. A subgraph may have some nodes whose child nodes are NOT part of that
   *               graph. The assumption is that such nodes have already been evaluated and their values can already be
   *               found in the shared computation cache.
   * @param statistics  a callback object to which details about the evaluation should be reported
   * @param executionResultQueue  a queue in to which the executor should enqueues individual calculation job results
   * @param logModeSource  the source of log mode information for the outputs, not null
   * @return An object you can call get() on to wait for completion
   */
  Future<T> execute(DependencyGraph graph, Queue<ExecutionResult> executionResultQueue, GraphExecutorStatisticsGatherer statistics, ExecutionLogModeSource logModeSource);

}
