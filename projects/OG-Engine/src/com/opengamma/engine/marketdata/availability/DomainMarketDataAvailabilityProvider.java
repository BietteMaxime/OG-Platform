/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} by providing an indication of whether an item of market data is
 * <i>likely</i> to be available based on the scheme of its unique identifier and other factors.
 */
public class DomainMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(DomainMarketDataAvailabilityProvider.class);
  /**
   * The security source to resolve against.
   */
  private final SecuritySource _securitySource;
  /**
   * The set of acceptable schemes.
   */
  private final Set<ExternalScheme> _acceptableSchemes;
  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;

  /**
   * Creates a provider.
   * 
   * @param securitySource  the security source, not null
   * @param acceptableSchemes  the acceptable schemes, not null
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public DomainMarketDataAvailabilityProvider(final SecuritySource securitySource, final Collection<ExternalScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    ArgumentChecker.notNull(validMarketDataRequirementNames, "validMarketDataRequirementNames");
    _securitySource = securitySource;
    _acceptableSchemes = new HashSet<ExternalScheme>(acceptableSchemes);
    _validMarketDataRequirementNames = new HashSet<String>(validMarketDataRequirementNames);
  }

  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    if (!_validMarketDataRequirementNames.contains(requirement.getValueName())) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    switch (requirement.getTargetSpecification().getType()) {
      case PRIMITIVE: {
        if (requirement.getTargetSpecification().getIdentifier() == null) {
          return MarketDataAvailability.NOT_AVAILABLE;
        }
        final ExternalScheme scheme = requirement.getTargetSpecification().getIdentifier().getScheme();
        return _acceptableSchemes.contains(scheme) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
      }
      case SECURITY: {
        try {
          final Security security = _securitySource.getSecurity(requirement.getTargetSpecification().getUniqueId());
          for (final ExternalId identifier : security.getExternalIdBundle()) {
            if (_acceptableSchemes.contains(identifier.getScheme())) {
              return MarketDataAvailability.AVAILABLE;
            }
          }
          return MarketDataAvailability.NOT_AVAILABLE;
        } catch (final DataNotFoundException ex) {
          return MarketDataAvailability.NOT_AVAILABLE;
        }
      }
      default:
        return MarketDataAvailability.NOT_AVAILABLE;
    }
  }

}
