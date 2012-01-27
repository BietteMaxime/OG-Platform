package com.opengamma.examples.portfolioloader.parsers;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.examples.portfolioloader.RowParser;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

public class IRFutureParser extends RowParser {

  private static final String ID_SCHEME = "IR_FUTURE_LOADER";

  public static final String EXPIRY = "expiry";
  public static final String TRADING_EXCHANGE = "trading exchange";
  public static final String SETTLEMENT_EXCHANGE = "settlement exchange";
  public static final String CURRENCY = "currency";
  public static final String UNIT_AMOUNT = "unit amount";
  public static final String UNDERLYING_ID = "underlying id";
  public static final String NAME = "name";
  public static final String BBG_CODE = "bbg code";

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> irFutureDetails) {
    Currency ccy = Currency.of(PortfolioLoaderHelper.getWithException(irFutureDetails, CURRENCY));
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(irFutureDetails, EXPIRY), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    String tradingExchange = PortfolioLoaderHelper.getWithException(irFutureDetails, TRADING_EXCHANGE);
    String settlementExchange = PortfolioLoaderHelper.getWithException(irFutureDetails, SETTLEMENT_EXCHANGE);
    double unitAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(irFutureDetails, UNIT_AMOUNT));
    String bbgId = PortfolioLoaderHelper.getWithException(irFutureDetails, UNDERLYING_ID);
    ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);
    InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(expiry, tradingExchange, settlementExchange, ccy, unitAmount, underlyingID);
    String identifierValue = PortfolioLoaderHelper.getWithException(irFutureDetails, BBG_CODE);
    irFuture.addExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue));
    String name = PortfolioLoaderHelper.getWithException(irFutureDetails, NAME);
    irFuture.setName(name);
    irFuture.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {irFuture};
    return result;
  }

}
