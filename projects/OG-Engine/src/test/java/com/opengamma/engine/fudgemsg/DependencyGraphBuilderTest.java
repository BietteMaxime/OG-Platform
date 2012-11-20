/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests {@link DependencyGraphFudgeBuilder}
 */
@Test
public class DependencyGraphBuilderTest extends AbstractFudgeBuilderTestCase {
  
  @Test
  public void testCycleSimpleGraph() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = env.compileViewDefinition(Instant.now(), VersionCorrection.LATEST);
    DependencyGraph graph = compiledViewDefinition.getDependencyGraph(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME);
    DependencyGraph cycledGraph = cycleObject(DependencyGraph.class, graph);
    
    assertEquals(graph.getCalculationConfigurationName(), cycledGraph.getCalculationConfigurationName());
    assertEquals(graph.getAllComputationTargets(), cycledGraph.getAllComputationTargets());
    assertEquals(graph.getOutputSpecifications(), cycledGraph.getOutputSpecifications());
    assertEquals(graph.getSize(), cycledGraph.getSize());
    assertEquals(graph.getTerminalOutputSpecifications(), cycledGraph.getTerminalOutputSpecifications());
    
    for (DependencyNode node : graph.getDependencyNodes()) {
      boolean isRoot = graph.getRootNodes().contains(node);
      for (ValueSpecification spec : node.getOutputValues()) {
        DependencyNode equivalentNode = cycledGraph.getNodeProducing(spec);
        assertEquals(isRoot, cycledGraph.getRootNodes().contains(equivalentNode));
        assertEquals(node.getInputValues(), equivalentNode.getInputValues());
        assertEquals(node.getOutputValues(), equivalentNode.getOutputValues());
        assertEquals(node.getTerminalOutputValues(), equivalentNode.getTerminalOutputValues());
      }
    }
  }

}
