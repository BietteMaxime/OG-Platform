/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.FinancialFunctions;
import com.opengamma.financial.analytics.ircurve.IRCurveFunctions;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.function.rest.DataRepositoryConfigurationSourceResource;
import com.opengamma.financial.function.rest.RemoteRepositoryConfigurationSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.web.spring.BloombergVolatilityCubeFunctions;
import com.opengamma.web.spring.DemoStandardFunctionConfiguration;

/**
 * Component factory for the repository configuration source.
 */
@BeanDefinition
public class RepositoryConfigurationSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The config master.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigMaster _configMaster;
  /**
   * The convention bundle source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionBundleSource _conventionBundleSource;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final RepositoryConfigurationSource source = initSource();
    //final RepositoryConfigurationSource source = sorted(initSource());

    final ComponentInfo info = new ComponentInfo(RepositoryConfigurationSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteRepositoryConfigurationSource.class);
    repo.registerComponent(info, source);

    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataRepositoryConfigurationSourceResource(source));
    }
  }

  /**
   * Debug utility to sort a repository. This allows two to be compared more easily.
   *
   * @param source the raw repository configuration source
   * @return a source that return a sorted list of functions
   */
  protected RepositoryConfigurationSource sorted(final RepositoryConfigurationSource source) {
    return new RepositoryConfigurationSource() {

      @Override
      public RepositoryConfiguration getRepositoryConfiguration() {
        final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>(source.getRepositoryConfiguration().getFunctions());
        Collections.sort(functions, new Comparator<FunctionConfiguration>() {

          @Override
          public int compare(final FunctionConfiguration o1, final FunctionConfiguration o2) {
            if (o1 instanceof ParameterizedFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                final ParameterizedFunctionConfiguration p1 = (ParameterizedFunctionConfiguration) o1;
                final ParameterizedFunctionConfiguration p2 = (ParameterizedFunctionConfiguration) o2;
                // Order by class name
                int c = p1.getDefinitionClassName().compareTo(p2.getDefinitionClassName());
                if (c != 0) {
                  return c;
                }
                // Order by parameter lengths
                c = p1.getParameter().size() - p2.getParameter().size();
                if (c != 0) {
                  return c;
                }
                // Order by parameters
                for (int i = 0; i < p1.getParameter().size(); i++) {
                  c = p1.getParameter().get(i).compareTo(p2.getParameter().get(i));
                  if (c != 0) {
                    return c;
                  }
                }
                // Equal? Put a breakpoint here; we don't really want this to be happening.
                //assert false;
                return 0;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Static goes first
                return 1;
              }
            } else if (o1 instanceof StaticFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                // Static goes first
                return -1;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Sort by class name
                return ((StaticFunctionConfiguration) o1).getDefinitionClassName().compareTo(((StaticFunctionConfiguration) o2).getDefinitionClassName());
              }
            }
            throw new UnsupportedOperationException("Can't compare " + o1.getClass() + " and " + o2.getClass());
          }

        });
        return new RepositoryConfiguration(functions);
      }

    };
  }

  /**
   * Initializes the source.
   * <p>
   * Calls {@link #initSources()} and combines the result using {@link CombiningRepositoryConfigurationSource}.
   *
   * @return the list of base sources to be combined, not null
   */
  protected RepositoryConfigurationSource initSource() {
    final List<RepositoryConfigurationSource> underlying = initSources();
    final RepositoryConfigurationSource[] array = underlying.toArray(new RepositoryConfigurationSource[underlying.size()]);
    return CombiningRepositoryConfigurationSource.of(array);
  }

  protected RepositoryConfigurationSource financialFunctions() {
    return FinancialFunctions.instance();
  }

  protected RepositoryConfigurationSource standardConfiguration() {
    return DemoStandardFunctionConfiguration.instance();
  }

  protected RepositoryConfigurationSource curveConfigurations() {
    return IRCurveFunctions.providers(getConfigMaster());
  }

  protected RepositoryConfigurationSource cubeConfigurations() {
    return BloombergVolatilityCubeFunctions.instance();
  }

  /**
   * Initializes the list of sources to be combined.
   *
   * @return the list of base sources to be combined, not null
   */
  protected List<RepositoryConfigurationSource> initSources() {
    final List<RepositoryConfigurationSource> sources = new LinkedList<RepositoryConfigurationSource>();
    sources.add(financialFunctions());
    sources.add(standardConfiguration());
    sources.add(curveConfigurations());
    sources.add(cubeConfigurations());
    return sources;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RepositoryConfigurationSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static RepositoryConfigurationSourceComponentFactory.Meta meta() {
    return RepositoryConfigurationSourceComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(RepositoryConfigurationSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public RepositoryConfigurationSourceComponentFactory.Meta metaBean() {
    return RepositoryConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -614707837:  // publishRest
        return isPublishRest();
      case 10395716:  // configMaster
        return getConfigMaster();
      case -1281578674:  // conventionBundleSource
        return getConventionBundleSource();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case 10395716:  // configMaster
        setConfigMaster((ConfigMaster) newValue);
        return;
      case -1281578674:  // conventionBundleSource
        setConventionBundleSource((ConventionBundleSource) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_configMaster, "configMaster");
    JodaBeanUtils.notNull(_conventionBundleSource, "conventionBundleSource");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RepositoryConfigurationSourceComponentFactory other = (RepositoryConfigurationSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getConventionBundleSource(), other.getConventionBundleSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionBundleSource());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the new value of the property, not null
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    JodaBeanUtils.notNull(configMaster, "configMaster");
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention bundle source.
   * @return the value of the property, not null
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  /**
   * Sets the convention bundle source.
   * @param conventionBundleSource  the new value of the property, not null
   */
  public void setConventionBundleSource(ConventionBundleSource conventionBundleSource) {
    JodaBeanUtils.notNull(conventionBundleSource, "conventionBundleSource");
    this._conventionBundleSource = conventionBundleSource;
  }

  /**
   * Gets the the {@code conventionBundleSource} property.
   * @return the property, not null
   */
  public final Property<ConventionBundleSource> conventionBundleSource() {
    return metaBean().conventionBundleSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RepositoryConfigurationSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", RepositoryConfigurationSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", RepositoryConfigurationSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", RepositoryConfigurationSourceComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code conventionBundleSource} property.
     */
    private final MetaProperty<ConventionBundleSource> _conventionBundleSource = DirectMetaProperty.ofReadWrite(
        this, "conventionBundleSource", RepositoryConfigurationSourceComponentFactory.class, ConventionBundleSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "configMaster",
        "conventionBundleSource");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case 10395716:  // configMaster
          return _configMaster;
        case -1281578674:  // conventionBundleSource
          return _conventionBundleSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RepositoryConfigurationSourceComponentFactory> builder() {
      return new DirectBeanBuilder<RepositoryConfigurationSourceComponentFactory>(new RepositoryConfigurationSourceComponentFactory());
    }

    @Override
    public Class<? extends RepositoryConfigurationSourceComponentFactory> beanType() {
      return RepositoryConfigurationSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code conventionBundleSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionBundleSource> conventionBundleSource() {
      return _conventionBundleSource;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
