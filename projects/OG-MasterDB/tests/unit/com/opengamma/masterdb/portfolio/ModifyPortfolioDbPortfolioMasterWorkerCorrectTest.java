/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
public class ModifyPortfolioDbPortfolioMasterWorkerCorrectTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPortfolioDbPortfolioMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_nullDocument() {
    _prtMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noPortfolioId() {
    ManageablePortfolio position = new ManageablePortfolio("Test");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setObject(position);
    _prtMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noPortfolio() {
    PortfolioDocument doc = new PortfolioDocument();
    doc.setUniqueId(UniqueId.of("DbPrt", "201", "0"));
    _prtMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    ManageablePortfolio port = new ManageablePortfolio("Test");
    port.setUniqueId(UniqueId.of("DbPrt", "0", "0"));
    port.setRootNode(new ManageablePortfolioNode("Root"));
    PortfolioDocument doc = new PortfolioDocument(port);
    _prtMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_notLatestCorrection() {
    PortfolioDocument base = _prtMaster.get(UniqueId.of("DbPrt", "201", "0"));
    _prtMaster.correct(base);  // correction
    base = _prtMaster.get(UniqueId.of("DbPrt", "201", "0"));  // get old version
    _prtMaster.correct(base);  // cannot update old correction
  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    UniqueId oldPortfolioId = UniqueId.of("DbPrt", "201", "0");
    PortfolioDocument base = _prtMaster.get(oldPortfolioId);
    ManageablePortfolio port = new ManageablePortfolio("NewName");
    port.setUniqueId(oldPortfolioId);
    port.setRootNode(base.getObject().getRootNode());
    PortfolioDocument input = new PortfolioDocument(port);
    
    PortfolioDocument corrected = _prtMaster.correct(input);
    assertEquals(UniqueId.of("DbPrt", "201"), corrected.getUniqueId().toLatest());
    assertEquals(false, base.getUniqueId().getVersion().equals(corrected.getUniqueId().getVersion()));
    assertEquals(_version1Instant, corrected.getVersionFromInstant());
    assertEquals(_version2Instant, corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getObject(), corrected.getObject());
    
    PortfolioDocument old = _prtMaster.get(oldPortfolioId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(_version1Instant, old.getVersionFromInstant());
    assertEquals(_version2Instant, old.getVersionToInstant());  // old version ended
    assertEquals(_version1Instant, old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());
    assertEquals("TestPortfolio201", old.getObject().getName());
    assertEquals("TestNode211", old.getObject().getRootNode().getName());
    
    PortfolioDocument newer = _prtMaster.get(corrected.getUniqueId());
    assertEquals(corrected.getUniqueId(), newer.getUniqueId());
    assertEquals(_version1Instant, newer.getVersionFromInstant());
    assertEquals(_version2Instant, newer.getVersionToInstant());
    assertEquals(now, newer.getCorrectionFromInstant());
    assertEquals(null, newer.getCorrectionToInstant());
    assertEquals("NewName", newer.getObject().getName());
    assertEquals("TestNode211", newer.getObject().getRootNode().getName());
    assertEquals(old.getObject().getRootNode().getUniqueId().toLatest(),
        newer.getObject().getRootNode().getUniqueId().toLatest());
    assertEquals(false, old.getObject().getRootNode().getUniqueId().getVersion().equals(
        newer.getObject().getRootNode().getUniqueId().getVersion()));
    
    PortfolioHistoryRequest search = new PortfolioHistoryRequest(base.getUniqueId(), _version1Instant.plusSeconds(5), null);
    PortfolioHistoryResult searchResult = _prtMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(corrected.getUniqueId(), searchResult.getDocuments().get(0).getUniqueId());
    assertEquals(oldPortfolioId, searchResult.getDocuments().get(1).getUniqueId());
  }

}
