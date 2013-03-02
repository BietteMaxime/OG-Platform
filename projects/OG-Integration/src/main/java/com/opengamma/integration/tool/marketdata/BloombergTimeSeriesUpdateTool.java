/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Updates time-series using the Bloomberg historical loader.
 */
@Scriptable
public class BloombergTimeSeriesUpdateTool extends AbstractTool<IntegrationToolContext> {

  @Override
  protected void doRun() throws Exception {
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
        getToolContext().getHistoricalTimeSeriesMaster(),
        getToolContext().getHistoricalTimeSeriesProvider(),
        new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
    loader.setUpdateDb(true);
    loader.setReload(getCommandLine().hasOption("reload"));
    loader.setEndDate(LocalDate.MAX);
    loader.run();
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(new Option("r", "reload", false, "Reload complete time series"));
    return options;
  }
  
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new BloombergTimeSeriesUpdateTool().initAndRun(args, IntegrationToolContext.class);
    System.exit(success ? 0 : 1);
  }

}
