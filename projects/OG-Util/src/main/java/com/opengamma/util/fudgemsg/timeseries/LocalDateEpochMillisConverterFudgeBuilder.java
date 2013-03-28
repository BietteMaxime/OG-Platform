/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.localdate.LocalDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for LocalDateEpochMillisConverter.
 */
@FudgeBuilderFor(LocalDateEpochDaysConverter.class)
public class LocalDateEpochMillisConverterFudgeBuilder extends DateTimeConverterFudgeBuilder<LocalDateEpochDaysConverter> {
  @Override
  public LocalDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new LocalDateEpochDaysConverter(timeZone);
  }
}
