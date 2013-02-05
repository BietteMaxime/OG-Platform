/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.OtcTradeTarget;

/**
 * Formats {@link OtcTradeTarget}s for sending to the client as JSON.
 */
/* package */ class OtcTradeTargetFormatter extends AbstractFormatter<OtcTradeTarget> {

  /** Key for the name in the JSON. */
  private static final String NAME = "name";
  /** Key for the ID in the JSON. */
  private static final String ID = "id";

  /* package */ OtcTradeTargetFormatter() {
    super(OtcTradeTarget.class);
  }

  @Override
  public Object formatCell(OtcTradeTarget trade, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, trade.getName(),
                           ID, trade.getId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.OTC_TRADE;
  }
}
