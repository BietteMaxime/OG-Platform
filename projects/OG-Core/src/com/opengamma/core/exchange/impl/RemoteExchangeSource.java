/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.exchange.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.ObjectChangeListener;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to an {@link ExchangeSource}.
 */
public class RemoteExchangeSource extends AbstractRemoteSource<Exchange> implements ExchangeSource {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteExchangeSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(Exchange.class);
  }

  @Override
  public Exchange get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataExchangeSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(Exchange.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<? extends Exchange> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    
    URI uri = DataExchangeSourceResource.uriSearch(getBaseUri(), versionCorrection, bundle);
    return accessRemote(uri).get(FudgeListWrapper.class).getList();
  }

  //-------------------------------------------------------------------------
  @Override
  public Exchange getSingle(final ExternalId identifier) {
    try {
      return getSingle(ExternalIdBundle.of(identifier));
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

  @Override
  public Exchange getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    
    try {
      URI uri = DataExchangeSourceResource.uriSearchSingle(getBaseUri(), bundle);
      return accessRemote(uri).get(Exchange.class);
    } catch (DataNotFoundException ex) {
      return null;
    } catch (UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

}
