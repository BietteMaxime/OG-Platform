/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.util.ArgumentChecker;

/**
 * A MarketDataShiftSpecification which specifies a yield curve to be shifted. Note that this
 * class is not responsible for specifying the actual manipulation to be done.
 */
public class YieldCurveShiftSpecification implements MarketDataShiftSpecification {

  /**
   * The key indicating the yield curve that needs to be shifted.
   */
  private final YieldCurveKey _yieldCurveKey;

  private YieldCurveShiftSpecification(YieldCurveKey yieldCurveKey) {
    ArgumentChecker.notNull(yieldCurveKey, "yieldCurveKey");
    _yieldCurveKey = yieldCurveKey;
  }

  /**
   * Construct a specification for the supplied yield curve key.
   *
   * @param yieldCurveKey the key of the yield curve to be shifted, not null
   * @return a new MarketDataShiftSpecification for the yield curve
   */
  public static MarketDataShiftSpecification of(YieldCurveKey yieldCurveKey) {
    return new YieldCurveShiftSpecification(yieldCurveKey);
  }

  @Override
  public boolean appliesTo(StructureIdentifier structureId,
                           String calculationConfigurationName) {
    return StructureIdentifier.of(_yieldCurveKey).equals(structureId);
  }

  @Override
  public StructureType getApplicableStructureType() {
    return StructureType.YIELD_CURVE;
  }

  @Override
  public StructuredMarketDataSnapshot apply(StructuredMarketDataSnapshot structuredSnapshot) {
    return structuredSnapshot;
  }

  @Override
  public boolean containsShifts() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    YieldCurveShiftSpecification that = (YieldCurveShiftSpecification) o;
    return _yieldCurveKey.equals(that._yieldCurveKey);
  }

  @Override
  public int hashCode() {
    return _yieldCurveKey.hashCode();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("yieldCurveKey", _yieldCurveKey);
    return msg;
  }

  public static MarketDataShiftSpecification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return of(msg.getValue(YieldCurveKey.class, "yieldCurveKey"));
  }
}
