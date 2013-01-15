/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.convert.StringConvert;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;

/**
 * Builds a security and adds it to the security master as a new security. The security data must not contain a
 * unique ID.
 */
/* package */ class NewOtcTradeBuilder extends OtcTradeBuilder {

  /* package */ NewOtcTradeBuilder(SecurityMaster securityMaster,
                                   PositionMaster positionMaster,
                                   Set<MetaBean> metaBeans,
                                   StringConvert stringConvert) {
    super(securityMaster, positionMaster, metaBeans, stringConvert);
  }

  /**
   * Saves a security using {@link SecurityMaster#add}. The security data must not contain a unique ID.
   * @param security The security
   * @return The saved security.
   */
  @Override
  /* package */ ManageableSecurity saveSecurity(ManageableSecurity security) {
    return getSecurityMaster().add(new SecurityDocument(security)).getSecurity();
  }

  /**
   * Saves the position using {@link PositionMaster#add}. The position data must contain a unique ID.
   * @param position The position
   * @return The saved position
   */
  @Override
  /* package */ ManageablePosition savePosition(ManageablePosition position) {
    return getPositionMaster().add(new PositionDocument(position)).getPosition();
  }

  @Override
  ManageablePosition getPosition(ManageableTrade trade) {
    ManageablePosition position = new ManageablePosition();
    position.setQuantity(trade.getQuantity());
    position.setSecurityLink(new ManageableSecurityLink(trade.getSecurityLink()));
    position.addTrade(trade);
    return position;
  }
}
