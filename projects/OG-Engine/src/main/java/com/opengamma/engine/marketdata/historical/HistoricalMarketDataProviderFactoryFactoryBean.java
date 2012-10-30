/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link HistoricalMarketDataProviderFactory}.
 */
public class HistoricalMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<HistoricalMarketDataProviderFactory> {

  private HistoricalTimeSeriesSource _timeSeriesSource;
  private SecuritySource _securitySource;
  
  public HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  public void setTimeSeriesSource(HistoricalTimeSeriesSource timeSeriesSource) {
    _timeSeriesSource = timeSeriesSource;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  @Override
  protected HistoricalMarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getTimeSeriesSource(), "timeSeriesSource");
    return new HistoricalMarketDataProviderFactory(getTimeSeriesSource(), getSecuritySource());
  }

}
