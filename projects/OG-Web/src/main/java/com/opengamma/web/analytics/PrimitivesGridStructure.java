/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 *
 */
public class PrimitivesGridStructure extends MainGridStructure {

  private PrimitivesGridStructure() {
  }

  private PrimitivesGridStructure(GridColumnGroups columnGroups, TargetLookup targetLookup) {
    super(columnGroups, targetLookup);
  }

  /* package */ static PrimitivesGridStructure create(CompiledViewDefinition compiledViewDef, ValueMappings valueMappings) {
    List<MainGridStructure.Row> rows = rows(compiledViewDef);
    GridColumn labelColumn = new GridColumn("Label", "", String.class, new PrimitivesLabelRenderer(rows));
    GridColumnGroup fixedColumns = new GridColumnGroup("fixed", ImmutableList.of(labelColumn));
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildColumns(compiledViewDef.getViewDefinition(), targetLookup);
    List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.addAll(analyticsColumns);
    return new PrimitivesGridStructure(new GridColumnGroups(groups), targetLookup);
  }

  private static List<GridColumnGroup> buildColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<GridColumn> columns = Lists.newArrayList();
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (specificRequirement.getTargetReference().getType().isTargetType(ComputationTargetType.PRIMITIVE)) {
          String valueName = specificRequirement.getValueName();
          Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          ValueProperties constraints = specificRequirement.getConstraints();
          ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
          columns.add(GridColumn.forKey(columnSpec, columnType, targetLookup));
        }
      }
      columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns));
    }
    return columnGroups;
  }

  private static List<MainGridStructure.Row> rows(CompiledViewDefinition compiledViewDef) {
    Set<ComputationTargetSpecification> specs = Sets.newLinkedHashSet();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDef.getCompiledCalculationConfigurations()) {
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
        if (targetSpec.getType() == ComputationTargetType.PRIMITIVE) {
          specs.add(targetSpec);
        }
      }
    }
    List<MainGridStructure.Row> rows = Lists.newArrayList();
    for (ComputationTargetSpecification spec : specs) {
      rows.add(new Row(spec, spec.getUniqueId().toString()));
    }
    return rows;
  }

  public static PrimitivesGridStructure empty() {
    return new PrimitivesGridStructure();
  }
}
