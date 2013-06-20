/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class GroovyDslTest {

  private static final Logger s_logger = LoggerFactory.getLogger(GroovyDslTest.class);

  @Test
  public void createSimulationFromDsl() {
    Simulation simulation = SimulationUtils.createSimulationFromDsl("src/test/resources/scenarios/SimulationDslTest.groovy");
    assertNotNull(simulation);
    // TODO check the simulation
    s_logger.debug(simulation.toString());
  }

  @Test
  public void createScenarioFromDsl() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/resources/scenarios/ScenarioDslTest.groovy");
    assertNotNull(scenario);
    // TODO check the simulation
    s_logger.debug(scenario.toString());
  }
}
