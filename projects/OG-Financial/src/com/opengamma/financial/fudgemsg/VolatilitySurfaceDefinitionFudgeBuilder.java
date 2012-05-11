/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceDefinition.class)
public class VolatilitySurfaceDefinitionFudgeBuilder implements FudgeBuilder<VolatilitySurfaceDefinition<?, ?>> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceDefinitionFudgeBuilder.class);

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilitySurfaceDefinition<?, ?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    // the following forces it not to use a secondary type if one is available.
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    if (object.getTarget() instanceof Currency) {
      final Currency ccy = (Currency) object.getTarget();
      message.add("currency", null, ccy.getCode());
    } else {
      // just for now...
      message.add("currency", null, Currency.USD.getCode());
    }
    message.add("name", object.getName());
    for (final Object x : object.getXs()) {
      if (x instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, "xs", null, x);
      } else {
        message.add("xs", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    for (final Object y : object.getYs()) {
      if (y instanceof Number) {
        serializer.addToMessageWithClassHeaders(message, "ys", null, y);
      } else {
        message.add("ys", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(y), y.getClass()));
      }
    }
    return message;
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    UniqueIdentifiable target;
    if (!message.hasField("target")) {
      final String currencyCode = message.getString("currency");
      target = Currency.of(currencyCode);
    } else {
      target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    }
    final String name = message.getString("name");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    for (final FudgeField xField : xsFields) {
      final Object x = deserializer.fieldValueToObject(xField);
      xs.add(x);
    }
    final List<FudgeField> ysFields = message.getAllByName("ys");
    final List<Object> ys = new ArrayList<Object>();
    for (final FudgeField yField : ysFields) {
      final Object y = deserializer.fieldValueToObject(yField);
      ys.add(y);
    }
    return new VolatilitySurfaceDefinition<Object, Object>(name, target, xs.toArray(), ys.toArray());
  }

}
