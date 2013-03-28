/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.timeseries.date.time.MapDateTimeDoubleTimeSeries;

@Test(groups = "unit")
public class MapDateTimeDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  protected DateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return new MapDateTimeDoubleTimeSeries();
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new MapDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new MapDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new MapDateTimeDoubleTimeSeries(dts);
  }

}
