/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * 
 */
public class CalculationJobSpecificationTest {
  
  @Test
  public void testHashCode() {
    CalculationJobSpecification spec1 = new CalculationJobSpecification("view", "config", 1L, 1L);
    CalculationJobSpecification spec2 = new CalculationJobSpecification("view", "config", 1L, 1L);
    
    assertEquals(spec1.hashCode(), spec2.hashCode());
    
    spec2 = new CalculationJobSpecification("view2", "config", 1L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config2", 1L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config", 2L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config", 1L, 2L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
  }

  @Test
  public void testEquals() {
    CalculationJobSpecification spec1 = new CalculationJobSpecification("view", "config", 1L, 1L);
    assertTrue(spec1.equals(spec1));
    assertFalse(spec1.equals(null));
    assertFalse(spec1.equals("Kirk"));
    CalculationJobSpecification spec2 = new CalculationJobSpecification("view", "config", 1L, 1L);
    assertTrue(spec1.equals(spec2));
    
    spec2 = new CalculationJobSpecification("view2", "config", 1L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config2", 1L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config", 2L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config", 1L, 2L);
    assertFalse(spec1.equals(spec2));
  }
  
  @Test
  public void fudgeEncoding() {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    CalculationJobSpecification spec1 = new CalculationJobSpecification("view", "config", 1L, 1L);
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(context);
    MutableFudgeFieldContainer inMsg = serializationContext.objectToFudgeMsg(spec1);
    FudgeFieldContainer outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(context);
    CalculationJobSpecification spec2 = deserializationContext.fudgeMsgToObject(CalculationJobSpecification.class, outMsg);
    assertEquals(spec1, spec2);
  }

}
