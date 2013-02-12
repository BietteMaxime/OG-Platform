/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test.
 */
@Test(groups = "unit")
public class EHCachingDistributionSpecificationResolverTest {

  private CacheManager _cacheManager;

  @BeforeMethod
  public void setUp() {
    _cacheManager = CacheManager.newInstance();
  }

  @AfterMethod
  public void tearDown() {
    _cacheManager = EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  //-------------------------------------------------------------------------
  public void testCaching() {
    ExternalId id = ExternalId.of("foo", "bar");
    
    LiveDataSpecification request = new LiveDataSpecification(
        "TestNormalization",
        ExternalId.of("foo", "bar"));
    
    DistributionSpecification distributionSpec = new DistributionSpecification(id, StandardRules.getNoNormalization(), "testtopic");
    Map<LiveDataSpecification, DistributionSpecification> returnValue = new HashMap<LiveDataSpecification, DistributionSpecification>();
    returnValue.put(request, distributionSpec);        
    
    DistributionSpecificationResolver underlying = mock(DistributionSpecificationResolver.class);
    when(underlying.resolve(Collections.singletonList(request))).thenReturn(returnValue);
    
    EHCachingDistributionSpecificationResolver resolver = new EHCachingDistributionSpecificationResolver(underlying, _cacheManager);
    assertEquals(distributionSpec, resolver.resolve(request));
    assertEquals(distributionSpec, resolver.resolve(request));
    
    verify(underlying, times(1)).resolve(Collections.singletonList(request));
  }

}
