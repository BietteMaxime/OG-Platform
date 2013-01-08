/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBCurveCalculationConfigSource implements CurveCalculationConfigSource {
  private final ConfigSource _configSource;

  public ConfigDBCurveCalculationConfigSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configuration source");
    _configSource = configSource;
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name) {
    return _configSource.getLatestByName(MultiCurveCalculationConfig.class, name);
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name, final VersionCorrection versionCorrection) {
    return _configSource.getSingle(MultiCurveCalculationConfig.class, name, versionCorrection);
  }

}
