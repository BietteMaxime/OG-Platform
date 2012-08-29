/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;

/**
 * Test.
 */
@Test(groups = "unit")
public class ReconnectManagerTest {

  @Test
  public void reconnection() throws Exception {
    MockLiveDataServer server = new MockLiveDataServer(ExternalScheme.of("BLOOMBERG_BUID"));
    ReconnectManager manager = new ReconnectManager(server, 20);
    
    try {
      server.subscribe("foo");
      Assert.fail("Not connected yet");
    } catch (RuntimeException e) {
      // ok
    }
    
    manager.start();
    
    assertEquals(0, server.getNumConnections());
    server.connect();
    assertEquals(1, server.getNumConnections());
    
    server.subscribe("foo");
    assertEquals(1, server.getActualSubscriptions().size());
    
    Thread.sleep(50); // shouldn't reconnect
    assertEquals(1, server.getNumConnections());
    assertEquals(0, server.getNumDisconnections());
    
    manager.stop();
    server.disconnect();
    assertEquals(1, server.getNumDisconnections());
    assertEquals(1, server.getNumConnections());
    
    manager.start();
    Thread.sleep(50); // should reconnect and reestablish subscriptions
    
    assertEquals(1, server.getNumDisconnections());
    assertEquals(2, server.getNumConnections());
    assertEquals(2, server.getActualSubscriptions().size());
  }

}
