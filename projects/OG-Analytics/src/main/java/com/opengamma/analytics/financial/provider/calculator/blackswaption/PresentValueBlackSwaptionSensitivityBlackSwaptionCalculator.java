/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackSwaptionFlatProviderInterface, PresentValueBlackSwaptionSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator INSTANCE = new PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborBlackMethod.getInstance();

  // -----     Swaption     ------

  @Override
  public PresentValueBlackSwaptionSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface black) {
    return METHOD_SWT_PHYS.presentValueBlackSensitivity(swaption, black);
  }

}
