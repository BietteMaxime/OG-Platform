/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for importing data from various 3rd party file formats
 */
public abstract class SingleSheetPortfolioLoader implements PortfolioLoader {
 
  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineTool.class);

  private SheetReader _sheet;         // The spreadsheet from which to import
     
  public SingleSheetPortfolioLoader(SheetReader sheet) {
    _sheet = sheet;
  }
    
  public abstract String[] getColumns();
   
  public SheetReader getSheet() {
    return _sheet;
  }

  public void setSheet(SheetReader sheet) {
    _sheet = sheet;
  }
  
  protected void prettyPrintRow(Map<String, String> row) {
    String out = "Read in: ";
    for (String s : row.keySet()) {
      out += (s + ": " + row.get(s) + " | ");
    }
    s_logger.info(out);
  }

}
