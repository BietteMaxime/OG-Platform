/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Represents a rectangular set of cells visible in a grid. The viewport is defined by collections of row and
 * column indices of the visible cells. These are non-contiguous ordered sets. Row indices can be non-contiguous if
 * the grid rows have a tree structure and parts of the structure are collapsed and therefore not visible. Column
 * indices can be non-contiguous if columns are hidden or there is a fixed set of columns and the non-fixed columns
 * have been scrolled.
 */
public class RectangularViewportDefinition extends ViewportDefinition {

  /** Indices of rows in the viewport, not empty, sorted in ascending order. */
  private final List<Integer> _rows;
  /** Indices of columns in the viewport, not empty, sorted in ascending order. */
  private final List<Integer> _columns;

  /**
   * @param version
   * @param rows Indices of rows in the viewport, not empty
   * @param columns Indices of columns in the viewport, not empty
   * @param format
   */
  /* package */ RectangularViewportDefinition(int version,
                                              List<Integer> rows,
                                              List<Integer> columns,
                                              TypeFormatter.Format format) {
    super(version, format);
    ArgumentChecker.notEmpty(rows, "rows");
    ArgumentChecker.notEmpty(columns, "columns");
    // TODO bounds checking
    _rows = ImmutableList.copyOf(rows);
    _columns = ImmutableList.copyOf(columns);
  }

  @Override
  public Iterator<GridCell> iterator() {
    return new CellIterator();
  }

  @Override
  public boolean isValidFor(GridStructure grid) {
    if (!_rows.isEmpty()) {
      int maxRow = _rows.get(_rows.size() - 1);
      if (maxRow >= grid.getRowCount()) {
        return false;
      }
    }
    if (!_columns.isEmpty()) {
      int maxCol = _columns.get(_columns.size() - 1);
      if (maxCol >= grid.getColumnCount()) {
        return false;
      }
    }
    return true;
  }

  /* package */ List<Integer> getColumns() {
    return _columns;
  }

  /**
   * @return false
   */
  @Override
  boolean enableLogging() {
    return false;
  }

  @Override
  public String toString() {
    return "RectangularViewportDefinition [_rows=" + _rows + ", _columns=" + _columns + "]";
  }

  /**
   * Iterator that returns the viewports cells by traversing rows followed by columns.
   */
  private class CellIterator implements Iterator<GridCell> {

    private final Iterator<Integer> _rowIterator = _rows.iterator();

    private Iterator<Integer> _colIterator;
    private int _rowIndex;

    private CellIterator() {
      initRow();
    }

    private void initRow() {
      _rowIndex = _rowIterator.next();
      _colIterator = _columns.iterator();
    }

    @Override
    public boolean hasNext() {
      return _colIterator.hasNext() || _rowIterator.hasNext();
    }

    @Override
    public GridCell next() {
      if (!_colIterator.hasNext()) {
        initRow();
      }
      return new GridCell(_rowIndex, _colIterator.next());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported");
    }
  }
}
