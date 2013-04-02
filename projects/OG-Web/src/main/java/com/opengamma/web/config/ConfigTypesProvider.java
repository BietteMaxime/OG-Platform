/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.core.config.Config;
import com.opengamma.util.annotation.AnnotationScanner;
import com.opengamma.util.annotation.AnnotationScannerImpl;

/**
 * Provides all supported configuration types
 */
public final class ConfigTypesProvider {

  /**
   * Singleton instance.
   */
  private static final ConfigTypesProvider s_instance = new ConfigTypesProvider();
  
  private final Map<String, Class<?>> _configTypeMap;
  
  /**
   * Restricted constructor
   */
  private ConfigTypesProvider() {
    _configTypeMap = getConfigValue();
  }
  
  public static ConfigTypesProvider getInstance() {
    return s_instance;
  }
  
  private Map<String, Class<?>> getConfigValue() {
    Builder<String, Class<?>> result = ImmutableMap.builder();
    AnnotationScanner annotationScanner = new AnnotationScannerImpl();
    Set<Class<?>> configClasses = annotationScanner.scan(Config.class);
    for (Class<?> configClass : configClasses) {
      Annotation annotation = configClass.getAnnotation(Config.class);
      if (annotation instanceof Config) {
        Config configValueAnnotation = (Config) annotation;
        Class<?> configType = configValueAnnotation.searchType();
        if (configType == Object.class) {
          configType = configClass;
        }
        result.put(configType.getSimpleName(), configType);
      }
    }
    return result.build();
  }
  
  public Set<String> getConfigTypes() {
    return ImmutableSortedSet.copyOf(_configTypeMap.keySet());
  }
  
  public Map<String, Class<?>> getConfigTypeMap() {
    return _configTypeMap;
  }
  
}
