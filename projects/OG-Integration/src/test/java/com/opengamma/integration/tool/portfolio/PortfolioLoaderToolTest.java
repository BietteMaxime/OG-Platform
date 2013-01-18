package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * Test the portfolio loader tool behaves as expected.
 */
@Test
public class PortfolioLoaderToolTest {

  private final ToolContext _toolContext = new ToolContext();
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private File _tempFile;

  @BeforeMethod
  public void setup() throws IOException {

    _tempFile = File.createTempFile("portfolio-", ".csv");
    System.out.println("Created temp file: " + _tempFile.getAbsolutePath());
    _portfolioMaster = new InMemoryPortfolioMaster();
    _positionMaster = new InMemoryPositionMaster();
    _toolContext.setPortfolioMaster(_portfolioMaster);
    _toolContext.setPositionMaster(_positionMaster);
    _toolContext.setSecurityMaster(new InMemorySecurityMaster());
  }

  @AfterMethod
  public void tearDown() {

    // Clean up the file we were using
    if (_tempFile != null && _tempFile.exists()) {
      System.out.println("Removing file: " + _tempFile.getAbsolutePath());
      _tempFile.delete();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testToolContextMustBeProvided() {
    new PortfolioLoader(null, "My portfolio", "Equity", _tempFile.getAbsolutePath(), true, true, false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPortfolioNameMustBeProvided() {
    new PortfolioLoader(_toolContext, null, "Equity", _tempFile.getAbsolutePath(), true, true, false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFilenameMustBeProvided() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", null, true, true, false, true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testFileMustHaveRecognisedExtension() {
    new PortfolioLoader(_toolContext, "My portfolio", "Equity", "some_file.goobledygook", true, true, false, true).execute();
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
  public void testLoadCashFlowPortfolio() throws IOException {

    String data = "\"amount\",\"currency\",\"settlement\",\"externalIdBundle\",\"position:quantity\",\"securityType\",\"shortName\",\"trade:counterpartyExternalId\",\"trade:deal\",\"trade:premium\",\"trade:premiumCurrency\",\"trade:premiumDate\",\"trade:premiumTime\",\"trade:quantity\",\"trade:tradeDate\",\"trade:tradeTime\"\n" +
        "150000,\"USD\",\"2014-01-01T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF001\",\"4\",\"CASHFLOW\",\"CPID~123\",,,,,,,,\n" +
        "60000,\"EUR\",\"2014-02-02T00:00:00+00:00[Europe/London]\",\"SOME_ID~CF002\",\"2\",\"CASHFLOW\",\"CPID~234\",,,,,,,,\n";

    doPortfolioLoadTest("Cashflow Portfolio", "CashFlow", data, 1, 2);
  }

  private void doPortfolioLoadTest(String portfolioName, String securityType, String data, int expectedPortfolios, int expectedPositions) {

    populateFileWithData(data);

    new PortfolioLoader(_toolContext, portfolioName, securityType, _tempFile.getAbsolutePath(), true, true, false, true).execute();

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
