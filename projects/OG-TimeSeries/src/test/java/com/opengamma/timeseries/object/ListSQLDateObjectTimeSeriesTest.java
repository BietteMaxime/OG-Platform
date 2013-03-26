/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.object;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.sqldate.ListSQLDateObjectTimeSeries;
import com.opengamma.timeseries.sqldate.SQLDateObjectTimeSeries;

/**
 * Test.
 */
@Test(groups = "unit")
public class ListSQLDateObjectTimeSeriesTest extends SQLDateObjectTimeSeriesTest {

  @Override
  protected SQLDateObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListSQLDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  protected SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected SQLDateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  protected ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ListSQLDateObjectTimeSeries<BigDecimal>(dts);
  }

}
