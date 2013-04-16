/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;

/**
 * 
 */
public class CapFloorPricer {

  private final SimpleOptionData[] _caplets;
  private final int _n;

  /**
   * Decomposes a cap (floor) down to relevant information about its caplets (floorlets), i.e. the forward (ibor) values, the fixing times and
   * the discount factors. Each caplet (floorlet), and hence the whole cap (floor) can then be priced by suppling a VolatilityModel1D 
   * (which gives a Black vol for a particular forward/strike/expiry) to the method price 
   * @param cap a cap or floor 
   * @param ycb The relevant yield curves 
   */
  public CapFloorPricer(final CapFloor cap, final YieldCurveBundle ycb) {
    _caplets = CapFloorDecomposer.toOptions(cap, ycb);
    _n = _caplets.length;
  }

  public double price(final double vol) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  /**
   * Price a cap (floor) with a VolatilityModel1D. This allows the same cap to be prices with different models (different models include different 
   * parameters for the same model), with repeating calculations (e.g. as part of a caplet stripping routine) 
   * @param volModel VolatilityModel1D  which gives a Black vol for a particular forward/strike/expiry
   * @return The cap (floor) price 
   */
  public double price(final VolatilityModel1D volModel) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      final double vol = volModel.getVolatility(_caplets[i]);
      sum += BlackFormulaRepository.price(_caplets[i], vol);
    }
    return sum;
  }

  public double impliedVol(final double capPrice) {
    return BlackFormulaRepository.impliedVolatility(_caplets, capPrice);
  }

  public double impliedVol(final VolatilityModel1D capletVolModel) {
    final double price = price(capletVolModel);
    return impliedVol(price);
  }

  public double vega(final double capVolatility) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      sum += BlackFormulaRepository.vega(_caplets[i], capVolatility);
    }
    return sum;
  }

  public double vega(final VolatilityModel1D capletVolModel) {
    final double vol = impliedVol(capletVolModel);
    return vega(vol);
  }

  /**
   * Gets the fwds.
   * @return the fwds
   */
  protected double[] getForwards() {
    double[] fwds = new double[_n];
    for (int i = 0; i < _n; i++) {
      fwds[i] = _caplets[i].getForward();
    }
    return fwds;
  }

  protected double getCapForward() {
    double sum1 = 0;
    double sum2 = 0;
    double[] df = getDiscountFactors();
    double[] fwds = getForwards();
    for (int i = 0; i < _n; i++) {
      sum1 += df[i] * fwds[i];
      sum2 += df[i];
    }
    return sum1 / sum2;
  }

  /**
   * Gets the t.
   * @return the t
   */
  protected double[] getExpiries() {
    double[] t = new double[_n];
    for (int i = 0; i < _n; i++) {
      t[i] = _caplets[i].getTimeToExpiry();
    }
    return t;
  }

  /**
   * Gets the df.
   * @return the df
   */
  protected double[] getDiscountFactors() {
    double[] df = new double[_n];
    for (int i = 0; i < _n; i++) {
      df[i] = _caplets[i].getDiscountFactor();
    }
    return df;
  }

  /**
   * Gets the k.
   * @return the k
   */
  protected double getStrike() {
    return _caplets[0].getStrike();
  }

  /**
   * Gets the n.
   * @return the n
   */
  protected int getNumberCaplets() {
    return _n;
  }

}
