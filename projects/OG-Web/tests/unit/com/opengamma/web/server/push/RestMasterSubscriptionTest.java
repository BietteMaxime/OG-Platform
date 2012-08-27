/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import java.io.IOException;

import javax.time.Instant;

import org.eclipse.jetty.server.Server;
import org.json.JSONException;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.push.WebPushTestUtils;

public class RestMasterSubscriptionTest {

  private Server _server;
  private TestChangeManager _positionChangeManager;
  private WebPushTestUtils _webPushTestUtils = new WebPushTestUtils();

  @BeforeClass
  public void createServer() throws Exception {
    Pair<Server,WebApplicationContext> serverAndContext = _webPushTestUtils.createJettyServer("classpath:/com/opengamma/web/server/push/rest-subscription-test.xml");
    _server = serverAndContext.getFirst();
    WebApplicationContext context = serverAndContext.getSecond();
    _positionChangeManager = context.getBean("positionChangeManager", TestChangeManager.class);
  }

  @AfterClass
  public void tearDown() throws Exception {
    _server.stop();
  }

  @Test
  public void masterSubscription() throws IOException, JSONException {
    String clientId = _webPushTestUtils.handshake();
    String restUrl = "/jax/test/positions";
    // this REST request should set up a subscription for any changes in the position master
    _webPushTestUtils.readFromPath(restUrl, clientId);
    // send a change event
    UniqueId uid = UniqueId.of("Tst", "101");
    _positionChangeManager.entityChanged(ChangeType.UPDATED, uid, uid, Instant.now());
    // connect to the long-polling URL to receive notification of the change
    String json = _webPushTestUtils.readFromPath("/updates/" + clientId);
    _webPushTestUtils.checkJsonResults(json, restUrl);
  }
}
