/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Loads configuration from the INI format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  </code>${key} = value</code> declares a replacement for use later in the file<br>
 *  </code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  </code>[group]</code> defines the start of a named group of configs<br>
 *  </code>key = value</code> defines a single config element within a group<br>
 *  Everything is trimmed as necessary.
 */
public class ComponentConfigLoader {

  /**
   * Starts the components defined in the specified resource.
   * <p>
   * The specified properties are simple key=value pairs and must not be surrounded with ${}.
   * 
   * @param resource  the config resource to load, not null
   * @param properties  the default set of replacements, not null
   * @return the config, not null
   */
  public ComponentConfig load(Resource resource, ConcurrentMap<String, String> properties) {
    Map<String, String> iniProperties = extractIniProperties(properties);
    properties = adjustProperties(properties);
    
    List<String> lines = readLines(resource);
    ComponentConfig config = new ComponentConfig();
    String group = "global";
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      if (line.startsWith("${") && line.substring(2).contains("=") &&
          StringUtils.substringBefore(line, "=").trim().endsWith("}")) {
        parseReplacement(line, properties);
        
      } else {
        line = applyReplacements(line, properties);
        
        if (line.startsWith("[") && line.endsWith("]")) {
          group = line.substring(1, line.length() - 1);
        
        } else {
          String key = StringUtils.substringBefore(line, "=").trim();
          String value = StringUtils.substringAfter(line, "=").trim();
          if (key.length() == 0) {
            throw new IllegalArgumentException("Invalid key, line " + lineNum);
          }
          config.add(group, key, value);
        }
      }
    }
    
    // override config with properties prefixed by INI
    for (Entry<String, String> entry : iniProperties.entrySet()) {
      String iniGroup = StringUtils.substringBefore(entry.getKey(), ".");
      String iniKey = StringUtils.substringAfter(entry.getKey(), ".");
      config.getGroup(iniGroup).put(iniKey, entry.getValue());  // throws exception if iniGroup not found
    }
    return config;
  }

  /**
   * Extracts any properties that start with "INI.".
   * <p>
   * These directly override any INI file settings.
   * 
   * @param properties  the properties, not null
   * @return the extracted set of INI properties, not null
   */
  private Map<String, String> extractIniProperties(ConcurrentMap<String, String> properties) {
    Map<String, String> extracted = new HashMap<String, String>();
    for (String key : properties.keySet()) {
      if (key.startsWith("INI.") && key.substring(4).contains(".")) {
        extracted.put(key.substring(4), properties.get(key));
      }
    }
    return extracted;
  }

  //-------------------------------------------------------------------------
  private ConcurrentMap<String, String> adjustProperties(ConcurrentMap<String, String> input) {
    ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();
    for (String key : input.keySet()) {
      map.put("${" + key + "}", input.get(key));
    }
    for (String key : new HashSet<String>(map.keySet())) {
      String value = map.get(key);
      boolean hasChanged;
      do {
        hasChanged = false;
        for (Map.Entry<String, String> replacementEntry : map.entrySet()) {
          if (value.contains(replacementEntry.getKey())) {
            if (replacementEntry.getKey().equals(key)) {
              throw new OpenGammaRuntimeException("Property " + key + " references itself");
            }
            hasChanged = true;
            value = value.replace(replacementEntry.getKey(), replacementEntry.getValue());
          }
        }
      } while (hasChanged);
      map.put(key, value);
    }
    return map;
  }

  private void parseReplacement(String line, ConcurrentMap<String, String> properties) {
    String key = StringUtils.substringBefore(line, "=").trim();
    String value = StringUtils.substringAfter(line, "=").trim();
    // do not overwrite properties that were passed in
    properties.putIfAbsent(key, value);
  }

  private String applyReplacements(String line, ConcurrentMap<String, String> properties) {
    for (Entry<String, String> entry : properties.entrySet()) {
      line = StringUtils.replace(line, entry.getKey(), entry.getValue());
    }
    return line;
  }

  /**
   * Reads lines from the resource.
   * 
   * @param resource  the resource to read, not null
   * @return the lines, not null
   */
  private List<String> readLines(Resource resource) {
    try {
      return IOUtils.readLines(resource.getInputStream(), "UTF8");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read resource: " + resource, ex);
    }
  }

}
