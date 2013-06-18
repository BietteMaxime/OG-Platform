/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class VolatilitySurfaceManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final VolatilitySurfaceSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ VolatilitySurfaceManipulatorBuilder(Scenario scenario, VolatilitySurfaceSelector selector) {
    ArgumentChecker.notNull(scenario, "scenario");
    ArgumentChecker.notNull(selector, "selector");
    _scenario = scenario;
    _selector = selector;
  }

  public VolatilitySurfaceManipulatorBuilder parallelShift(double shift) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.ParallelShift(shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder singleAdditiveShift(double x, double y, double shift) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.SingleAdditiveShift(x, y, shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleAdditiveShifts(double[] x, double[] y, double[] shifts) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.MultipleAdditiveShifts(x, y, shifts));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder constantMultiplicativeShift(double shift) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.ConstantMultiplicativeShift(shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder singleMultiplicativeShift(double x, double y, double shift) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.SingleMultiplicativeShift(x, y, shift));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder multipleMultiplicativeShifts(double[] x, double[] y, double[] shifts) {
    _scenario.add(_selector, new VolatilitySurfaceManipulations.MultipleMultiplicativeShifts(x, y, shifts));
    return this;
  }
}
