/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Builder for converting VolatilitySurfaceData instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceData.class)
public class VolatilitySurfaceDataFudgeBuilder implements FudgeBuilder<VolatilitySurfaceData<?, ?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilitySurfaceData<?, ?> object) {
    final MutableFudgeMsg message = serializer.newMessage();
    // the following forces it not to use a secondary type if one is available.
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    // for compatibility with old code, remove.
    if (object.getTarget() instanceof Currency) {
      message.add("currency", object.getTarget());
    } else {
      // just for now...
      message.add("currency", Currency.USD);
    }

    serializer.addToMessage(message, "target", null, object.getTarget());
    message.add("definitionName", object.getDefinitionName());
    message.add("specificationName", object.getSpecificationName());
    for (final Object x : object.getXs()) {
      if (x != null) {
        message.add("xs", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(x), x.getClass()));
      }
    }
    for (final Object y : object.getYs()) {
      if (y != null) {
        message.add("ys", null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(y), y.getClass()));
      }
    }
    for (final Entry<?, Double> entry : object.asMap().entrySet()) {
      final Pair<Object, Object> pair = (Pair<Object, Object>) entry.getKey();
      final MutableFudgeMsg subMessage = serializer.newMessage();
      if (pair.getFirst() != null && pair.getSecond() != null) {
        subMessage.add("x", null, serializer.objectToFudgeMsg(pair.getFirst()));
        subMessage.add("y", null, serializer.objectToFudgeMsg(pair.getSecond()));
        subMessage.add("value", null, entry.getValue());
        message.add("values", null, subMessage);
      }
    }
    return message;
  }

  @Override
  public VolatilitySurfaceData<?, ?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    UniqueIdentifiable target;
    if (!message.hasField("target")) {
      target = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    } else {
      target = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName("target"));
    }
    final String definitionName = message.getString("definitionName");
    final String specificationName = message.getString("specificationName");
    final List<FudgeField> xsFields = message.getAllByName("xs");
    final List<Object> xs = new ArrayList<Object>();
    Object[] xsArray = null;
    for (final FudgeField xField : xsFields) {
      final Object x = deserializer.fieldValueToObject(xField);
      xs.add(x);
      if (xsArray == null) {
        xsArray = (Object[]) Array.newInstance(x.getClass(), 0);
      }
    }
    final List<FudgeField> ysFields = message.getAllByName("ys");
    final List<Object> ys = new ArrayList<Object>();
    Object[] ysArray = null;
    for (final FudgeField yField : ysFields) {
      final Object y = deserializer.fieldValueToObject(yField);
      ys.add(y);
      if (ysArray == null) {
        ysArray = (Object[]) Array.newInstance(y.getClass(), 0);
      }
    }
    if (xs.size() > 0 && ys.size() > 0) {
      final Class<?> xClazz = xs.get(0).getClass();
      final Class<?> yClazz = ys.get(0).getClass();
      final Map<Pair<Object, Object>, Double> values = new HashMap<Pair<Object, Object>, Double>();
      final List<FudgeField> valuesFields = message.getAllByName("values");
      for (final FudgeField valueField : valuesFields) {
        final FudgeMsg subMessage = (FudgeMsg) valueField.getValue();
        final Object x = deserializer.fieldValueToObject(xClazz, subMessage.getByName("x"));
        final Object y = deserializer.fieldValueToObject(yClazz, subMessage.getByName("y"));
        final Double value = subMessage.getDouble("value");
        values.put(Pair.of(x, y), value);
      }
      return new VolatilitySurfaceData<Object, Object>(definitionName, specificationName, target, xs.toArray(xsArray), ys.toArray(ysArray), values);
    }
    return new VolatilitySurfaceData<Object, Object>(definitionName, specificationName, target, xs.toArray(), ys.toArray(), Collections.<Pair<Object, Object>, Double>emptyMap());
  }

}
