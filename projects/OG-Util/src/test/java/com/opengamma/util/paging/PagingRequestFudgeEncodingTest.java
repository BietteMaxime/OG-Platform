/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test(groups = "unit")
public class PagingRequestFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    PagingRequest object = PagingRequest.ofIndex(0, 20);
    assertEncodeDecodeCycle(PagingRequest.class, object);
  }

}
