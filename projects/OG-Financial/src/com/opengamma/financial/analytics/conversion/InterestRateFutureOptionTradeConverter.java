/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * 
 */
public class InterestRateFutureOptionTradeConverter {
  private final InterestRateFutureOptionSecurityConverter _securityConverter;

  public InterestRateFutureOptionTradeConverter(final InterestRateFutureOptionSecurityConverter securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InstrumentDefinition<?> convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof IRFutureOptionSecurity, "Can only handle trades with security type IRFutureOptionSecurity");
    final Object securityDefinition = _securityConverter.convert((IRFutureOptionSecurity) trade.getSecurity());
    final int quantity = 1; // trade.getQuantity().intValue(); TODO: correct when position/trade dilemma is solved.
    //TODO trade time or premium time?
    //    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getPremiumDate().atTime(trade.getPremiumTime()),
    //        TimeZone.UTC); //TODO get the real time zone
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO get the real time zone
    final double tradePrice = trade.getPremium() == null ? 0 : trade.getPremium() / 100; //TODO remove the default value and throw an exception
    // TODO: if the premium the right place to store the trade price?
    if (securityDefinition instanceof InterestRateFutureOptionMarginSecurityDefinition) {
      final InterestRateFutureOptionMarginSecurityDefinition underlyingOption = (InterestRateFutureOptionMarginSecurityDefinition) securityDefinition;
      return new InterestRateFutureOptionMarginTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
    }
    final InterestRateFutureOptionPremiumSecurityDefinition underlyingOption = (InterestRateFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new InterestRateFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
  }
}
