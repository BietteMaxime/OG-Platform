/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.PositionTarget;

/* package */ class PositionTargetFormatter extends AbstractFormatter<PositionTarget> {

  private static final String NAME = "name";
  private static final String ID = "id";

  /* package */  PositionTargetFormatter() {
    super(PositionTarget.class);
  }

  @Override
  public Object formatCell(PositionTarget value, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, value.getName(), ID, value.getId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.POSITION_ID;
  }
}
