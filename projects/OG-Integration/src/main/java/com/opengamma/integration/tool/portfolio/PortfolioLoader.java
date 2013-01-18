package com.opengamma.integration.tool.portfolio;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads a portfolio from a file location. Primarily used by the {@link PortfolioLoaderTool} but split out to allow
 * for reuse in other contexts.
 */
public class PortfolioLoader {

  /**
   * Tool context for executing the load - must not be null.
   */
  private final ToolContext _toolContext;

  /**
   * The name for the portfolio - must not be null.
   */
  private final String _portfolioName;

  /**
   * The security type for the portfolio (if not a multi-asset portfolio).
   */
  private final String _securityType;

  /**
   * The filename to read the portfolio from - must not be null.
   */
  private final String _fileName;

  /**
   * Should the data actually be written to the masters.
   */
  private final boolean _write;

  /**
   * Should existing data with the same name be overwritten.
   */
  private final boolean _overwrite;

  /**
   * Should the output be vebose.
   */
  private final boolean _verbose;

  /**
   * Should the version hashes in the multi-asset zip file be ignored.
   */
  private final boolean _ignoreVersion;

  /**
   * Constructs a new portfolio loader ready to load a portfolio from file.
   *
   * @param toolContext tool context for executing the load - must not be null
   * @param portfolioName the name for the portfolio - must not be null when write is true
   * @param securityType the security type for the portfolio (if not a multi-asset portfolio)
   * @param fileName the filename to read the portfolio from - must not be null
   * @param write should the data actually be written to the masters
   * @param overwrite should existing daat with the same name be overwritten
   * @param verbose should the output be vebose
   * @param ignoreVersion should the version hashes in the multi-asset zip file be ignored
   */
  public PortfolioLoader(ToolContext toolContext, String portfolioName, String securityType, String fileName,
                         boolean write, boolean overwrite, boolean verbose, boolean ignoreVersion) {

    ArgumentChecker.notNull(toolContext, "toolContext ");
    ArgumentChecker.isTrue(!write || portfolioName != null, "Portfolio name must be specified if writing to a master");
    ArgumentChecker.notNull(fileName, "fileName ");

    _toolContext = toolContext;
    _fileName = fileName;
    _portfolioName = portfolioName;
    _write = write;
    _overwrite = overwrite;
    _securityType = securityType;
    _verbose = verbose;
    _ignoreVersion = ignoreVersion;
  }

  /**
   * Execute the portfolio load with the configured options.
   */
  public void execute() {

    PortfolioWriter portfolioWriter = constructPortfolioWriter(_toolContext, _portfolioName, _write, _overwrite);
    PortfolioReader portfolioReader = constructPortfolioReader(_fileName, _securityType, _ignoreVersion);
    SimplePortfolioCopier portfolioCopier = new SimplePortfolioCopier();

    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor =
        _verbose ? new VerbosePortfolioCopierVisitor() : new QuietPortfolioCopierVisitor();

    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(portfolioReader, portfolioWriter, portfolioCopierVisitor);

    // close stuff
    portfolioReader.close();
    portfolioWriter.close();
  }

  private PortfolioWriter constructPortfolioWriter(ToolContext toolContext, String portfolioName, boolean write, boolean overwrite) {

    if (write) {

      // Check that the portfolio name was specified
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }

      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPortfolioWriter(
          portfolioName,
          toolContext.getPortfolioMaster(),
          toolContext.getPositionMaster(),
          toolContext.getSecurityMaster(),
          overwrite, false, false, false);

    } else {

      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new PrettyPrintingPortfolioWriter(true);
    }
  }

  private PortfolioReader constructPortfolioReader(String filename, String securityType, boolean ignoreVersion) {

    SheetFormat sheetFormat = SheetFormat.of(filename);
    switch (sheetFormat) {

      case CSV:
      case XLS:
        // Check that the asset class was specified on the command line
        if (securityType == null) {
          throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename);
        } else {
//          if (securityType.equalsIgnoreCase("exchangetraded")) {
//            return new SingleSheetSimplePortfolioReader(filename, new ExchangeTradedRowParser(s_context.getBloombergSecuritySource()));
//          } else {
          return new SingleSheetSimplePortfolioReader(filename, securityType);
//          }
        }

      case ZIP:
        // Create zipped multi-asset class loader
        return new ZippedPortfolioReader(filename, ignoreVersion);

      default:
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
    }
  }
}
