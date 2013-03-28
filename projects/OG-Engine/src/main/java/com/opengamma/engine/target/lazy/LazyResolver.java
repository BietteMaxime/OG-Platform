/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Map;
import java.util.Set;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Marker interface for a target resolver that supports lazy operations.
 */
public interface LazyResolver {

  LazyResolveContext getLazyResolveContext();

  void setLazyResolveContext(LazyResolveContext context);

  /**
   * Base class of {@link ObjectResolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ObjectResolverImpl<T extends UniqueIdentifiable> implements ObjectResolver<T> {

    private final LazyResolver _parent;
    private final ObjectResolver<T> _underlying;

    public ObjectResolverImpl(final LazyResolver parent, final ObjectResolver<T> underlying) {
      _parent = parent;
      _underlying = underlying;
    }

    protected ObjectResolver<T> getUnderlying() {
      return _underlying;
    }

    protected abstract T lazy(T object, LazyResolveContext.AtVersionCorrection context);

    @Override
    public T resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
      final T underlying = _underlying.resolveObject(uniqueId, versionCorrection);
      if (underlying == null) {
        return null;
      }
      return lazy(underlying, _parent.getLazyResolveContext().atVersionCorrection(versionCorrection));
    }

    @Override
    public ChangeManager changeManager() {
      return getUnderlying().changeManager();
    }

  }

  /**
   * Base class of {@link Resolver} instances that are owned by a parent {@link LazyResolver}.
   */
  public abstract static class ResolverImpl<T extends UniqueIdentifiable> extends ObjectResolverImpl<T> implements Resolver<T> {

    public ResolverImpl(final LazyResolver parent, final Resolver<T> underlying) {
      super(parent, underlying);
    }

    protected Resolver<T> getUnderlying() {
      return (Resolver<T>) super.getUnderlying();
    }

    @Override
    public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveExternalId(identifiers, versionCorrection);
    }

    @Override
    public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Set<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveExternalIds(identifiers, versionCorrection);
    }

    @Override
    public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveObjectId(identifier, versionCorrection);
    }

    @Override
    public Map<ObjectId, UniqueId> resolveObjectIds(final Set<ObjectId> identifiers, final VersionCorrection versionCorrection) {
      return getUnderlying().resolveObjectIds(identifiers, versionCorrection);
    }

  }

  /**
   * Lazy resolution of portfolios.
   */
  public static class LazyPortfolioResolver extends ResolverImpl<Portfolio> {

    public LazyPortfolioResolver(final LazyResolver parent, final Resolver<Portfolio> underlying) {
      super(parent, underlying);
    }

    @Override
    public Portfolio lazy(final Portfolio object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPortfolio(context, object);
    }

  }

  /**
   * Lazy resolution of portfolio nodes.
   */
  public static class LazyPortfolioNodeResolver extends ObjectResolverImpl<PortfolioNode> {

    public LazyPortfolioNodeResolver(final LazyResolver parent, final ObjectResolver<PortfolioNode> underlying) {
      super(parent, underlying);
    }

    @Override
    public PortfolioNode lazy(final PortfolioNode object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPortfolioNode(context, object);
    }

  }

  /**
   * Lazy resolution of positions.
   */
  public static class LazyPositionResolver extends ResolverImpl<Position> {

    public LazyPositionResolver(final LazyResolver parent, final Resolver<Position> underlying) {
      super(parent, underlying);
    }

    // ResolverImpl

    @Override
    public Position lazy(final Position object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedPosition(context, object);
    }

  }

  /**
   * Lazy resolution of trades.
   */
  public static class LazyTradeResolver extends ObjectResolverImpl<Trade> {

    public LazyTradeResolver(final LazyResolver parent, final ObjectResolver<Trade> underlying) {
      super(parent, underlying);
    }

    @Override
    public Trade lazy(final Trade object, final LazyResolveContext.AtVersionCorrection context) {
      return new LazyResolvedTrade(context, object);
    }

  }

}
