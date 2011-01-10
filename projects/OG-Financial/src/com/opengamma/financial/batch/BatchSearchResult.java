/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * 
 */
@BeanDefinition
public class BatchSearchResult extends DirectBean {
  
  /**
   * The list of matched time-series documents.
   */
  @PropertyDefinition
  private final List<BatchSearchResultItem> _items = new ArrayList<BatchSearchResultItem>();

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BatchSearchResult}.
   * @return the meta-bean, not null
   */
  public static BatchSearchResult.Meta meta() {
    return BatchSearchResult.Meta.INSTANCE;
  }

  @Override
  public BatchSearchResult.Meta metaBean() {
    return BatchSearchResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 100526016:  // items
        return getItems();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 100526016:  // items
        setItems((List<BatchSearchResultItem>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of matched time-series documents.
   * @return the value of the property
   */
  public List<BatchSearchResultItem> getItems() {
    return _items;
  }

  /**
   * Sets the list of matched time-series documents.
   * @param items  the new value of the property
   */
  public void setItems(List<BatchSearchResultItem> items) {
    this._items.clear();
    this._items.addAll(items);
  }

  /**
   * Gets the the {@code items} property.
   * @return the property, not null
   */
  public final Property<List<BatchSearchResultItem>> items() {
    return metaBean().items().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BatchSearchResult}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code items} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<BatchSearchResultItem>> _items = DirectMetaProperty.ofReadWrite(this, "items", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("items", _items);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public BatchSearchResult createBean() {
      return new BatchSearchResult();
    }

    @Override
    public Class<? extends BatchSearchResult> beanType() {
      return BatchSearchResult.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code items} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<BatchSearchResultItem>> items() {
      return _items;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
