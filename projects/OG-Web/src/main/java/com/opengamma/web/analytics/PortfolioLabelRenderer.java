/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Renders cells in the portfolio grid label column.
 */
/* package */ class PortfolioLabelRenderer implements GridColumn.CellRenderer {

  /** The rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  /* package */ PortfolioLabelRenderer(List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType) {
    PortfolioGridRow row = _rows.get(rowIndex);
    ComputationTargetSpecification target = row.getTarget();
    ComputationTargetType targetType = target.getType();
    // TODO do I need to use the target type to figure out the row type? can I just have different row types?
    if (targetType.isTargetType(ComputationTargetType.POSITION)) {
      RowTarget rowTarget;
      if (isOtc(row.getSecurity())) {
        // OTC trades and positions are shown as a single row as there's always one trade per position
        rowTarget = new OtcTradeTarget(row.getName(), row.getNodeId(), row.getPositionId(), row.getTradeId());
      } else {
        // Positions in fungible trades can contain multiple trades so the position has its own row and child rows
        // for each of its trades
        rowTarget = new PositionTarget(row.getName(), row.getNodeId(), row.getPositionId());
      }
      return ResultsCell.forStaticValue(rowTarget, columnType);
    } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
      return ResultsCell.forStaticValue(new NodeTarget(row.getName(), row.getNodeId()), columnType);
    } else if (targetType.isTargetType(ComputationTargetType.TRADE)) {
      // only fungible trades have their own row, OTC trades are shown on the same row as their parent position
      FungibleTradeTarget tradeTarget =
          new FungibleTradeTarget(row.getName(), row.getNodeId(), row.getPositionId(), row.getTradeId());
      return ResultsCell.forStaticValue(tradeTarget, columnType);
    }
    throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
  }

  private static boolean isOtc(Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }
}
