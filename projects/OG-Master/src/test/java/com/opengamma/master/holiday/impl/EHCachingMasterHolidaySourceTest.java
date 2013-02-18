/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;

import net.sf.ehcache.CacheManager;

/**
 * Test.
 */
@Test
public class EHCachingMasterHolidaySourceTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final LocalDate DATE_TUESDAY = LocalDate.of(2010, 10, 26);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.GBP;
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);

  private HolidayMaster _underlyingHolidayMaster = null;
  private EHCachingMasterHolidaySource _cachingHolidaySource = null;
  private CacheManager _cacheManager = EHCacheUtils.createCacheManager();

  @BeforeMethod
  public void setUp() {
//    _cacheManager = EHCacheUtils.createCacheManager();
    _underlyingHolidayMaster = mock(HolidayMaster.class);
    _cachingHolidaySource = new EHCachingMasterHolidaySource(_underlyingHolidayMaster, _cacheManager);
  }

  @AfterMethod
  public void tearDown() {
//    _cacheManager = EHCacheUtils.shutdownQuiet(_cacheManager);
    EHCacheUtils.clear(_cacheManager, EHCachingMasterHolidaySource.HOLIDAY_CACHE);
  }

  //-------------------------------------------------------------------------
  public void isHoliday_dateAndCurrency() {
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, GBP));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, GBP));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, GBP)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

  public void isHoliday_dateTypeAndBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, BUNDLE);
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, HolidayType.BANK, BUNDLE));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, HolidayType.BANK, BUNDLE)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

  public void isHoliday_dateTypeAndExternalId() {
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, ExternalIdBundle.of(ID));
    
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    
    when(_underlyingHolidayMaster.search(request)).thenReturn(result);
    
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_MONDAY, HolidayType.BANK, ID));
    assertFalse(_cachingHolidaySource.isHoliday(DATE_TUESDAY, HolidayType.BANK, ID));
    assertTrue(_cachingHolidaySource.isHoliday(DATE_SUNDAY, HolidayType.BANK, ID)); // weekend
    
    verify(_underlyingHolidayMaster, times(1)).search(request);
  }

}
