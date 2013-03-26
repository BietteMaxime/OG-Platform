/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.date.time.DateEpochMillisConverter;

/**
 * Fudge message builder (serializer/deserializer) for DateEpochMillisConverter
 */
@FudgeBuilderFor(DateEpochMillisConverter.class)
public class DateEpochMillisConverterFudgeBuilder extends DateTimeConverterFudgeBuilder<DateEpochMillisConverter> {
  @Override
  public DateEpochMillisConverter makeConverter(TimeZone timeZone) {
    return new DateEpochMillisConverter(timeZone);
  }
}
