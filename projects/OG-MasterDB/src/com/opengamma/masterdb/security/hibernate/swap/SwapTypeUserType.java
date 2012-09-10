/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the SwapType enum
 */
public class SwapTypeUserType extends EnumUserType<SwapType> {

  private static final String FORWARD_SWAP_TYPE = "Forward";
  private static final String SWAP_TYPE = "Swap";

  public SwapTypeUserType() {
    super(SwapType.class, SwapType.values());
  }

  @Override
  protected String enumToStringNoCache(SwapType value) {
    return value.accept(new FinancialSecurityVisitorAdapter<String>() {

      @Override
      public String visitForwardSwapSecurity(ForwardSwapSecurity security) {
        return FORWARD_SWAP_TYPE;
      }

      @Override
      public String visitSwapSecurity(SwapSecurity security) {
        return SWAP_TYPE;
      }

    });
  }

}
