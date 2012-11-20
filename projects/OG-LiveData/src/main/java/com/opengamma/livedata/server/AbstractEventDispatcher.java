/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * A job that gets events from the underlying market data API,
 * then sends them to {@code AbstractLiveDataServer}.
 */
public abstract class AbstractEventDispatcher extends TerminatableJob {
  
  private static final Logger s_logger = LoggerFactory
    .getLogger(AbstractEventDispatcher.class);
  
  private static final long MAX_WAIT_MILLISECONDS = 1000;
  
  private StandardLiveDataServer _server;
  
  public AbstractEventDispatcher(StandardLiveDataServer server) {
    ArgumentChecker.notNull(server, "Live Data Server");
    _server = server;
  }
  
  /**
   * @return the server
   */
  public StandardLiveDataServer getServer() {
    return _server;
  }

  @Override
  protected void runOneCycle() {
    try {
      dispatch(MAX_WAIT_MILLISECONDS);
    } catch (RuntimeException e) {
      s_logger.error("Failed to dispatch", e);      
    }
  }
  
  protected void disconnected() {
    _server.setConnectionStatus(StandardLiveDataServer.ConnectionStatus.NOT_CONNECTED);
    terminate();
  }
  
  protected abstract void dispatch(long maxWaitMilliseconds);

}
