/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Definition for the 30E/360 day count convention. The day count fraction is
 * defined as:<br>
 * <i>fraction = 360(Y<sub>2</sub> - Y<sub>1</sub> + 30(M<sub>2</sub> -
 * M<sub>1</sub> + (D<sub>2</sub> - D<sub>1</sub> / 360</i><br>
 * where:<br>
 * <i>Y<sub>1</sub></i> is the year in which the first day of the period falls;<br>
 * <i>Y<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>M<sub>1</sub></i> is the month in which the first day of the period falls;<br>
 * <i>M<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>D<sub>1</sub></i> is the day in which the first day of the period falls
 * unless the day number is 31, in which case it is adjusted to 30; and<br>
 * <i>D<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls unless the day number is 31, in which case it is
 * adjusted to 30.<br>
 * <p>
 * This convention is also known as "EuroBond Basis".
 * 
 * @author emcleod
 */
public class ThirtyEThreeSixtyDayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    // TODO Auto-generated method stub
    return 0;
  }

}
