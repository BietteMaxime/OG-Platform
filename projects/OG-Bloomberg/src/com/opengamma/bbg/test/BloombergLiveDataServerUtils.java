/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import java.lang.reflect.Method;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.CachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.CombiningBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionBloombergLiveDataServer;
import com.opengamma.bbg.livedata.faketicks.FakeSubscriptionSelector;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.CombiningLiveDataServer;

/**
 * Test utilities for Bloomberg.
 */
public class BloombergLiveDataServerUtils {

  /**
   * Gets a reference data provider for a class, defined by a method.
   * 
   * @param testMethod  the test method, not null
   * @return the data provider, not null
   */
  public static CachingReferenceDataProvider getCachingReferenceDataProvider(Method testMethod) {
    return getCachingReferenceDataProvider(testMethod.getClass());
  }

  /**
   * Gets a reference data provider for a class.
   * 
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  public static CachingReferenceDataProvider getCachingReferenceDataProvider(Class<?> testClass) {
    BloombergReferenceDataProvider brdp = getUnderlyingProvider();
    return getCachingReferenceDataProvider(brdp, testClass);
  }

  /**
   * Adds caching to a reference data provider.
   * 
   * @param underlying  the underlying provider, not null
   * @param testClass  the test class, not null
   * @return the data provider, not null
   */
  private static CachingReferenceDataProvider getCachingReferenceDataProvider(ReferenceDataProvider underlying, Class<?> testClass) {
    return MongoCachedReferenceData.makeMongoProvider(underlying, testClass);
  }

  /**
   * Creates a Bloomberg reference data provider, that has been started, for testing.
   * 
   * @return the provider, not null
   */
  public static BloombergReferenceDataProvider getUnderlyingProvider() {
    SessionOptions options = BloombergTestUtils.getSessionOptions();
    BloombergReferenceDataProvider brdp = new BloombergReferenceDataProvider(options);
    brdp.start();
    return brdp;
  }

  /**
   * Stops the specified reference data provider.
   * 
   * @param refDataProvider  the provider to stop, null ignored
   */
  public static void stopCachingReferenceDataProvider(CachingReferenceDataProvider refDataProvider) {
    if (refDataProvider != null) {
      ReferenceDataProvider underlying = refDataProvider.getUnderlying();
      if (underlying instanceof CachingReferenceDataProvider) {
        stopCachingReferenceDataProvider((CachingReferenceDataProvider) underlying);
      } else if (underlying instanceof BloombergReferenceDataProvider) {
        BloombergReferenceDataProvider bbgProvider = (BloombergReferenceDataProvider) refDataProvider.getUnderlying();
        bbgProvider.stop();
      }
    }
  }

  //-------------------------------------------------------------------------
  public static BloombergLiveDataServer startTestServer(Method testMethod) {
    return startTestServer(testMethod.getClass());
  }

  public static BloombergLiveDataServer startTestServer(Class<?> testClass) {
    CachingReferenceDataProvider refDataProvider = getCachingReferenceDataProvider(testClass);
    return getTestServer(refDataProvider);
  }

  public static void stopTestServer(BloombergLiveDataServer testServer) {
    stopCachingReferenceDataProvider(testServer.getCachingReferenceDataProvider());
    testServer.stop();
  }

  public static BloombergLiveDataServer getTestServer(CachingReferenceDataProvider cachingRefDataProvider) {
    SessionOptions options = BloombergTestUtils.getSessionOptions();
    
    BloombergLiveDataServer server = new BloombergLiveDataServer(options, cachingRefDataProvider);
    DistributionSpecificationResolver distributionSpecificationResolver = server.getDefaultDistributionSpecificationResolver();
    server.setDistributionSpecificationResolver(distributionSpecificationResolver);
    
    server.start();
    return server;
  }

  public static CombiningBloombergLiveDataServer startTestServer(Class<?> testClass, FakeSubscriptionSelector subscriptionSelector, ReferenceDataProvider refDataProvider) {
    CachingReferenceDataProvider cachingRefDataProvider = getCachingReferenceDataProvider(refDataProvider, testClass);
    BloombergLiveDataServer underlying = getTestServer(cachingRefDataProvider);
    
    FakeSubscriptionBloombergLiveDataServer fakeServer = new FakeSubscriptionBloombergLiveDataServer(underlying);
    fakeServer.start();
    
    CombiningBloombergLiveDataServer combinedServer = new CombiningBloombergLiveDataServer(fakeServer, underlying, subscriptionSelector);
        
    combinedServer.start();
    return combinedServer;
  }

  public static void stopTestServer(AbstractLiveDataServer server) {
    if (server instanceof BloombergLiveDataServer) {
      stopTestServer((BloombergLiveDataServer) server);
    } else if (server instanceof CombiningLiveDataServer) {
      stopTestServer(((CombiningBloombergLiveDataServer) server).getFakeServer());
    }
  }

}
