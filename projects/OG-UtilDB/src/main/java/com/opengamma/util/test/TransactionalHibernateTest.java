/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * DB test involving Hibernate and transactions.
 */
public abstract class TransactionalHibernateTest extends HibernateTest {

  private HibernateTransactionManager _transactionManager;
  private TransactionStatus _transaction;

  public TransactionalHibernateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  @BeforeMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void setUp() throws Exception {
    super.setUp();
    startNewTransaction();
  }

  protected void startNewTransaction() {
    _transactionManager = new HibernateTransactionManager(getSessionFactory());
    _transaction = _transactionManager.getTransaction(new DefaultTransactionDefinition());
  }

  protected void commit() {
    if (_transaction != null && !_transaction.isRollbackOnly() && !_transaction.isCompleted()) {
      _transactionManager.commit(_transaction);
    }        
  }

  @AfterMethod(groups = {TestGroup.UNIT_DB, TestGroup.INTEGRATION})
  public void tearDown() throws Exception {
    commit();
    super.tearDown();
  }

  public HibernateTransactionManager getHibernateTransactionManager() {
    return _transactionManager;
  }

  public TransactionStatus getHibernateTransaction() {
    return _transaction;
  }

}
