/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapBucketedIR01Calculator;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;

/**
 * 
 */
public class StandardVanillaBucketedIR01CDSFunction extends StandardVanillaIR01CDSFunction {
  private static final ISDACreditDefaultSwapBucketedIR01Calculator CALCULATOR = new ISDACreditDefaultSwapBucketedIR01Calculator();

  public StandardVanillaBucketedIR01CDSFunction() {
    super(ValueRequirementNames.BUCKETED_IR01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition, final ISDADateCurve yieldCurve, final ZonedDateTime[] times,
      final double[] marketSpreads, final ZonedDateTime valuationDate, final ComputationTarget target, final ValueProperties properties,
      final FunctionInputs inputs) {
    final Double interestRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)));
    final InterestRateBumpType interestRateBumpType =
        InterestRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE)));
    final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    final double[] ir01 = CALCULATOR.getIR01BucketedCreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, interestRateCurveBump,
        interestRateBumpType, priceType);
    final int n = yieldCurve.getNumberOfCurvePoints();
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = yieldCurve.getCurveDates()[i].toLocalDate();
    }
    final LocalDateLabelledMatrix1D ir01Matrix = new LocalDateLabelledMatrix1D(dates, ir01);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.BUCKETED_IR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ir01Matrix));
  }

}
