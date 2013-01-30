/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

import java.util.Comparator;

import org.threeten.bp.Period;

/**
 * Comparator for FactorExposureData.  
 */
public class FactorExposureDataComparator implements Comparator<FactorExposureData> {

  @Override
  public int compare(FactorExposureData arg0, FactorExposureData arg1) {
    final int id = Long.valueOf(arg0.getFactorSetId()).compareTo(arg1.getFactorSetId());
    if (id  != 0) {
      return id;
    }
    final int factorType = arg0.getFactorType().getFactorType().compareTo(arg1.getFactorType().getFactorType());
    if (factorType != 0) {
      return factorType;
    }
    if (arg0.getNode() != null && arg0.getNode().length() > 0) {
      Period p0 = Period.parse("P" + arg0.getNode());
      if (arg1.getNode() != null && arg1.getNode().length() > 0) {
        Period p1 = Period.parse("P" + arg1.getNode());
        final long node = p0.normalizedDaysToHours().getTimeNanos() - p1.normalizedDaysToHours().getTimeNanos();
        if (node != 0) {
          return (int) node;
        }
      } else {
        return 1;
      }
    } else {
      if (arg1.getNode() != null && arg0.getNode().length() > 0) {
        return -1;
      }
    }
    return arg0.getFactorName().compareTo(arg1.getFactorName());
  }

}
