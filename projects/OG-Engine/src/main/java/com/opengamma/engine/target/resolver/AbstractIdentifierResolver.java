/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Partial implementation of {@link IdentifierResolver}.
 */
public abstract class AbstractIdentifierResolver implements IdentifierResolver {

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIds(final IdentifierResolver resolver, final Set<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ExternalIdBundle identifier : identifiers) {
      final UniqueId uid = resolver.resolveExternalId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

  public static Map<ObjectId, UniqueId> resolveObjectIds(final IdentifierResolver resolver, final Set<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ObjectId identifier : identifiers) {
      final UniqueId uid = resolver.resolveObjectId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

  // IdentifierResolver

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(Set<ExternalIdBundle> identifiers, VersionCorrection versionCorrection) {
    return resolveExternalIds(this, identifiers, versionCorrection);
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(Set<ObjectId> identifiers, VersionCorrection versionCorrection) {
    return resolveObjectIds(this, identifiers, versionCorrection);
  }

}
