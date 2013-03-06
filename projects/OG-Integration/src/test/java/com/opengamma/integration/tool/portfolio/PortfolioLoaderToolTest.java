/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.test.DbTest;

/**
 * Test the portfolio loader tool behaves as expected. Data should be read from a file and
 * inserted into the correct database masters.
 */
@Test
public class PortfolioLoaderToolTest extends DbTest{

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderToolTest.class);

  private ConfigurableApplicationContext _context;
  private ToolContext _toolContext;
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private File _tempFile;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public PortfolioLoaderToolTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    _tempFile = File.createTempFile("portfolio-", ".csv");
    s_logger.info("Created temp file: " + _tempFile.getAbsolutePath());

    _context = new FileSystemXmlApplicationContext("config/test-master-context.xml");
    _context.start();

    _portfolioMaster = getDbMaster("DbPortfolioMaster", PortfolioMaster.class);
    _positionMaster = getDbMaster("DbPositionMaster", PositionMaster.class);

    _toolContext = new ToolContext();
    _toolContext.setPortfolioMaster(_portfolioMaster);
    _toolContext.setPositionMaster(_positionMaster);
    _toolContext.setSecurityMaster(getDbMaster("DbSecurityMaster", SecurityMaster.class));
  }

  private <T> T getDbMaster(final String master, final Class<T> requiredType) {
    return _context.getBean(getDatabaseType() + master, requiredType);
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    if (_context != null) {
      _context.stop();
      _context.close();
      _context = null;
    }
    _toolContext = null;
    _positionMaster = null;
    _portfolioMaster = null;

    // Clean up the file we were using
    if (_tempFile != null && _tempFile.exists()) {
      s_logger.info("Removing file: " + _tempFile.getAbsolutePath());
      _tempFile.delete();
    }

    super.tearDown();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToolContextMustBeProvided() {
    new PortfolioLoader(null, "My portfolio", "Equity", _tempFile.getAbsolutePath(), true, true, false, false, false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPortfolioNameMustBeProvided() {
    new PortfolioLoader(_toolContext, null, "Equity", _tempFile.getAbsolutePath(), true, true, false, false, false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFilenameMustBeProvided() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", null, true, true, false, false, false, true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testFileMustHaveRecognisedExtension() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", "some_file.goobledygook", true, true, false, false, false, true).execute();
  }

  @Test
  public void testLoadEquityPortfolio() throws IOException {

    String data = "\"companyName\",\"currency\",\"exchange\",\"exchangeCode\",\"externalIdBundle\",\"name\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"EXXON MOBIL CORP\",\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"XNYS\",\"BLOOMBERG_BUID~EQ0010054600001000, BLOOMBERG_TICKER~XOM US Equity, CUSIP~30231G102, ISIN~US30231G1022, SEDOL1~2326618\",\"EXXON MOBIL CORP\",\"1264\",\"EQUITY\",\"XOM\",\"CPID~123\",,,,,,,,\n" +
        "\"APPLE INC\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010169500001000, BLOOMBERG_TICKER~AAPL US Equity, CUSIP~037833100, ISIN~US0378331005, SEDOL1~2046251\",\"APPLE INC\",\"257\",\"EQUITY\",\"AAPL\",\"CPID~234\",,,,,,,,\n" +
        "\"MICROSOFT CORP\",\"USD\",\"NASDAQ/NGS (GLOBAL SELECT MARKET)\",\"XNGS\",\"BLOOMBERG_BUID~EQ0010174300001000, BLOOMBERG_TICKER~MSFT US Equity, CUSIP~594918104, ISIN~US5949181045, SEDOL1~2588173\",\"MICROSOFT CORP\",\"3740\",\"EQUITY\",\"MSFT\",\"CPID~345\",,,,,,,,";

    doPortfolioLoadTest("Equity Portfolio", "Equity", data, 1, 3);
  }

  @Test
  public void testLoadEquityIndexFutureOptionPortfolio() throws IOException {

    String data = "\"currency\",\"exchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"EQUITY_INDEX_FUTURE_OPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("EquityIndexFutureOption Portfolio", "EquityIndexFutureOption", data, 1, 1);
  }

  @Test
  public void testLoadEquityIndexDividendFutureOptionPortfolio() throws IOException {

    String data = "\"currency\",\"exchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"NEW YORK STOCK EXCHANGE INC.\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"EQUITY_INDEX_DIVIDEND_FUTURE_OPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("EquityIndexDividendFutureOption Portfolio", "EquityIndexDividendFutureOption", data, 1, 1);
  }

  @Test
  public void testLoadCashFlowPortfolio() throws IOException {

    String data = "\"amount\",\"currency\",\"settlement\",\"externalIdBundle\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "150000,\"USD\",\"2014-01-01T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF001\",\"4\",\"CASHFLOW\",\"CPID~123\",,,,,,,,\n" +
        "60000,\"EUR\",\"2014-02-02T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF002\",\"2\",\"CASHFLOW\",\"CPID~234\",,,,,,,,\n";

    doPortfolioLoadTest("Cashflow Portfolio", "CashFlow", data, 1, 2);
  }

  @Test
  public void testLoadCommodityFutureOptionPortfolio() throws IOException {

    String data = "\"currency\",\"tradingExchange\",\"settlementExchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"CME\",\"CME\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"COMMODITY_FUTUREOPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("CommodityFutureOption Portfolio", "CommodityFutureOption", data, 1, 1);
  }

  @Test
  public void testLoadFxFutureOptionPortfolio() throws IOException {

    String data = "\"currency\",\"tradingExchange\",\"settlementExchange\",\"exerciseType\",\"expiry\",\"externalIdBundle\",\"underlyingId\",\"optionType\",\"position:quantity\",\"securityType\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "\"USD\",\"CME\",\"CME\",\"EX_TYPE\",\"2050-01-01T00:00:00+00:00[Europe/London]\",\"EIFO_ID~EIFO1234\",\"UNDERLYING_ID~ul9999\",\"PUT\",\"1264\",\"FX_FUTUREOPTION\",\"CPID~123\",,,,,,,,\n";

    doPortfolioLoadTest("FxFutureOption Portfolio", "FxFutureOption", data, 1, 1);
  }

  private void doPortfolioLoadTest(String portfolioName, String securityType, String data, int expectedPortfolios, int expectedPositions) {

    populateFileWithData(data);

    new PortfolioLoader(_toolContext, portfolioName, securityType, _tempFile.getAbsolutePath(), true, true, false, false, false, true).execute();

    assertEquals(_portfolioMaster.search(new PortfolioSearchRequest()).getPortfolios().size(), expectedPortfolios);
    assertEquals(_positionMaster.search(new PositionSearchRequest()).getPositions().size(), expectedPositions);
  }

  private void populateFileWithData(String data) {

    try(BufferedWriter writer = new BufferedWriter(new FileWriter(_tempFile))) {
      writer.write(data);
      writer.flush();
    } catch (IOException e) {
      fail("Unable to write data to file: " + _tempFile.getAbsolutePath());
    }
  }
}
