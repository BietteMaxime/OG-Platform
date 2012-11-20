/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeObjectTimeSeries;

@Test
public class ArrayZonedDateTimeObjectTimeSeriesTest extends ZonedDateTimeObjectTimeSeriesTest {
  @Override
  public ZonedDateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ArrayZonedDateTimeObjectTimeSeries<BigDecimal>(TimeZone.UTC);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(ZonedDateTime[] times, BigDecimal[] values) {
    return new ArrayZonedDateTimeObjectTimeSeries<BigDecimal>(TimeZone.UTC, times, values);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<ZonedDateTime> times, List<BigDecimal> values) {
    return new ArrayZonedDateTimeObjectTimeSeries<BigDecimal>(TimeZone.UTC, times, values);
  }

  @Override
  public ObjectTimeSeries<ZonedDateTime, BigDecimal> createTimeSeries(ObjectTimeSeries<ZonedDateTime, BigDecimal> dts) {
    return new ArrayZonedDateTimeObjectTimeSeries<BigDecimal>(TimeZone.UTC, dts);
  }
}
