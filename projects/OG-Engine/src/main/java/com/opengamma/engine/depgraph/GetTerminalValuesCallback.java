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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Handles callback notifications of terminal values to populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);

  private static class PerFunctionNodeInfo {

    /**
     * All nodes using this function, indexed by target specification.
     */
    private final Map<ComputationTargetSpecification, Set<DependencyNode>> _target2nodes = new HashMap<ComputationTargetSpecification, Set<DependencyNode>>();

    /**
     * All targets that a {@link ComputationTargetCollapser} can apply to, with its checking group. Any targets in the same group have already been mutually compared, and cannot be collapsed.
     */
    private Map<ComputationTargetSpecification, Object> _target2collapseGroup;

    /**
     * All targets that a {@link ComputationTargetCollapser} can apply to, indexed by the checking group. Any targets in the same group have already been mutually compared, and cannot be collapsed.
     */
    private Map<Object, Collection<ComputationTargetSpecification>> _collapseGroup2targets;

    /**
     * Queue of targets that must be collapsed, the first element in each pair if the original target - the next element is the replacement target.
     */
    private Queue<Pair<ComputationTargetSpecification, ComputationTargetSpecification>> _collapse;

    public void storeForCollapse(final ComputationTargetSpecification targetSpecification) {
      if (_target2collapseGroup == null) {
        _target2collapseGroup = new HashMap<ComputationTargetSpecification, Object>();
        _collapseGroup2targets = new HashMap<Object, Collection<ComputationTargetSpecification>>();
        _collapse = new LinkedTransferQueue<Pair<ComputationTargetSpecification, ComputationTargetSpecification>>();
      }
      if (!_target2collapseGroup.containsKey(targetSpecification)) {
        final Object group = new Object();
        _target2collapseGroup.put(targetSpecification, group);
        _collapseGroup2targets.put(group, Collections.singleton(targetSpecification));
      }
    }

  }

  /**
   * Buffer of resolved value specifications. For any entries in here, all input values have been previously resolved and are in this buffer or the partially constructed graph. Information here gets
   * used to construct dependency graph fragments whenever a terminal item can be resolved.
   */
  private final ConcurrentMap<ValueSpecification, ResolvedValue> _resolvedBuffer = new ConcurrentHashMap<ValueSpecification, ResolvedValue>();

  /**
   * Index into the dependency graph nodes, keyed by their output specifications.
   */
  private final Map<ValueSpecification, DependencyNode> _spec2Node = new ConcurrentHashMap<ValueSpecification, DependencyNode>();

  /**
   * Index into the dependency graph nodes.
   */
  private final Map<ParameterizedFunction, PerFunctionNodeInfo> _func2nodeInfo = new HashMap<ParameterizedFunction, PerFunctionNodeInfo>();

  /**
   * All dependency graph nodes.
   */
  private final Set<DependencyNode> _graphNodes = new HashSet<DependencyNode>();

  /**
   * Terminal value resolutions, mapping the resolved value specifications back to the originally requested requested value requirements.
   */
  private final Map<ValueSpecification, Collection<ValueRequirement>> _resolvedValues = new HashMap<ValueSpecification, Collection<ValueRequirement>>();

  /**
   * Queue of completed resolutions that have not been processed into the partial graph. Graph construction is single threaded, with this queue holding work items if other thread produce results while
   * one is busy building the graph.
   */
  private final Queue<Pair<ValueRequirement, ResolvedValue>> _resolvedQueue = new LinkedTransferQueue<Pair<ValueRequirement, ResolvedValue>>();

  /**
   * Index of functions which have an active collapse set.
   */
  private final Set<ParameterizedFunction> _collapsers = new HashSet<ParameterizedFunction>();

  /**
   * Mutex for working on the resolved queue. Any thread can add to the queue, only the one that has claimed this mutex can process the elements from it and be sure of exclusive access to the other
   * data structures.
   */
  private final AtomicReference<Thread> _singleton = new AtomicReference<Thread>();

  /**
   * Optional visitor to process failures from the graph build. This can be specified to provide additional error reporting to the user.
   */
  private ResolutionFailureVisitor<?> _failureVisitor;

  /**
   * Optional logic to collapse nodes on mutually compatible targets into a single node.
   */
  private ComputationTargetCollapser _computationTargetCollapser;

  public GetTerminalValuesCallback(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setComputationTargetCollapser(final ComputationTargetCollapser collapser) {
    _computationTargetCollapser = collapser;
  }

  public ResolvedValue getProduction(final ValueSpecification specification) {
    final ResolvedValue value = _resolvedBuffer.get(specification);
    if (value != null) {
      return value;
    }
    final DependencyNode node = _spec2Node.get(specification);
    if (node == null) {
      return null;
    }
    // Can only use the specification if it is consumed by another node; i.e. it has been fully resolved
    // and is not just an advisory used to merge tentative results to give a single node producing multiple
    // outputs.
    synchronized (this) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (dependent.hasInputValue(specification)) {
          return new ResolvedValue(specification, node.getFunction(), node.getInputValuesCopy(), node.getOutputValuesCopy());
        }
      }
    }
    return null;
  }

  public void declareProduction(final ResolvedValue resolvedValue) {
    _resolvedBuffer.put(resolvedValue.getValueSpecification(), resolvedValue);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.info("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.checkFailure(value);
      if (_failureVisitor != null) {
        failureImpl.accept(_failureVisitor);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failureImpl));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  public synchronized void populateState(final DependencyGraph graph) {
    final Set<DependencyNode> remove = new HashSet<DependencyNode>();
    for (final DependencyNode node : graph.getDependencyNodes()) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (!graph.containsNode(dependent)) {
          // Need to remove "dependent" from the node. We can leave the output values there; they might be used, or will get discarded by discardUnusedOutputs afterwards
          remove.add(dependent);
        }
      }
      _graphNodes.add(node);
      for (final ValueSpecification output : node.getOutputValues()) {
        _spec2Node.put(output, node);
      }
      getOrCreateNodes(node.getFunction(), node.getComputationTarget()).add(node);
    }
    for (final DependencyNode node : remove) {
      node.clearInputs();
    }
    for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : graph.getTerminalOutputs().entrySet()) {
      _resolvedValues.put(terminal.getKey(), new ArrayList<ValueRequirement>(terminal.getValue()));
    }
  }

  private class CollapseNodes implements ContextRunnable {

    private final ParameterizedFunction _function;
    private final PerFunctionNodeInfo _nodeInfo;
    private final ComputationTargetSpecification[] _a;
    private final ComputationTargetSpecification[] _b;

    public CollapseNodes(ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo, final Collection<ComputationTargetSpecification> a, final Collection<ComputationTargetSpecification> b) {
      _function = function;
      _nodeInfo = nodeInfo;
      _a = a.toArray(new ComputationTargetSpecification[a.size()]);
      _b = b.toArray(new ComputationTargetSpecification[b.size()]);
    }

    // ContextRunnable

    @Override
    public boolean tryRun(final GraphBuildingContext context) {
      int aLength = _a.length;
      int bLength = _b.length;
      for (int i = 0; i < aLength; i++) {
        final ComputationTargetSpecification a = _a[i];
        for (int j = 0; j < bLength; j++) {
          final ComputationTargetSpecification b = _b[j];
          assert !a.equals(b);
          final ComputationTargetSpecification collapsed = _computationTargetCollapser.collapse(_function.getFunction(), a, b);
          if (collapsed != null) {
            if (collapsed.equals(a)) {
              // A and B merged into A
              _b[j--] = _b[--bLength];
              _nodeInfo._collapse.add(Pair.of(b, a));
              s_logger.debug("Merging {} into {}", b, a);
            } else if (collapsed.equals(b)) {
              // A and B merged into B
              _a[i--] = _a[--aLength];
              _nodeInfo._collapse.add(Pair.of(a, b));
              s_logger.debug("Merging {} into {}", a, b);
              break;
            } else {
              // A and B merged into new target
              _a[i--] = _a[--aLength];
              _b[j] = _b[--bLength];
              // Note the new target will go into its own evaluation group when this is actioned; it will then be compared against the other targets
              _nodeInfo._collapse.add(Pair.of(a, collapsed));
              _nodeInfo._collapse.add(Pair.of(b, collapsed));
              if (s_logger.isDebugEnabled()) {
                s_logger.debug("Merging {} and {} into new node {}", new Object[] {a, b, collapsed });
              }
              break;
            }
          }
        }
      }
      Collection<ComputationTargetSpecification> targets = new ArrayList<ComputationTargetSpecification>(aLength + bLength);
      for (int i = 0; i < aLength; i++) {
        targets.add(_a[i]);
      }
      for (int i = 0; i < bLength; i++) {
        targets.add(_b[i]);
      }
      final Object group = new Object();
      // TODO: Waiting for the lock could be costly; we could post this to a queue like the resolved values do
      synchronized (GetTerminalValuesCallback.this) {
        for (int i = 0; i < aLength; i++) {
          _nodeInfo._target2collapseGroup.put(_a[i], group);
        }
        for (int i = 0; i < bLength; i++) {
          _nodeInfo._target2collapseGroup.put(_b[i], group);
        }
        _nodeInfo._collapseGroup2targets.put(group, targets);
        scheduleCollapsers(context, _function, _nodeInfo);
      }
      return true;
    }
  }

  // TODO: Multiple nodes for a single collapse applicable target should be collapsed, probably with a "collapse(function, a, a)" sanity check first

  private void scheduleCollapsers(final GraphBuildingContext context, final ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo) {
    // Action anything already found asynchronously
    Pair<ComputationTargetSpecification, ComputationTargetSpecification> collapse = nodeInfo._collapse.poll();
    while (collapse != null) {
      s_logger.debug("Found collapse targets {}", collapse);
      nodeInfo._target2collapseGroup.remove(collapse.getFirst());
      final Set<DependencyNode> originalNodes = nodeInfo._target2nodes.remove(collapse.getFirst());
      final Set<DependencyNode> newNodes = getOrCreateNodes(function, collapse.getSecond());
      final DependencyNode newNode;
      if (newNodes.isEmpty()) {
        newNode = new DependencyNode(collapse.getSecond());
        newNode.setFunction(function);
        newNodes.add(newNode);
        _graphNodes.add(newNode);
      } else {
        // TODO: See comment above about whether multiple nodes for a single collapse applicable target should exist
        newNode = newNodes.iterator().next();
      }
      for (DependencyNode originalNode : originalNodes) {
        s_logger.debug("Applying collapse of {} into {}", originalNode, newNode);
        if (!_graphNodes.remove(originalNode)) {
          s_logger.error("Assertion error - {} is not in the graph", originalNode);
          continue;
        }
        originalNode.replaceWith(newNode);
        for (final ValueSpecification output : originalNode.getOutputValues()) {
          final ValueSpecification newOutput = MemoryUtils.instance(new ValueSpecification(output.getValueName(), newNode.getComputationTarget(), output.getProperties()));
          _spec2Node.put(output, newNode);
          _spec2Node.put(newOutput, newNode);
          final Collection<ValueRequirement> requirements = _resolvedValues.remove(output);
          if (requirements != null) {
            _resolvedValues.put(newOutput, requirements);
          }
        }
      }
      collapse = nodeInfo._collapse.poll();
    }
    // Schedule collapsing tasks to run asynchronously
    int collapseGroups = nodeInfo._collapseGroup2targets.size();
    if (collapseGroups > 1) {
      final Iterator<Map.Entry<ComputationTargetSpecification, Object>> itrTarget2CollapseGroup = nodeInfo._target2collapseGroup.entrySet().iterator();
      do {
        final Map.Entry<ComputationTargetSpecification, Object> target2collapseGroup = itrTarget2CollapseGroup.next();
        if (!nodeInfo._target2nodes.containsKey(target2collapseGroup.getKey())) {
          // Note: This happens because entries get written into the nodeInfo as soon as a target is requested. The target might not result in any
          // nodes being created because of an earlier substitution. This is a simple solution - an alternative and possible faster method is to not
          // create the target2collapseGroup entry until a node is created. The alternative is harder to implement though! 
          s_logger.debug("Found transient key {}", target2collapseGroup);
          final Collection<ComputationTargetSpecification> targetSpecs = nodeInfo._collapseGroup2targets.get(target2collapseGroup.getValue());
          if (targetSpecs.size() == 1) {
            if (targetSpecs.contains(target2collapseGroup.getKey())) {
              nodeInfo._collapseGroup2targets.remove(target2collapseGroup.getValue());
              collapseGroups--;
            } else {
              s_logger.error("Assertion error - transient singleton key {} not in reverse lookup table", target2collapseGroup.getKey());
            }
          } else {
            if (!targetSpecs.remove(target2collapseGroup.getKey())) {
              s_logger.error("Assertion error - transient key {} not in reverse lookup table", target2collapseGroup.getKey());
            }
          }
          itrTarget2CollapseGroup.remove();
        }
      } while (itrTarget2CollapseGroup.hasNext());
      if (collapseGroups > 1) {
        final Iterator<Collection<ComputationTargetSpecification>> itrCollapseGroup2Targets = nodeInfo._collapseGroup2targets.values().iterator();
        do {
          final Collection<ComputationTargetSpecification> a = itrCollapseGroup2Targets.next();
          if (!itrCollapseGroup2Targets.hasNext()) {
            break;
          }
          itrCollapseGroup2Targets.remove();
          final Collection<ComputationTargetSpecification> b = itrCollapseGroup2Targets.next();
          itrCollapseGroup2Targets.remove();
          context.submit(new CollapseNodes(function, nodeInfo, a, b));
        } while (itrCollapseGroup2Targets.hasNext());
      }
    }
  }

  private void scheduleCollapsers(final GraphBuildingContext context) {
    if (!_collapsers.isEmpty()) {
      final Iterator<ParameterizedFunction> itrCollapsers = _collapsers.iterator();
      do {
        final ParameterizedFunction function = itrCollapsers.next();
        final PerFunctionNodeInfo nodeInfo = _func2nodeInfo.get(function);
        scheduleCollapsers(context, function, nodeInfo);
      } while (itrCollapsers.hasNext());
    }
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, DependencyNode node,
      final boolean newNode) {
    Set<ValueSpecification> downstreamCopy = null;
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      DependencyNode inputNode;
      inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        if (input.getTargetSpecification().equals(inputNode.getComputationTarget())) {
          node.addInputValue(input);
        } else {
          // The node we connected to is a substitute following a target collapse; the original input value is now incorrect
          final ValueSpecification substituteInput = MemoryUtils.instance(new ValueSpecification(input.getValueName(), inputNode.getComputationTarget(), input.getProperties()));
          assert inputNode.getOutputValues().contains(substituteInput);
          node.addInputValue(substituteInput);
        }
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = _resolvedBuffer.get(input);
        if (inputValue != null) {
          if (downstreamCopy == null) {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            s_logger.debug("Downstream = {}", downstreamCopy);
          }
          inputNode = getOrCreateNode(inputValue, downstreamCopy);
          if (inputNode != null) {
            node.addInputNode(inputNode);
            node.addInputValue(input);
          } else {
            s_logger.warn("No node production for {}", inputValue);
            return null;
          }
        } else {
          s_logger.warn("No registered production for {}", input);
          return null;
        }
      }
    }
    if (newNode) {
      s_logger.debug("Adding {} to graph set", node);
      // [PLAT-346] Here is a good spot to tackle PLAT-346; which node's outputs do we discard if there are multiple
      // productions for a given value specification?
      for (final ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
        final DependencyNode existing = _spec2Node.get(valueSpecification);
        if (existing == null) {
          _spec2Node.put(valueSpecification, node);
        } else {
          // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
          node.removeOutputValue(valueSpecification);
        }
      }
      _graphNodes.add(node);
      _resolvedBuffer.remove(resolvedValue.getValueSpecification());
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode existingNode,
      final Set<DependencyNode> nodes) {
    if (existingNode != null) {
      return getOrCreateNode(resolvedValue, downstream, existingNode, false);
    } else {
      DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      newNode = getOrCreateNode(resolvedValue, downstream, newNode, true);
      if (newNode != null) {
        nodes.add(newNode);
      }
      return newNode;
    }
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final PerFunctionNodeInfo nodeInfo, final ComputationTargetSpecification targetSpecification) {
    Set<DependencyNode> nodes = nodeInfo._target2nodes.get(targetSpecification);
    if (nodes == null) {
      nodes = new HashSet<DependencyNode>();
      nodeInfo._target2nodes.put(targetSpecification, nodes);
      if ((_computationTargetCollapser != null) && _computationTargetCollapser.canApplyTo(targetSpecification)) {
        nodeInfo.storeForCollapse(targetSpecification);
        _collapsers.add(function);
      }
    }
    return nodes;
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTargetSpecification targetSpecification) {
    PerFunctionNodeInfo nodeInfo = _func2nodeInfo.get(function);
    if (nodeInfo == null) {
      nodeInfo = new PerFunctionNodeInfo();
      _func2nodeInfo.put(function, nodeInfo);
    }
    return getOrCreateNodes(function, nodeInfo, targetSpecification);
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    nextA: for (final ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      boolean mismatch = false;
      for (final ValueSpecification b : bs) {
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
   * Tests whether the union of value specifications would be mismatched; that is the two sets can't be composed. Given the intersection of common value names, the properties must be mutually
   * compatible.
   * 
   * @param as the first set of values, not null
   * @param bs the second set of values, not null
   * @return true if the values can't be composed, false if they can
   */
  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  private DependencyNode findExistingNode(final Set<DependencyNode> nodes, final ResolvedValue resolvedValue) {
    for (final DependencyNode node : nodes) {
      final Set<ValueSpecification> outputValues = node.getOutputValues();
      if (mismatchUnion(outputValues, resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Can't reuse {} for {}", node, resolvedValue);
      } else {
        s_logger.debug("Considering {} for {}", node, resolvedValue);
        // Update the output values for the node with the union. The input values will be dealt with by the caller.
        List<ValueSpecification> replacements = null;
        boolean matched = false;
        for (final ValueSpecification output : resolvedValue.getFunctionOutputs()) {
          if (outputValues.contains(output)) {
            // Exact match found
            matched = true;
            continue;
          }
          final String outputName = output.getValueName();
          final ValueProperties outputProperties = output.getProperties();
          for (final ValueSpecification outputValue : outputValues) {
            if (outputName == outputValue.getValueName()) {
              if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                // Found match
                matched = true;
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
        if (!matched) {
          continue;
        }
        if (replacements != null) {
          final Iterator<ValueSpecification> replacement = replacements.iterator();
          while (replacement.hasNext()) {
            final ValueSpecification oldValue = replacement.next();
            final ValueSpecification newValue = replacement.next();
            final int newConsumers = node.replaceOutputValue(oldValue, newValue);
            DependencyNode n = _spec2Node.remove(oldValue);
            assert n == node;
            n = _spec2Node.get(newValue);
            if (n != null) {
              // Reducing the value has created a collision ...
              if (newConsumers == 0) {
                // Keep the existing one (it's being used, or just an arbitrary choice if neither are used)
                node.removeOutputValue(newValue);
              } else {
                int existingConsumers = 0;
                for (final DependencyNode child : n.getDependentNodes()) {
                  if (child.hasInputValue(newValue)) {
                    existingConsumers++;
                  }
                }
                if (existingConsumers == 0) {
                  // Lose the existing (not being used), keep the new one
                  n.removeOutputValue(newValue);
                  _spec2Node.put(newValue, node);
                } else {
                  if (newConsumers <= existingConsumers) {
                    // Adjust the consumers of the reduced value to use the existing one
                    for (final DependencyNode child : node.getDependentNodes()) {
                      child.replaceInput(newValue, node, n);
                    }
                    node.removeOutputValue(newValue);
                  } else {
                    // Adjust the consumers of the existing value to use the new one
                    for (final DependencyNode child : n.getDependentNodes()) {
                      child.replaceInput(newValue, n, node);
                    }
                    n.removeOutputValue(newValue);
                    _spec2Node.put(newValue, node);
                  }
                }
              }
            } else {
              _spec2Node.put(newValue, node);
            }
          }
        }
        return node;
      }
    }
    return null;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream) {
    s_logger.debug("Resolved {}", resolvedValue.getValueSpecification());
    if (downstream.contains(resolvedValue.getValueSpecification())) {
      s_logger.debug("Already have downstream production of {} in {}", resolvedValue.getValueSpecification(), downstream);
      return null;
    }
    final DependencyNode existingNode = _spec2Node.get(resolvedValue.getValueSpecification());
    if (existingNode != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      return existingNode;
    }
    final Set<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getValueSpecification().getTargetSpecification());
    return getOrCreateNode(resolvedValue, downstream, findExistingNode(nodes, resolvedValue), nodes);
  }

  /**
   * Reports a successful resolution of a top level requirement. The production of linked {@link DependencyNode} instances to form the final graph is single threaded. The resolution is added to a
   * queue of successful resolutions. If this is the only (or first) thread to report resolutions then this will work to drain the queue and produce nodes for the graph based on the resolved value
   * cache in the building context. If other threads report resolutions while this is happening they are added to the queue and those threads return immediately.
   */
  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    if (pump != null) {
      context.close(pump);
    }
    _resolvedQueue.add(Pair.of(valueRequirement, resolvedValue));
    while (!_resolvedQueue.isEmpty() && _singleton.compareAndSet(null, Thread.currentThread())) {
      synchronized (this) {
        Pair<ValueRequirement, ResolvedValue> resolved = _resolvedQueue.poll();
        while (resolved != null) {
          final DependencyNode node = getOrCreateNode(resolved.getSecond(), Collections.<ValueSpecification>emptySet());
          if (node != null) {
            ValueSpecification outputValue = resolved.getSecond().getValueSpecification();
            if (!outputValue.getTargetSpecification().equals(node.getComputationTarget())) {
              outputValue = MemoryUtils.instance(new ValueSpecification(outputValue.getValueName(), node.getComputationTarget(), outputValue.getProperties()));
            }
            assert node.getOutputValues().contains(outputValue);
            Collection<ValueRequirement> requirements = _resolvedValues.get(outputValue);
            if (requirements == null) {
              requirements = new ArrayList<ValueRequirement>();
              _resolvedValues.put(outputValue, requirements);
            }
            requirements.add(resolved.getFirst());
          } else {
            s_logger.error("Resolved {} to {} but couldn't create one or more dependency node", resolved.getFirst(), resolved.getSecond().getValueSpecification());
          }
          resolved = _resolvedQueue.poll();
        }
        scheduleCollapsers(context);
      }
      _singleton.set(null);
    }
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

  /**
   * Returns the dependency graph nodes built by calls to {@link #resolved}. It is only valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned.
   * A copy of the internal structure is used so that it may be modified by the caller and this callback instance be used to process subsequent resolutions.
   * 
   * @return the dependency graph nodes, not null
   */
  public synchronized Collection<DependencyNode> getGraphNodes() {
    return new ArrayList<DependencyNode>(_graphNodes);
  }

  /**
   * Returns the map of top level requirements requested of the graph builder to the specifications it produced that are in the dependency graph. Failed resolutions are not reported here. It is only
   * valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned. A copy of the internal structure is used so that it may be modified by the caller
   * and this callback instance be used to process subsequent resolutions.
   * 
   * @return the map of resolutions, not null
   */
  public synchronized Map<ValueRequirement, ValueSpecification> getTerminalValues() {
    final Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>(_resolvedValues.size());
    for (final Map.Entry<ValueSpecification, Collection<ValueRequirement>> resolvedValues : _resolvedValues.entrySet()) {
      for (final ValueRequirement requirement : resolvedValues.getValue()) {
        result.put(requirement, resolvedValues.getKey());
      }
    }
    return result;
  }

  public void reportStateSize() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    s_logger.info("Graph = {} nodes for {} terminal outputs", _graphNodes.size(), _resolvedValues.size());
    s_logger.info("Resolved buffer = {}, resolved queue = {}", _resolvedBuffer.size(), _resolvedQueue.size());
  }

}
