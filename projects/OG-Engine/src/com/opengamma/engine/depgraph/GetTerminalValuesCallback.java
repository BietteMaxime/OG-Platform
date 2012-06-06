/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Handles callback notifications of terminal values to populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);
  private static final AtomicInteger s_nextDebugId = new AtomicInteger();

  private interface DependencyNodeCallback {

    void node(DependencyNode node);

  }

  private interface DependencyNodeProducer {

    void getNode(DependencyNodeCallback callback);

  }

  private final Map<ValueSpecification, DependencyNode> _spec2Node = new HashMap<ValueSpecification, DependencyNode>();
  private final Map<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNodeProducer>>> _func2target2nodes =
      new HashMap<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNodeProducer>>>();
  private final Collection<DependencyNode> _graphNodes = new ArrayList<DependencyNode>();
  private final Map<ValueRequirement, ValueSpecification> _resolvedValues = new HashMap<ValueRequirement, ValueSpecification>();
  private ResolutionFailureVisitor<?> _failureVisitor;

  public GetTerminalValuesCallback(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.error("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.assertValueRequirement(value);
      if (_failureVisitor != null) {
        failureImpl.accept(_failureVisitor);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failureImpl));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  private synchronized Set<DependencyNodeProducer> getOrCreateNodes(final ParameterizedFunction function, final ComputationTargetSpecification target) {
    Map<ComputationTargetSpecification, Set<DependencyNodeProducer>> target2nodes = _func2target2nodes.get(function);
    if (target2nodes == null) {
      target2nodes = new HashMap<ComputationTargetSpecification, Set<DependencyNodeProducer>>();
      _func2target2nodes.put(function, target2nodes);
    }
    Set<DependencyNodeProducer> nodes = target2nodes.get(target);
    if (nodes == null) {
      nodes = new HashSet<DependencyNodeProducer>();
      target2nodes.put(target, nodes);
    }
    return nodes;
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    nextA: for (ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      boolean mismatch = false;
      for (ValueSpecification b : bs) {
        if (aName == b.getValueName()) {
          // Match the name; check the constraints
          if (aProperties.isSatisfiedBy(b.getProperties())) {
            continue nextA;
          } else {
            // Mismatch found
            mismatch = true;
          }
        }
      }
      if (mismatch) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether the union of value specifications would be mismatched; that is the two sets can't be composed.
   * Given the intersection of common value names, the properties must be mutually compatible.
   * 
   * @param as the first set of values, not null
   * @param bs the second set of values, not null
   * @return true if the values can't be composed, false if they can
   */
  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  private final class NodeInputProduction {

    private final ResolvedValue _resolvedValue;
    private final DependencyNodeCallback _result;
    private final boolean _isNew;
    private int _count = 2;
    private DependencyNode _node;

    public NodeInputProduction(final ResolvedValue resolvedValue, final DependencyNode node, final DependencyNodeCallback result, final boolean isNew) {
      _resolvedValue = resolvedValue;
      _node = node;
      _result = result;
      _isNew = isNew;
    }

    public synchronized void addProducer() {
      _count++;
    }

    public void failed() {
      final boolean done;
      synchronized (this) {
        _node = null;
        done = (--_count == 0);
      }
      if (done) {
        _result.node(null);
      }
    }

    public void completed() {
      final boolean done;
      final DependencyNode node;
      synchronized (this) {
        node = _node;
        done = (--_count == 0);
      }
      if (done) {
        if (node != null) {
          nodeProduced(_resolvedValue, node, _result, _isNew);
        } else {
          s_logger.warn("Couldn't produce node for {}", _resolvedValue);
          _result.node(null);
        }
      }
    }

  }

  private void nodeProduced(final ResolvedValue resolvedValue, final DependencyNode node, final DependencyNodeCallback result, final boolean isNew) {
    if (isNew) {
      synchronized (this) {
        s_logger.debug("Adding {} to graph set", node);
        // [PLAT-346] Here is a good spot to tackle PLAT-346; which node's outputs do we discard if there are multiple
        // productions for a given value specification?
        for (ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
          DependencyNode existing = _spec2Node.get(valueSpecification);
          if (existing == null) {
            _spec2Node.put(valueSpecification, node);
          } else {
            // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
            node.removeOutputValue(valueSpecification);
          }
        }
        _graphNodes.add(node);
      }
    }
    result.node(node);
  }

  private final class FindExistingNodes implements DependencyNodeCallback, DependencyNodeProducer {

    private final ResolvedValue _resolvedValue;
    private int _pending;
    private DependencyNode _found;
    private DependencyNodeCallback _callback;

    public FindExistingNodes(final ResolvedValue resolvedValue, final Set<DependencyNodeProducer> nodes) {
      _resolvedValue = resolvedValue;
      synchronized (nodes) {
        _pending = nodes.size();
        for (DependencyNodeProducer node : nodes) {
          node.getNode(this);
        }
      }
    }

    public synchronized boolean isDeferred() {
      assert _callback == null;
      return _pending > 0;
    }

    public synchronized DependencyNode getNode() {
      assert _pending == 0;
      assert _callback == null;
      return _found;
    }

    @Override
    public void node(final DependencyNode node) {
      DependencyNodeCallback callback = null;
      synchronized (this) {
        _pending--;
        if (_found == null) {
          if (node != null) {
            // Any node that is candidate for reuse can't be considered concurrently as the composition
            // may become mutually exclusive.
            synchronized (node) {
              final Set<ValueSpecification> outputValues = node.getOutputValues();
              if (mismatchUnion(outputValues, _resolvedValue.getFunctionOutputs())) {
                s_logger.debug("Can't reuse {} for {}", node, _resolvedValue);
              } else {
                s_logger.debug("Reusing {} for {}", node, _resolvedValue);
                synchronized (GetTerminalValuesCallback.this) {
                  // Update the output values for the node with the union. The input values will
                  // be dealt with by the callback.
                  List<ValueSpecification> replacements = null;
                  for (ValueSpecification output : _resolvedValue.getFunctionOutputs()) {
                    if (outputValues.contains(output)) {
                      // Exact match found
                      continue;
                    }
                    final String outputName = output.getValueName();
                    final ValueProperties outputProperties = output.getProperties();
                    for (ValueSpecification outputValue : outputValues) {
                      if (outputName == outputValue.getValueName()) {
                        if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                          // Found match
                          final ValueProperties composedProperties = outputValue.getProperties().compose(outputProperties);
                          if (!composedProperties.equals(outputValue.getProperties())) {
                            final ValueSpecification newOutputValue = MemoryUtils
                                .instance(new ValueSpecification(outputValue.getValueName(), outputValue.getTargetSpecification(), composedProperties));
                            s_logger.debug("Replacing {} with {} in reused node", outputValue, newOutputValue);
                            if (replacements == null) {
                              replacements = new ArrayList<ValueSpecification>(outputValues.size() * 2);
                            }
                            replacements.add(outputValue);
                            replacements.add(newOutputValue);
                          }
                        }
                      }
                    }
                  }
                  if (replacements != null) {
                    final Iterator<ValueSpecification> replacement = replacements.iterator();
                    while (replacement.hasNext()) {
                      final ValueSpecification oldValue = replacement.next();
                      final ValueSpecification newValue = replacement.next();
                      node.replaceOutputValue(oldValue, newValue);
                    }
                  }
                }
                _found = node;
                callback = _callback;
                _callback = null;
              }
            }
          }
        }
      }
      if (callback != null) {
        callback.node(_found);
      }
    }

    @Override
    public void getNode(final DependencyNodeCallback callback) {
      synchronized (this) {
        assert _callback == null;
        if ((_found == null) && (_pending > 0)) {
          _callback = callback;
          return;
        }
      }
      callback.node(_found);
    }

  }

  private static final class PublishNode implements DependencyNodeProducer, DependencyNodeCallback {

    private final DependencyNodeCallback _underlying;
    private DependencyNode _node;
    private Collection<DependencyNodeCallback> _callbacks = new LinkedList<DependencyNodeCallback>();

    public PublishNode(final DependencyNodeCallback underlying) {
      _underlying = underlying;
    }

    @Override
    public void node(final DependencyNode node) {
      _underlying.node(node);
      final Collection<DependencyNodeCallback> callbacks;
      synchronized (this) {
        _node = node;
        if (_callbacks == null) {
          return;
        }
        if (_callbacks.isEmpty()) {
          callbacks = Collections.emptyList();
        } else {
          callbacks = new ArrayList<DependencyNodeCallback>(_callbacks);
        }
        _callbacks = null;
      }
      for (DependencyNodeCallback callback : callbacks) {
        callback.node(node);
      }
    }

    @Override
    public void getNode(final DependencyNodeCallback callback) {
      final DependencyNode node;
      synchronized (this) {
        if (_callbacks != null) {
          _callbacks.add(callback);
          return;
        }
        node = _node;
      }
      callback.node(node);
    }

  }

  private void getOrCreateNode(final GraphBuildingContext context, final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream,
      final DependencyNodeCallback result, final DependencyNode node, final boolean nodeIsNew) {
    final int debugId = s_nextDebugId.incrementAndGet();
    Set<ValueSpecification> downstreamCopy = null;
    NodeInputProduction producers = null;
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Searching for node for {} inputs at {}", resolvedValue.getFunctionInputs().size(), debugId);
    }
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      final DependencyNode inputNode;
      synchronized (this) {
        node.addInputValue(input);
        inputNode = _spec2Node.get(input);
        if (inputNode != null) {
          s_logger.debug("Found node {} for input {}", inputNode, input);
          node.addInputNode(inputNode);
        }
      }
      if (inputNode == null) {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = context.getProduction(input);
        if (inputValue != null) {
          final Set<ValueSpecification> downstreamFinal;
          if (downstreamCopy != null) {
            downstreamFinal = downstreamCopy;
          } else {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            downstreamFinal = downstreamCopy;
            s_logger.debug("Downstream = {}", downstreamFinal);
          }
          if (producers == null) {
            producers = new NodeInputProduction(resolvedValue, node, result, nodeIsNew);
            // Starts with count of 2 (for this producer, and for the overall "block")
          } else {
            producers.addProducer();
          }
          final NodeInputProduction producersFinal = producers;
          getOrCreateNode(context, inputValue, downstreamFinal, new DependencyNodeCallback() {

            @Override
            public void node(final DependencyNode inputNode) {
              if (inputNode != null) {
                synchronized (GetTerminalValuesCallback.this) {
                  node.addInputNode(inputNode);
                }
                producersFinal.completed();
              } else {
                s_logger.warn("No node production for {} at {}", inputValue, debugId);
                producersFinal.failed();
              }
            }

          });
        } else {
          s_logger.warn("No registered node production for {} at {}", input, debugId);
          result.node(null);
          return;
        }
      }
    }
    if (producers == null) {
      nodeProduced(resolvedValue, node, result, nodeIsNew);
    } else {
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("Production of {} deferred at {}", node, debugId);
      }
      // Release the first call
      producers.completed();
    }
  }

  private void getOrCreateNode(final GraphBuildingContext context, final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream,
      final DependencyNodeCallback result, final DependencyNode existingNode, final Set<DependencyNodeProducer> nodes) {
    if (existingNode != null) {
      getOrCreateNode(context, resolvedValue, downstream, result, existingNode, false);
    } else {
      final DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      final PublishNode publisher = new PublishNode(result);
      synchronized (nodes) {
        nodes.add(publisher);
      }
      getOrCreateNode(context, resolvedValue, downstream, publisher, newNode, true);
      publisher.getNode(new DependencyNodeCallback() {

        @Override
        public void node(final DependencyNode node) {
          if (node == null) {
            synchronized (nodes) {
              nodes.remove(publisher);
            }
          }
        }

      });
    }
  }

  private void getOrCreateNode(final GraphBuildingContext context, final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream,
      final DependencyNodeCallback result) {
    s_logger.debug("Resolved {}", resolvedValue.getValueSpecification());
    if (downstream.contains(resolvedValue.getValueSpecification())) {
      s_logger.debug("Already have downstream production of {} in {}", resolvedValue.getValueSpecification(), downstream);
      result.node(null);
      return;
    }
    final DependencyNode existingNode;
    synchronized (this) {
      existingNode = _spec2Node.get(resolvedValue.getValueSpecification());
    }
    if (existingNode != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      result.node(existingNode);
      return;
    }
    final Set<DependencyNodeProducer> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getValueSpecification().getTargetSpecification());
    final FindExistingNodes findExisting = new FindExistingNodes(resolvedValue, nodes);
    if (findExisting.isDeferred()) {
      s_logger.debug("Deferring evaluation of {} existing nodes", nodes.size());
      findExisting.getNode(new DependencyNodeCallback() {

        @Override
        public void node(final DependencyNode node) {
          getOrCreateNode(context, resolvedValue, downstream, result, node, nodes);
        }

      });
    } else {
      getOrCreateNode(context, resolvedValue, downstream, result, findExisting.getNode(), nodes);
    }
  }

  @Override
  public synchronized void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    if (pump != null) {
      context.close(pump);
    }
    getOrCreateNode(context, resolvedValue, Collections.<ValueSpecification>emptySet(), new DependencyNodeCallback() {

      @Override
      public void node(final DependencyNode node) {
        if (node == null) {
          s_logger.error("Resolved {} to {} but couldn't create one or more dependency nodes", valueRequirement, resolvedValue.getValueSpecification());
        } else {
          assert node.getOutputValues().contains(resolvedValue.getValueSpecification());
          synchronized (GetTerminalValuesCallback.this) {
            assert _graphNodes.contains(node);
            _resolvedValues.put(valueRequirement, resolvedValue.getValueSpecification());
          }
        }
      }

    });
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

  public synchronized Collection<DependencyNode> getGraphNodes() {
    return new ArrayList<DependencyNode>(_graphNodes);
  }

  public synchronized Map<ValueRequirement, ValueSpecification> getTerminalValues() {
    return new HashMap<ValueRequirement, ValueSpecification>(_resolvedValues);
  }

  protected synchronized void discardIntermediateState() {
    s_logger.debug("Discarding func2target2nodes state");
    _func2target2nodes.clear();
    s_logger.debug("Discarding spec2Node state");
    _spec2Node.clear();
  }

}
