/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * Provides mutator methods for live data, allowing customisation of live data.
 */
@PublicSPI
public interface MarketDataInjector {
  
  /**
   * Injects a live data value by {@link ValueRequirement}.
   * 
   * @param valueRequirement  the value requirement, not null
   * @param value  the value to add
   */
  void addValue(ValueRequirement valueRequirement, Object value);

  /**
   * Injects a live data value by {@link ExternalId}. This identifier is resolved automatically into the
   * {@link UniqueId} to use in a {@link ValueRequirement}.
   * 
   * @param identifier  an identifier of the target, not null
   * @param valueName  the name of the value being added, not null
   * @param value  the value to add
   */
  void addValue(ExternalId identifier, String valueName, Object value);
  
  /**
   * Removes a previously-added live data value by {@link ValueRequirement}.
   * 
   * @param valueRequirement  the value requirement, not null
   */
  void removeValue(ValueRequirement valueRequirement);
  
  /**
   * Removes a previously-added live data value by {@link ExternalId}. This identifier is resolved automatically into
   * a {@link ValueRequirement} so could be different from the one used when the value was added, as long as it
   * resolves to the same target.
   * 
   * @param identifier  an identifier of the target, not null
   * @param valueName  the name of the value being removed, not null
   */
  void removeValue(ExternalId identifier, String valueName);
  
}
