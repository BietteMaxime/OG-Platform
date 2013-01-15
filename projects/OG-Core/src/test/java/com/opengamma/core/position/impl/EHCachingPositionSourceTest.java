/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.math.BigDecimal;

import javax.time.Instant;

import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Tests the {@link EHCachingPositionSource} class.
 */
@Test
public class EHCachingPositionSourceTest {

  private Position createPosition(final int id) {
    return new SimplePosition(UniqueId.of("Test", Integer.toString(id)), BigDecimal.ONE, ExternalId.of("Foo", Integer.toString(id)));
  }

  private PortfolioNode createPortfolioA1() {
    final SimplePortfolioNode root = new SimplePortfolioNode("Root");
    root.setUniqueId(UniqueId.of("Node", "Root"));
    final SimplePortfolioNode a = new SimplePortfolioNode("Left");
    a.setUniqueId(UniqueId.of("Node", "Left"));
    a.addPosition(createPosition(1));
    a.addPosition(createPosition(2));
    final SimplePortfolioNode b = new SimplePortfolioNode("Right");
    b.setUniqueId(UniqueId.of("Node", "Right"));
    b.addPosition(createPosition(3));
    b.addPosition(createPosition(4));
    root.addPosition(createPosition(5));
    root.addPosition(createPosition(6));
    root.addChildNode(a);
    root.addChildNode(b);
    return root;
  }

  private PortfolioNode createPortfolioB() {
    final SimplePortfolioNode root = new SimplePortfolioNode("Foo");
    root.setUniqueId(UniqueId.of("Node", "Foo"));
    final SimplePortfolioNode a = new SimplePortfolioNode("A");
    a.setUniqueId(UniqueId.of("Node", "A"));
    a.addPosition(createPosition(7));
    a.addPosition(createPosition(8));
    root.addChildNode(a);
    root.addPosition(createPosition(9));
    root.addPosition(createPosition(10));
    return root;
  }

  private PortfolioNode createPortfolioA2() {
    final SimplePortfolioNode root = new SimplePortfolioNode("Root");
    root.setUniqueId(UniqueId.of("Node", "Root"));
    final SimplePortfolioNode a = new SimplePortfolioNode("Left");
    a.setUniqueId(UniqueId.of("Node", "Left"));
    a.addPosition(createPosition(7));
    a.addPosition(createPosition(8));
    final SimplePortfolioNode b = new SimplePortfolioNode("Right");
    b.setUniqueId(UniqueId.of("Node", "Right"));
    b.addPosition(createPosition(9));
    b.addPosition(createPosition(10));
    root.addPosition(createPosition(11));
    root.addPosition(createPosition(12));
    root.addChildNode(a);
    root.addChildNode(b);
    return root;
  }

  private PortfolioNode createPortfolioA3() {
    final SimplePortfolioNode root = new SimplePortfolioNode("Root");
    root.setUniqueId(UniqueId.of("Node", "Root"));
    final SimplePortfolioNode a = new SimplePortfolioNode("Left");
    a.setUniqueId(UniqueId.of("Node", "Left"));
    a.addPosition(createPosition(1));
    a.addPosition(createPosition(2));
    final SimplePortfolioNode b = new SimplePortfolioNode("Right");
    b.setUniqueId(UniqueId.of("Node", "Right"));
    b.addPosition(createPosition(3));
    b.addPosition(createPosition(10));
    root.addPosition(createPosition(5));
    root.addPosition(createPosition(6));
    root.addChildNode(a);
    root.addChildNode(b);
    return root;
  }

  public void addToFrontCache_missing() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    final EHCachingPositionSource cache = new EHCachingPositionSource(Mockito.mock(PositionSource.class), cacheManager);
    try {
      // Add two unrelated portfolios; both to be added and returned
      final PortfolioNode root1 = createPortfolioA1();
      final PortfolioNode root2 = createPortfolioB();
      final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now());
      assertSame(cache.addToFrontCache(root1, vc), root1);
      assertSame(cache.addToFrontCache(root2, vc), root2);
    } finally {
      cache.shutdown();
    }
  }

  public void addToFrontCache_allSame() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    final EHCachingPositionSource cache = new EHCachingPositionSource(Mockito.mock(PositionSource.class), cacheManager);
    try {
      // Add the same portfolio at the same v/c - original to be returned
      final PortfolioNode root = createPortfolioA1();
      final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now());
      assertSame(cache.addToFrontCache(root, vc), root);
      assertSame(cache.addToFrontCache(createPortfolioA1(), vc), root);
      // Add the same portfolio at a new v/c - original to be identified and returned
      assertSame(cache.addToFrontCache(root, VersionCorrection.ofVersionAsOf(vc.getVersionAsOf().plusSeconds(1))), root);
    } finally {
      cache.shutdown();
    }
  }

  public void addToFrontCache_allNew() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    final EHCachingPositionSource cache = new EHCachingPositionSource(Mockito.mock(PositionSource.class), cacheManager);
    try {
      // Add two similar portfolios with different position resolutions; both to be added and returned
      final PortfolioNode root1 = createPortfolioA1();
      final PortfolioNode root2 = createPortfolioA2();
      final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now());
      assertSame(cache.addToFrontCache(root1, vc), root1);
      assertSame(cache.addToFrontCache(root2, VersionCorrection.ofVersionAsOf(vc.getVersionAsOf().plusSeconds(1))), root2);
    } finally {
      cache.shutdown();
    }
  }

  public void addToFrontCache_someNew() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    final EHCachingPositionSource cache = new EHCachingPositionSource(Mockito.mock(PositionSource.class), cacheManager);
    try {
      // Add two similar portfolios with some positions (and hence nodes) shared; shared nodes and positions to be reused in returned node
      final PortfolioNode root1 = createPortfolioA1();
      final PortfolioNode root2 = createPortfolioA3();
      final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now());
      assertSame(cache.addToFrontCache(root1, vc), root1);
      final PortfolioNode root2b = cache.addToFrontCache(root2, VersionCorrection.ofVersionAsOf(vc.getVersionAsOf().plusSeconds(1)));
      assertNotSame(root2b, root2);
      assertEquals(root2b.getName(), root2.getName());
      assertEquals(root2b.getParentNodeId(), root2.getParentNodeId());
      assertEquals(root2b.getUniqueId(), root2.getUniqueId());
      assertEquals(root2b.getPositions(), root2.getPositions());
      assertSame(root2b.getPositions().get(0), root1.getPositions().get(0));
      assertSame(root2b.getPositions().get(1), root1.getPositions().get(1));
      assertSame(root2b.getChildNodes().get(0), root1.getChildNodes().get(0));
      assertEquals(root2b.getChildNodes().get(1), root2.getChildNodes().get(1));
      assertNotSame(root2b.getChildNodes().get(1), root2.getChildNodes().get(1));
      assertSame(root2b.getChildNodes().get(1).getPositions().get(0), root1.getChildNodes().get(1).getPositions().get(0));
      assertSame(root2b.getChildNodes().get(1).getPositions().get(1), root2.getChildNodes().get(1).getPositions().get(1));
    } finally {
      cache.shutdown();
    }
  }

}
