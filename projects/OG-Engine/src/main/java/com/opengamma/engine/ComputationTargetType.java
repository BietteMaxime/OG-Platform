/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.util.PublicAPI;

/**
 * The type that computation will be base on.
 */
@PublicAPI
public enum ComputationTargetType {
  
  // TODO: move to com.opengamma.engine.target

  // REVIEW 2012-06-14 Andrew -- A Portfolio can't be used as a PORTFOLIO_NODE. Some bits of code will recognise it, but the ComputationTarget has a getPortfolioNode method
  // that will throw a class cast exception. This has its origins from when Portfolio stopped extending PortfolioNode. We either need PORTFOLIO as a target type, or get rid
  // of the code that maps a Portfolio to PORTFOLIO_NODE

  /**
   * A set of positions (a portfolio node, or whole portfolio).
   */
  PORTFOLIO_NODE,
  /**
   * A position.
   */
  POSITION,
  /**
   * A security.
   */
  SECURITY,
  /**
   * A simple type, effectively "anything else".
   */
  PRIMITIVE,
  /**
   * A trade.
   */
  TRADE;

  /**
   * Checks if the type is compatible with the target.
   * @param target The target to check for compatibility
   * @return true if compatible
   */
  public boolean isCompatible(final Object target) {
    switch(this) {
      case PORTFOLIO_NODE:
        return (target instanceof PortfolioNode || target instanceof Portfolio);
      case POSITION:
        return (target instanceof Position);
      case TRADE:
        return (target instanceof Trade);
      case SECURITY:
        return (target instanceof Security);
      case PRIMITIVE:
        return (target instanceof Portfolio == false &&
                target instanceof PortfolioNode == false &&
                target instanceof Position == false &&
                target instanceof Security == false);
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + this);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Derives the type for the specified target.
   * @param target  the target to introspect, may be null
   * @return the type, not null
   */
  public static ComputationTargetType determineFromTarget(final Object target) {
    if (target instanceof Portfolio) {
      return PORTFOLIO_NODE;
    }
    if (target instanceof PortfolioNode) {
      return PORTFOLIO_NODE;
    }
    if (target instanceof Position) {
      return POSITION;
    }
    if (target instanceof Trade) {
      return TRADE;
    }
    if (target instanceof Security) {
      return SECURITY;
    }
    return PRIMITIVE;  // anything else
  }

}
