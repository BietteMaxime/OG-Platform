/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.impl.NonVersionedRedisSecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.MetricProducer;

/*
 * REDIS DATA STRUCTURES:
 * Portfolio Unique ID Lookups:
 *     Key["NAME-"Name] -> Hash
 *        Hash[UNIQUE_ID] -> UniqueId for the portfolio
 * Portfolio objects themselves:
 *     Key["PRT-"UniqueId] -> Hash
 *        Hash[NAME] -> Name
 *        HASH["ATT-"AttributeName] -> Attribute Value
 * Portfolio contents:
 *     Key["PRT-"UniqueId"-POS"] -> Set
 *        Each item in the list is a UniqueId for a position
 * Positions:
 *     Key["POS-"UniqueId] -> Hash
 *        Hash[QTY] -> Quantity
 *        Hash[SEC] -> ExternalId for the security
 *        Hash["ATT-"AttributeName] -> Attribute Value
 * 
 */

/**
 * A lightweight {@link PositionSource} that cannot handle any versioning, and
 * which stores all positions and portfolios as Redis-native data structures
 * (rather than Fudge encoding).
 */
public class NonVersionedRedisPositionSource implements PositionSource, MetricProducer {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisSecuritySource.class);
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private Timer _getPortfolioTimer = new Timer();
  private Timer _getPositionTimer = new Timer();
  private Timer _portfolioStoreTimer = new Timer();
  private Timer _positionStoreTimer = new Timer();
  private Timer _positionSetTimer = new Timer();
  private Timer _positionAddTimer = new Timer();
  
  public NonVersionedRedisPositionSource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisPositionSource(JedisPool jedisPool, String redisPrefix) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
  }
  
  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getPortfolioTimer = summaryRegistry.timer(namePrefix + ".getPortfolio");
    _getPositionTimer = summaryRegistry.timer(namePrefix + ".getPosition");
    _portfolioStoreTimer = summaryRegistry.timer(namePrefix + ".portfolioStore");
    _positionStoreTimer = summaryRegistry.timer(namePrefix + ".positionStore");
    _positionSetTimer = summaryRegistry.timer(namePrefix + ".positionSet");
    _positionAddTimer = summaryRegistry.timer(namePrefix + ".positionAdd");
  }


  // ---------------------------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------------------------
  
  protected String toPortfolioRedisKey(UniqueId uniqueId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("PRT-");
    sb.append(uniqueId);
    String keyText = sb.toString();
    return keyText;
  }
  
  protected String toPositionRedisKey(UniqueId uniqueId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("POS-");
    sb.append(uniqueId);
    String keyText = sb.toString();
    return keyText;
  }
  
  // ---------------------------------------------------------------------------------------
  // DATA MANIPULATION
  // ---------------------------------------------------------------------------------------
  
  /**
   * Deep store an entire portfolio, including all positions.
   * The portfolio itself is not modified, including setting the unique ID.
   * 
   * @param portfolio The portfolio to store.
   * @return the UniqueId of the portfolio.
   */
  public UniqueId storePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    UniqueId uniqueId = null;
    
    try (Timer.Context context = _portfolioStoreTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        uniqueId = storePortfolio(jedis, portfolio);
        storePortfolioNodes(jedis, toPortfolioRedisKey(uniqueId) + "-POS", portfolio.getRootNode());
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store portfolio " + portfolio, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store portfolio " + portfolio, e);
      }
      
    }
    
    return uniqueId;
  }
  
  public UniqueId storePosition(Position position) {
    ArgumentChecker.notNull(position, "position");
    UniqueId uniqueId = null;
    
    try (Timer.Context context = _positionStoreTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        uniqueId = storePosition(jedis, position);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }
      
    }
    
    return uniqueId;
  }
  
  /**
   * A special fast-pass method to just update a position quantity, without
   * updating any of the other fields. Results in a single Redis write.
   * 
   * @param position The position, which must already be in the source.
   */
  public void updatePositionQuantity(Position position) {
    ArgumentChecker.notNull(position, "position");
    
    try (Timer.Context context = _positionSetTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        String redisKey = toPositionRedisKey(position.getUniqueId());
        jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }
      
    }
  }
  
  /**
   * Store a new position and attach it to the specified portfolio.
   * @param portfolio the existing portfolio. Must already be in this source.
   * @param position the new position to store and attach.
   */
  public void addPositionToPortfolio(Portfolio portfolio, Position position) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(portfolio.getUniqueId(), "portfolio UniqueId");
    ArgumentChecker.notNull(position, "position");
    
    try (Timer.Context context = _positionAddTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        UniqueId uniqueId = storePosition(jedis, position);
        jedis.sadd(toPortfolioRedisKey(portfolio.getUniqueId()) + "-POS", uniqueId.toString());
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }
      
    }
    
  }
  
  protected UniqueId storePortfolio(Jedis jedis, Portfolio portfolio) {
    UniqueId uniqueId = portfolio.getUniqueId();
    if (uniqueId == null) {
      uniqueId = UniqueId.of("UUID", UUID.randomUUID().toString());
    }
    
    String redisKey = toPortfolioRedisKey(uniqueId);
    jedis.hset("NAME-" + portfolio.getName(), "UNIQUE_ID", uniqueId.toString());
    
    jedis.hset(redisKey, "NAME", portfolio.getName());
    
    for (Map.Entry<String, String> attribute : portfolio.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }
    
    return uniqueId;
  }
  
  protected void storePortfolioNodes(Jedis jedis, String redisKey, PortfolioNode node) {
    Set<String> positionUniqueIds = new HashSet<String>();
    for (Position position : node.getPositions()) {
      UniqueId uniqueId = storePosition(jedis, position);
      positionUniqueIds.add(uniqueId.toString());
    }
    jedis.sadd(redisKey, positionUniqueIds.toArray(new String[0]));
    
    if (!node.getChildNodes().isEmpty()) {
      s_logger.warn("Possible misuse. Portfolio has a deep structure, but this source flattens. Positions being stored flat.");
    }
    for (PortfolioNode childNode : node.getChildNodes()) {
      storePortfolioNodes(jedis, redisKey, childNode);
    }
  }
  
  protected UniqueId storePosition(Jedis jedis, Position position) {
    UniqueId uniqueId = position.getUniqueId();
    if (uniqueId == null) {
      uniqueId = UniqueId.of("UUID", UUID.randomUUID().toString());
    }
    
    String redisKey = toPositionRedisKey(uniqueId);
    jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());
    ExternalIdBundle securityBundle = position.getSecurityLink().getExternalId();
    if (securityBundle == null) {
      throw new OpenGammaRuntimeException("Can only store positions with a link to an ExternalId");
    }
    if (securityBundle.size() != 1) {
      s_logger.warn("Bundle {} not exactly one. Possible misuse of this source.", securityBundle);
    }
    ExternalId securityId = securityBundle.iterator().next();
    jedis.hset(redisKey, "SEC", securityId.toString());
    
    for (Map.Entry<String, String> attribute : position.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }
    
    if ((position.getTrades() != null) && !position.getTrades().isEmpty()) {
      s_logger.warn("Position has trades. This source does not support trades. Possible misuse.");
    }
    
    return uniqueId;
  }
  
  // ---------------------------------------------------------------------------------------
  // IMPLEMENTATION OF POSITION SOURCE
  // ---------------------------------------------------------------------------------------
  
  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Change manager not supported.");
  }

  @Override
  public Portfolio getPortfolio(UniqueId uniqueId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    SimplePortfolio portfolio = null;
    
    try (Timer.Context context = _getPortfolioTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        String redisKey = toPortfolioRedisKey(uniqueId);
        if (jedis.exists(redisKey)) {
          Map<String, String> hashFields = jedis.hgetAll(redisKey);
          
          portfolio = new SimplePortfolio(hashFields.get("NAME"));
          portfolio.setUniqueId(uniqueId);

          for (Map.Entry<String, String> field : hashFields.entrySet()) {
            if (!field.getKey().startsWith("ATT-")) {
              continue;
            }
            String attributeName = field.getKey().substring(4);
            portfolio.addAttribute(attributeName, field.getValue());
          }
          
          SimplePortfolioNode portfolioNode = new SimplePortfolioNode();
          portfolioNode.setName(portfolio.getName());
          
          Set<String> positionUniqueIds = jedis.smembers(redisKey + "-POS");
          for (String positionUniqueId : positionUniqueIds) {
            Position position = getPosition(jedis, UniqueId.parse(positionUniqueId));
            if (position != null) {
              portfolioNode.addPosition(position);
            }
          }
          portfolio.setRootNode(portfolioNode);
        }
        
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get portfolio " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get portfolio " + uniqueId, e);
      }
      
    }
    
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection) {
    return getPortfolio(UniqueId.of(objectId, null), null);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId uniqueId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Trades not supported.");
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    SimplePosition position = null;
    
    try (Timer.Context context = _getPositionTimer.time()) {
      
      Jedis jedis = getJedisPool().getResource();
      try {
        
        position = getPosition(jedis, uniqueId);
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to get position " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get position " + uniqueId, e);
      }
      
    }
    
    return position;
  }

  protected SimplePosition getPosition(Jedis jedis, UniqueId uniqueId) {
    String redisKey = toPositionRedisKey(uniqueId);
    if (!jedis.exists(redisKey)) {
      return null;
    }
    SimplePosition position = new SimplePosition();
    position.setUniqueId(uniqueId);
    Map<String, String> hashFields = jedis.hgetAll(redisKey);
    position.setQuantity(new BigDecimal(hashFields.get("QTY")));
    ExternalId secId = ExternalId.parse(hashFields.get("SEC"));
    SimpleSecurityLink secLink = new SimpleSecurityLink();
    secLink.addExternalId(secId);
    position.setSecurityLink(secLink);
    
    for (Map.Entry<String, String> field : hashFields.entrySet()) {
      if (!field.getKey().startsWith("ATT-")) {
        continue;
      }
      String attributeName = field.getKey().substring(4);
      position.addAttribute(attributeName, field.getValue());
    }
    return position;
  }
  
  @Override
  public Position getPosition(ObjectId objectId, VersionCorrection versionCorrection) {
    return getPosition(UniqueId.of(objectId, null));
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Trades not supported.");
  }

}
