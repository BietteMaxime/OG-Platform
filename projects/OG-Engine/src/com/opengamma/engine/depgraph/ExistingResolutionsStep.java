/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Triple;

/* package */final class ExistingResolutionsStep extends FunctionApplicationStep implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(ExistingResolutionsStep.class);

  private ResolutionPump _pump;

  public ExistingResolutionsStep(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> nextFunctions,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, nextFunctions, resolved, resolvedOutput);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Failed to resolve {} from {}", value, this);
    storeFailure(failure);
    synchronized (this) {
      _pump = null;
    }
    // All existing resolutions have been completed, so now try the actual application
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getFunctions(), getResolved(), getResolvedOutput()), context);
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Resolved {} from {}", value, this);
    if (pump != null) {
      synchronized (this) {
        _pump = pump;
      }
      if (!pushResult(context, value, false)) {
        synchronized (this) {
          assert _pump == pump;
          _pump = null;
        }
        context.pump(pump);
      }
    } else {
      if (!pushResult(context, value, true)) {
        context.failed(this, valueRequirement, null);
      }
    }
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    final ResolutionPump pump;
    synchronized (this) {
      pump = _pump;
      _pump = null;
    }
    if (pump != null) {
      s_logger.debug("Pumping underlying delegate");
      context.pump(pump);
    }
  }

  @Override
  protected void onDiscard(final GraphBuildingContext context) {
    final ResolutionPump pump;
    synchronized (this) {
      pump = _pump;
      _pump = null;
    }
    if (pump != null) {
      context.close(pump);
    }
  }

  @Override
  public String toString() {
    return "EXISTING_RESOLUTIONS" + getObjectId();
  }

}
