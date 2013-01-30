/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

/**
 * An implementation of {@link MarketDataSnapshot} backed by an {@link InMemoryLKVMarketDataProvider}.
 */
public class InMemoryLKVMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataSnapshot.class);
  
  private final InMemoryLKVMarketDataProvider _provider;
  private Instant _snapshotTime;
  private Map<ValueRequirement, ComputedValue> _snapshot;
  
  public InMemoryLKVMarketDataSnapshot(InMemoryLKVMarketDataProvider provider) {
    _provider = provider;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "InMemoryLKVMarketDataSnapshot:" + getSnapshotTime());
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime != null ? _snapshotTime : Instant.now();
  }
  
  @Override
  public void init() {
    init(null, 0, null);
  }

  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    _snapshot = getProvider().doSnapshot();
    _snapshotTime = Instant.now();
    s_logger.debug("Snapshotted at {}", _snapshotTime);
  }

  @Override
  public Instant getSnapshotTime() {
    return _snapshotTime;
  }

  @Override
  public ComputedValue query(ValueRequirement requirement) {
    return getSnapshot().get(requirement);
  }

  //-------------------------------------------------------------------------
  public Set<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableSet(getSnapshot().keySet());
  }
  
  //-------------------------------------------------------------------------
  private Map<ValueRequirement, ComputedValue> getSnapshot() {
    if (_snapshot == null) {
      throw new IllegalStateException("Snapshot has not been initialised");
    }
    return _snapshot;
  }
  
  private InMemoryLKVMarketDataProvider getProvider() {
    return _provider;
  }

}
