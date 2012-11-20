/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A general purpose security loader for populating a master.
 * <p>
 * SecurityLoader adds or updates the details about a security in the attached master.
 * This will normally be achieved by calling a standard external data source.
 */
@PublicSPI
public interface SecurityLoader {

  /**
   * Loads the security data for the requested bundles.
   * 
   * @param identifiers  a collection of identifiers to load, not null
   * @return a map of input bundle to created unique identifier from the master, not null
   */
  Map<ExternalIdBundle, UniqueId> loadSecurity(Collection<ExternalIdBundle> identifiers);

  /**
   * Gets the associated master.
   * 
   * @return the master that is being populated, not null
   */
  SecurityMaster getSecurityMaster();

}
