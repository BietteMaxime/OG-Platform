/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.scratchpad;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;

/**
 * 
 */
public class IRSPFETest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private int calculateNumberOfTimeNodes(final double T, final double q) {
    return (int) q * (int) T + 1;
  }

  private double[] generateTimeNodes(final double T, final double deltaT) {

    int numberOfTimeNodes = calculateNumberOfTimeNodes(T, 1.0 / deltaT);

    double[] timeNodes = new double[numberOfTimeNodes];

    double t = 0.0;

    for (int j = 0; j < numberOfTimeNodes; j++) {

      timeNodes[j] = t;
      t += deltaT;
    }

    return timeNodes;
  }

  @Test
  public void testIRSPFECalc() {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    NormalRandomNumberGenerator normRand = new NormalRandomNumberGenerator(0.0, 1.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final int numberOfPositions = 1;

    final int numberOfSims = 10000;

    final double T = 20.0;
    final double deltaT = 0.5;

    double[] timeNodes = generateTimeNodes(T, deltaT);

    int numberOfTimeNodes = timeNodes.length;

    final double r0 = 0.05;
    final double a = 0.10;
    final double b = 0.05;
    final double sigma = 0.01;

    final double swapRate = 0.0496;

    final double[] r = new double[numberOfTimeNodes];

    double Z = 0.0;

    double[] epsilon = new double[numberOfTimeNodes];

    double[][] VFixed = new double[numberOfTimeNodes][numberOfSims];
    double[][] VFloat = new double[numberOfTimeNodes][numberOfSims];
    double[][] V = new double[numberOfTimeNodes][numberOfSims];

    double[][] EE = new double[numberOfTimeNodes][numberOfSims];

    r[0] = r0;

    int counter = 1;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    for (int alpha = 0; alpha < numberOfSims; alpha++) {

      /*
      if (alpha % 100 == 0) {
        System.out.println("Simulation = " + alpha);
      }
      */

      // ----------------------------------------------------------------------------------------------------------------------------------------

      epsilon = normRand.getVector(numberOfTimeNodes);

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // First generate the simulated rates at each timenode for this simulation

      for (int j = 1; j < numberOfTimeNodes; j++) {

        double dt = timeNodes[j] - timeNodes[j - 1];

        r[j] = r[j - 1] + a * (b - r[j - 1]) * dt + sigma * Math.sqrt(dt) * epsilon[j];
      }

      // ----------------------------------------------------------------------------------------------------------------------------------------

      counter = 0;

      int timeIndexCounter = 0;

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Now step through each of the simulation timenodes
      for (double t = 0.0; t <= T; t += deltaT) {

        // ----------------------------------------------------------------------------------------------------------------------------------------

        // Loop through each of the remaining coupon dates
        for (double tPrime = deltaT; tPrime <= (T - t); tPrime += deltaT) {

          // ----------------------------------------------------------------------------------------------------------------------------------------

          double B = (1 - Math.exp(-a * tPrime)) / a;
          double A = Math.exp((b - sigma * sigma / (2 * a * a)) * (B - tPrime) + (-sigma * sigma * B * B / (4 * a)));

          // Calculate the discount factor
          Z = A * Math.exp(-B * r[counter]);

          // ----------------------------------------------------------------------------------------------------------------------------------------

          // Add this discount factor to the running total for the fixed leg
          VFixed[timeIndexCounter][alpha] += Z;

          // ----------------------------------------------------------------------------------------------------------------------------------------
        }

        VFloat[timeIndexCounter][alpha] = 1.0 - Z;

        // ----------------------------------------------------------------------------------------------------------------------------------------

        VFixed[timeIndexCounter][alpha] *= swapRate * 0.25;

        V[timeIndexCounter][alpha] = VFixed[timeIndexCounter][alpha] - VFloat[timeIndexCounter][alpha];

        EE[timeIndexCounter][alpha] = Math.max(V[timeIndexCounter][alpha], 0.0);

        timeIndexCounter++;

        counter++;
      }

      // ----------------------------------------------------------------------------------------------------------------------------------------

      //EE[timeIndexCounter - 1] /= numberOfSims;

      //System.out.println(EE[timeIndexCounter - 1]);
    }

    double[] EPE = new double[numberOfTimeNodes];

    for (int i = 0; i < EPE.length; i++) {

      for (int alpha = 0; alpha < numberOfSims; alpha++) {
        EPE[i] += EE[i][alpha];
      }
      EPE[i] /= numberOfSims;

      System.out.println(i + "\t" + EPE[i]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }
}
