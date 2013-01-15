/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Specialized form of {@link ObjectComputationTargetType} for primitive objects that can be converted directly to/from unique identifiers without an external resolver service. Instances also serve as
 * a {@link ObjectResolver} so that they can be added to a {@link ComputationTargetResolver} to handle the type.
 * 
 * @param <T> the target object type
 */
public final class PrimitiveComputationTargetType<T extends UniqueIdentifiable> extends ObjectComputationTargetType<T> implements ObjectResolver<T> {

  private static final long serialVersionUID = 1L;

  private final ObjectResolver<T> _resolver;

  private PrimitiveComputationTargetType(final ComputationTargetType type, final Class<T> clazz, final ObjectResolver<T> resolver) {
    super(type, clazz);
    _resolver = resolver;
  }

  public static <T extends UniqueIdentifiable> PrimitiveComputationTargetType<T> of(final ComputationTargetType type, final Class<T> clazz, final ObjectResolver<T> resolver) {
    assert type.isTargetType(clazz);
    assert resolver != null;
    return new PrimitiveComputationTargetType<T>(type, clazz, resolver);
  }

  public T resolve(final UniqueId identifier) {
    return _resolver.resolveObject(identifier, VersionCorrection.LATEST);
  }

  @Override
  public T resolveObject(final UniqueId identifier, final VersionCorrection versionCorrection) {
    return _resolver.resolveObject(identifier, versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return _resolver.changeManager();
  }

}
