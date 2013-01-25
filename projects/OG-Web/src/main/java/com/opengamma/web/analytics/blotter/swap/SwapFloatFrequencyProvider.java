/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.web.analytics.blotter.ValueProvider;

/**
*
*/
public class SwapFloatFrequencyProvider implements ValueProvider<SwapSecurity> {

  @Override
  public Frequency getValue(SwapSecurity security) {
    // float leg frequency for fixed/float, receive leg frequency for float/float
    return new FrequencyVisitor().visit(security).getSecond();
  }
}
