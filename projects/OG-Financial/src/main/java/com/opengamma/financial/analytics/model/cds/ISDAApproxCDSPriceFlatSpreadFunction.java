/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDAApproxCDSPricingMethod;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.ISDACDSSecurityConverter;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 * Price CDS contracts according to the ISDA model using a flat spread
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPricingMethod
 */
public class ISDAApproxCDSPriceFlatSpreadFunction extends ISDAApproxCDSPriceFunction {

  private static final ISDAApproxCDSPricingMethod ISDA_APPROX_METHOD = new ISDAApproxCDSPricingMethod();
  
  @Override
  protected String getHazardRateStructure() {
    return ISDAFunctionConstants.ISDA_HAZARD_RATE_FLAT;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();

    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();

    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(cds.getCurrency()), ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD,
        ISDAFunctionConstants.ISDA_METHOD_NAME).get()));
    
    // TODO: Are extra value properties needed here? (see ISDAApproxFlatSpreadFunction)
    requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE, target.toSpecification(), ValueProperties.none()));
    
    return requirements;
  }
  
  @Override
  public DoublesPair executeImpl(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    
    // Set up converter (could this be compiled?)
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ISDACDSSecurityConverter converter = new ISDACDSSecurityConverter(holidaySource);
    
    // Security being priced
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final ISDACDSDefinition cdsDefinition = (ISDACDSDefinition) cds.accept(converter);
    
    // Time point to price for
    // TODO: Supply an option for the user to specify non-standard step-in and settlement dates
    final ZonedDateTime pricingDate = ZonedDateTime.now(executionContext.getValuationClock());
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = findSettlementDate(pricingDate, cdsDefinition.getConvention());

    // Discount curve
    final ISDACurve discountCurve = (ISDACurve) inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    final double flatSpread = (Double) inputs.getValue(ValueRequirementNames.SPOT_RATE);
    
    // Convert security in to format suitable for pricing
    final ISDACDSDerivative cdsDerivative = cdsDefinition.toDerivative(pricingDate, stepinDate, settlementDate, discountCurve.getName());
    
    // Go price!
    double dirtyPrice = ISDA_APPROX_METHOD.calculateUpfrontCharge(cdsDerivative, discountCurve, flatSpread, false, pricingDate, stepinDate, settlementDate);
    final double cleanPrice = dirtyPrice - cdsDerivative.getAccruedInterest();
    
    return DoublesPair.of(cleanPrice, dirtyPrice);
  }
  
}
