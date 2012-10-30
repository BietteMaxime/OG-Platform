/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateObjectTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateObjectTimeSeries;

@Test
public class ArraySQLDateObjectTimeSeriesTest extends SQLDateObjectTimeSeriesTest {

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ArraySQLDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ArraySQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ArraySQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ArraySQLDateObjectTimeSeries<BigDecimal>(dts);
  }
}
