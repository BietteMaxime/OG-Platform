/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public enum SwapType {
  /**
   * 
   */
  SWAP,
  /**
   * 
   */
  FORWARD;

  public static SwapType identify(final SwapSecurity object) {
    return object.accept(new FinancialSecurityVisitorAdapter<SwapType>() {

      @Override
      public SwapType visitForwardSwapSecurity(ForwardSwapSecurity security) {
        return FORWARD;
      }

      @Override
      public SwapType visitSwapSecurity(SwapSecurity security) {
        return SWAP;
      }

    });
  }

  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    switch (this) {
      case SWAP:
        return visitor.visitSwapSecurity(null);
      case FORWARD:
        return visitor.visitForwardSwapSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapType: " + this);
    }
  }

}
