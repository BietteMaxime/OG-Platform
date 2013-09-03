/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * The grid viewport.
 */
public class PortfolioGridViewport extends MainGridViewport {

  /** The node structure. */
  private  ViewportNodeStructure _nodeStructure;
  /** The current expanded paths. */
  private Set<List<String>> _currentExpandedPaths;
  /** Row and column structure of the grid. */
  private MainGridStructure _gridStructure;
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioGridViewport.class);

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes initially
   * @param structureCallbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   * @param cycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  PortfolioGridViewport(MainGridStructure gridStructure,
                        String callbackId,
                        String structureCallbackId,
                        ViewportDefinition viewportDefinition,
                        ViewCycle cycle,
                        ResultsCache cache) {
    super(callbackId, structureCallbackId, viewportDefinition);
    _gridStructure = gridStructure;
    _nodeStructure = new ViewportNodeStructure(getGridStructure().getRootNode(), getGridStructure().getTargetLookup());
    _currentExpandedPaths = new HashSet<>(_nodeStructure.getInitialPaths());
    update(viewportDefinition, cycle, cache);
  }

  @Override
  public MainGridStructure getGridStructure() {
    return _gridStructure;
  }

  /**
   * Updates the structure of the tree nodes in the viewport.
   * called when the first set of results arrives after a view def recompilation
   * @param gridStructure The latest structure of the grid
   */
  public void updateResultsAndStructure(PortfolioGridStructure gridStructure) {
    ViewportNodeStructure node = new ViewportNodeStructure(gridStructure.getRootNode(),
                                                           gridStructure.getTargetLookup(),
                                                           _currentExpandedPaths);
    setViewportDefinition(ViewportDefinition.createEmpty(0));
    _gridStructure = gridStructure.withNode(node.getRootNode());
    _nodeStructure = new ViewportNodeStructure(getGridStructure().getRootNode(), getGridStructure().getTargetLookup());
  }

  /**
   * Updates the viewport definition (e.g. in response to the user scrolling the grid and changing the visible area).
   * @param viewportDefinition The new viewport definition
   * @param viewCycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  @Override
  public void update(ViewportDefinition viewportDefinition, ViewCycle viewCycle, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(getGridStructure())) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + getGridStructure());
    }
    if (getDefinition()  != null) {
      Pair<Integer, Boolean> changedNode = getDefinition().getChangedNode(viewportDefinition);
      // if this is null then the user scrolled the viewport and didn't expand or collapse a node
      if (changedNode != null) {
        Integer rowIndex = changedNode.getFirst();
        // was it expanded or collapsed
        Boolean expanded = changedNode.getSecond();
        List<String> path = _nodeStructure.getPathForRow(rowIndex);
        s_logger.debug("Node at row {} {}", rowIndex.toString(), expanded ? "expanded" : "collapsed");
        //System.out.println("Row: " + rowIndex.toString() + " Expanded: " + expanded.toString() + " Path: " + path);
        if (expanded) {
          _currentExpandedPaths.add(path);
        } else {
          _currentExpandedPaths.remove(path);
        }
        s_logger.debug("Current expanded set of nodes {}", _currentExpandedPaths);
        //System.out.println("Current: " + _currentExpandedPaths);
      }
    }
    setViewportDefinition(viewportDefinition);
    updateResults(cache);
  }

}
