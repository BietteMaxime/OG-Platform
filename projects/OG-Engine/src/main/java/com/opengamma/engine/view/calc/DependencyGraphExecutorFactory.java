/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 *
 * @param <T> return type of the executor
 */
public interface DependencyGraphExecutorFactory<T> {
  
  DependencyGraphExecutor<T> createExecutor(SingleComputationCycle cycle);

}
