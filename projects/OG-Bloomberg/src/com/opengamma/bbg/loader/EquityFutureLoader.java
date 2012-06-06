/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.*;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/** Creates EquityFutureSecurity from fields loaded from Bloomberg */
public class EquityFutureLoader extends SecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityFutureLoader.class);

  /** The fields to load from Bloomberg */
  private static final Set<String> BLOOMBERG_EQUITY_FUTURE_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_FUT_LONG_NAME,
      FIELD_FUT_LAST_TRADE_DT,
      FIELD_FUT_TRADING_HRS,
      FIELD_ID_MIC_PRIM_EXCH,
      FIELD_CRNCY,
      FIELD_MARKET_SECTOR_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_UNDL_SPOT_TICKER,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1,
      FIELD_FUT_VAL_PT));

  /** The set of valid Bloomberg 'Futures Category Types' that will map to EquityFutureSecurity */
  public static final Set<String> VALID_SECURITY_TYPES = ImmutableSet.of(BLOOMBERG_EQUITY_INDEX_TYPE);

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public EquityFutureLoader(ReferenceDataProvider referenceDataProvider) {
    super(s_logger, referenceDataProvider, SecurityType.EQUITY_FUTURE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(FudgeMsg fieldData) {
    String expiryDate = fieldData.getString(FIELD_FUT_LAST_TRADE_DT);
    String futureTradingHours = fieldData.getString(FIELD_FUT_TRADING_HRS);
    String micExchangeCode = fieldData.getString(FIELD_ID_MIC_PRIM_EXCH);
    String currencyStr = fieldData.getString(FIELD_CRNCY);
    String underlyingTicker = fieldData.getString(FIELD_UNDL_SPOT_TICKER);
    String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUT_LONG_NAME), " ");
    String category = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_FUTURES_CATEGORY), " ");
    String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    String marketSector = fieldData.getString(FIELD_MARKET_SECTOR_DES);
    String unitAmount = fieldData.getString(FIELD_FUT_VAL_PT);

    if (!isValidField(bbgUnique)) {
      s_logger.warn("bbgUnique is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(expiryDate)) {
      s_logger.warn("expiry date is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(futureTradingHours)) {
      s_logger.warn("futures trading hours is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(micExchangeCode)) {
      s_logger.warn("settlement exchange is null, cannot construct EquityFutureSecurity");
      return null;
    }
    if (!isValidField(currencyStr)) {
      s_logger.info("currency is null, cannot construct EquityFutureSecurity");
      return null;
    }
    ExternalId underlying = null;
    if (underlyingTicker != null) {
      underlying = ExternalSchemes.bloombergTickerSecurityId(underlyingTicker + " " + marketSector);
    }

    Currency currency = Currency.parse(currencyStr);

    Expiry expiry = decodeExpiry(expiryDate, futureTradingHours);
    if (expiry == null) {
      return null;
    }

    // FIXME: Case - treatment of Settlement Date
    s_logger.warn("Creating EquityFutureSecurity - settlementDate set equal to expiryDate. Missing lag.");
    ZonedDateTime settlementDate = expiry.getExpiry();

    EquityFutureSecurity security = new EquityFutureSecurity(expiry, micExchangeCode, micExchangeCode, currency, Double.valueOf(unitAmount), settlementDate, underlying, category);

    // set identifiers
    parseIdentifiers(fieldData, security);
    return security;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_EQUITY_FUTURE_FIELDS;
  }

}
