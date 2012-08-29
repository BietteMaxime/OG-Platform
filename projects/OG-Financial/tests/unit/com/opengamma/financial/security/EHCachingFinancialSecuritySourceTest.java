/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Test EHCachingFinancialSecuritySource.
 */
public class EHCachingFinancialSecuritySourceTest {

  private MockFinancialSecuritySource _underlyingSecuritySource = null;
  private EHCachingFinancialSecuritySource _cachingSecuritySource = null;
  private ExternalId _secId1 = ExternalId.of("d1", "v1");
  private ExternalId _secId2 = ExternalId.of("d1", "v2");
  private SimpleSecurity _security1 = new SimpleSecurity("");
  private SimpleSecurity _security1_alternate = new SimpleSecurity("alternate");
  private SimpleSecurity _security2 = new SimpleSecurity("");

  @BeforeMethod
  public void setUp() throws Exception {    
    _underlyingSecuritySource = new MockFinancialSecuritySource();
    _cachingSecuritySource = new EHCachingFinancialSecuritySource(_underlyingSecuritySource, EHCacheUtils.createCacheManager ());
    
    _security1.addExternalId(_secId1);
    _security1_alternate.addExternalId(_secId1);
    _security2.addExternalId(_secId2);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _underlyingSecuritySource = null;
    if (_cachingSecuritySource != null) {
      _cachingSecuritySource.shutdown();
    }
    _cachingSecuritySource = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurity_UniqueId() {
    addSecuritiesToMock(_security1, _security2);
    
    UniqueId uid1 = _security1.getUniqueId();
    Security underlyingSec = _underlyingSecuritySource.getSecurity(uid1);
    Security cachedSec = _cachingSecuritySource.getSecurity(uid1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    assertEquals(1, singleSecCache.getSize());
    Element element = singleSecCache.getQuiet(uid1);
    assertNotNull(element);
    for (int i = 1; i < 10; i++) {
      cachedSec = _cachingSecuritySource.getSecurity(uid1);
      assertNotNull(cachedSec);
    }
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void getSecurity_UniqueId_empty() {
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    
    UniqueId uid = UniqueId.of("Mock", "99");
    try {
      _cachingSecuritySource.getSecurity(uid);
    } finally {
      assertEquals(0, singleSecCache.getSize());
      Element element = singleSecCache.get(uid);
      assertNull(element);
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurities_ExternalIdBundle() {
    addSecuritiesToMock(_security1, _security2);
    ExternalIdBundle secKey = ExternalIdBundle.of(_secId1, _secId2);
    
    Collection<Security> underlyingSecurities = _underlyingSecuritySource.getSecurities(secKey);
    assertNotNull(underlyingSecurities);
    Collection<Security> cachedSecurities = _cachingSecuritySource.getSecurities(secKey);
    assertNotNull(cachedSecurities);
    assertEquals(underlyingSecurities, cachedSecurities);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    assertNotNull(cacheManager);
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    assertNotNull(singleSecCache);
    assertEquals(2, singleSecCache.getSize());

    Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    Element sec2Element = singleSecCache.getQuiet(_security2.getUniqueId());
    assertNotNull(sec1Element);
    assertNotNull(sec2Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey);
      assertEquals(0, sec1Element.getHitCount());
      assertEquals(0, sec2Element.getHitCount());
    }
  }

  @Test
  public void getSecurity_ExternalIdBundle_Changing() {
    ExternalIdBundle secKey = ExternalIdBundle.of(_secId1, _secId2);
    addSecuritiesToMock(_security1);
    
    Security underlyingSecurity = _cachingSecuritySource.getSecurity(secKey, VersionCorrection.LATEST);
    assertEquals(_security1, underlyingSecurity);
    
    _underlyingSecuritySource.removeSecurity(_security1);
    addSecuritiesToMock(_security1_alternate);
    
    underlyingSecurity = _cachingSecuritySource.getSecurity(secKey, VersionCorrection.LATEST);
    assertEquals(_security1_alternate, underlyingSecurity);
  }
  
  @Test
  public void getSecurities_ExternalIdBundle_empty() {
    ExternalIdBundle secKey = ExternalIdBundle.of(_secId1);
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
  }

  //-------------------------------------------------------------------------
  @Test
  public void getSecurity_ExternalIdBundle() {
    addSecuritiesToMock(_security1, _security2);
    
    ExternalIdBundle secKey1 = ExternalIdBundle.of(_secId1);
    Security underlyingSec = _underlyingSecuritySource.getSecurity(secKey1);
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey1);
    assertNotNull(underlyingSec);
    assertNotNull(cachedSec);
    assertSame(underlyingSec, cachedSec);
    
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    assertNotNull(cacheManager);
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    assertNotNull(singleSecCache);
    assertEquals(1, singleSecCache.getSize());
    
    Element sec1Element = singleSecCache.getQuiet(_security1.getUniqueId());
    assertNotNull(sec1Element);
    for (int i = 1; i < 10; i++) {
      _cachingSecuritySource.getSecurities(secKey1);
      assertEquals(0, sec1Element.getHitCount());
    }
  }

  @Test
  public void getSecurity_ExternalIdBundle_empty() {
    ExternalIdBundle secKey = ExternalIdBundle.of(_secId1);
    CacheManager cacheManager = _cachingSecuritySource.getCacheManager();
    Cache singleSecCache = cacheManager.getCache(EHCachingFinancialSecuritySource.SINGLE_SECURITY_CACHE);
    
    Security cachedSec = _cachingSecuritySource.getSecurity(secKey);
    assertNull(cachedSec);
    assertEquals(0, singleSecCache.getSize());
  }

  private void addSecuritiesToMock(Security ... securities) {
    for (Security security : securities) {
      _underlyingSecuritySource.addSecurity(security);
    }
  }

}
