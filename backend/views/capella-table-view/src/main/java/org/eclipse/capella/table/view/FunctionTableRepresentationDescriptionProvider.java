/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.capella.table.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.table.view.providers.CellIconURLsProvider;
import org.eclipse.capella.table.view.providers.CellOptionIdProvider;
import org.eclipse.capella.table.view.providers.CellOptionLabelProvider;
import org.eclipse.capella.table.view.providers.CellOptionsProvider;
import org.eclipse.capella.table.view.providers.CellStringValueProvider;
import org.eclipse.capella.table.view.providers.CellTypePredicate;
import org.eclipse.capella.table.view.providers.ColumnTargetObjectIdProvider;
import org.eclipse.capella.table.view.providers.TableLabelProvider;
import org.eclipse.capella.table.view.providers.TableTargetObjectIdProvider;
import org.eclipse.capella.table.view.providers.TableTargetObjectKindProvider;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextRepresentationDescriptionProvider;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.emf.tables.CursorBasedNavigationServices;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.tables.ColumnFilter;
import org.eclipse.sirius.components.tables.descriptions.ColumnDescription;
import org.eclipse.sirius.components.tables.descriptions.ICellDescription;
import org.eclipse.sirius.components.tables.descriptions.IconLabelCellDescription;
import org.eclipse.sirius.components.tables.descriptions.LineDescription;
import org.eclipse.sirius.components.tables.descriptions.PaginatedData;
import org.eclipse.sirius.components.tables.descriptions.SelectCellDescription;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.sirius.components.tables.descriptions.TextareaCellDescription;
import org.eclipse.sirius.components.tables.renderer.TableRenderer;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.impl.ActionUsageImpl;
import org.springframework.stereotype.Service;

/**
 * Description of the Logical Architecture Function Table using table description.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableRepresentationDescriptionProvider implements IEditingContextRepresentationDescriptionProvider {

    public static final String TABLE_DESCRIPTION_ID = "function_table_description";

    public static final String DESCRIPTION_NAME = "Logical Architecture Function Table";

    public static final int ROW_INITIAL_HEIGHT = 45;

    public static final int COLUMN_INITIAL_WIDTH = 240;

    private static final Map<ENamedElement, String> COLUMN_LABELS =
            Collections.unmodifiableMap(
                    new LinkedHashMap<ENamedElement, String>() {{
                        this.put(SysmlPackage.eINSTANCE.getLiteralString(), "Description");
                        this.put(SysmlPackage.eINSTANCE.getOwningMembership(), "Status");
                        this.put(SysmlPackage.eINSTANCE.getPartUsage(), "Allocated to Component");
                        this.put(SysmlPackage.eINSTANCE.getReferenceUsage(), "Function Ports");
                    }}
            );

    private static final Map<ENamedElement, String> COLUMN_URIS = COLUMN_LABELS.keySet()
            .stream()
            .collect(Collectors.toMap(
                    elt -> elt,
                    elt -> EcoreUtil.getURI(elt).toString()
            ));

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final TransverseQueryService transverseQueryService;

    public FunctionTableRepresentationDescriptionProvider(IIdentityService identityService,
            ILabelService labelService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.transverseQueryService = new TransverseQueryService();
    }


    @Override
    public List<IRepresentationDescription> getRepresentationDescriptions(IEditingContext editingContext) {
        Function<VariableManager, String> headerLabelProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.labelService::getStyledLabel)
                .map(Objects::toString)
                .orElse(null);

        Function<VariableManager, List<String>> headerIconURLsProvider = variableManager -> List.of("icons/full/obj16/_Function-wip-base with font.svg");

        Function<VariableManager, String> headerIndexLabelProvider = variableManager -> "";

        var lineDescription = LineDescription.newLineDescription(UUID.nameUUIDFromBytes("Function Table - Line".getBytes()).toString())
                .targetObjectIdProvider(new TableTargetObjectIdProvider(this.identityService))
                .targetObjectKindProvider(new TableTargetObjectKindProvider(this.identityService))
                .semanticElementsProvider(variableManager ->
                        this.getSemanticElements(variableManager, editingContext))
                .headerLabelProvider(headerLabelProvider)
                .headerIconURLsProvider(headerIconURLsProvider)
                .headerIndexLabelProvider(headerIndexLabelProvider)
                .isResizablePredicate(variableManager -> true)
                .initialHeightProvider(variableManager -> ROW_INITIAL_HEIGHT)
                .depthLevelProvider(this::getSemanticElementDepthLevel)
                .hasChildrenProvider(this::hasChildren)
                .build();

        var tableDescription = TableDescription.newTableDescription(TABLE_DESCRIPTION_ID)
                .label(DESCRIPTION_NAME)
                .labelProvider(new TableLabelProvider(this.labelService))
                .canCreatePredicate(this::canCreate)
                .lineDescription(lineDescription)
                .columnDescriptions(this.getColumnDescriptions())
                .targetObjectIdProvider(new TableTargetObjectIdProvider(this.identityService))
                .targetObjectKindProvider(new TableTargetObjectKindProvider(this.identityService))
                .cellDescriptions(this.getCellDescriptions(editingContext))
                .iconURLsProvider(variableManager -> List.of("/images/la-function-table-icon.svg"))
                .isStripeRowPredicate(variableManager -> true)
                .enableSubRows(true)
                .pageSizeOptionsProvider(variableManager -> List.of(5, 10, 20, 50))
                .defaultPageSizeIndexProvider(variableManager -> 1)
                .build();

        return List.of(tableDescription);
    }

    private PaginatedData getSemanticElements(VariableManager variableManager, IEditingContext editingContext) {
        var self = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        var cursor = variableManager.get(TableRenderer.PAGINATION_CURSOR, EObject.class).orElse(null);
        var direction = variableManager.get(TableRenderer.PAGINATION_DIRECTION, String.class).orElse(null);
        var size = variableManager.get(TableRenderer.PAGINATION_SIZE, Integer.class).orElse(0);
        var globalFilter = variableManager.get(TableRenderer.GLOBAL_FILTER_DATA, String.class).orElse(null);
        List<ColumnFilter> columnFilters = variableManager.get(TableRenderer.COLUMN_FILTERS, List.class).orElse(List.of());
        List<String> expandedIds = variableManager.get(TableRenderer.EXPANDED_IDS, List.class).orElse(List.of());
        List<String> activeRowFilterIds = variableManager.get(TableRenderer.ACTIVE_ROW_FILTER_IDS, List.class).orElse(List.of());
        boolean expandAll = variableManager.get(TableRenderer.EXPAND_ALL, Boolean.class).orElse(false);

        List<ActionUsage> actionUsages = this.transverseQueryService.getFunctions(self);

        Predicate<EObject> predicate = eObject -> {
            if (this.isFunction(eObject, actionUsages)) {
                boolean isAllAncestorsExpanded = this.isAllAncestorsExpanded(eObject, expandedIds, expandAll);
                boolean matchesGlobalFilter = this.matchesGlobal(eObject, globalFilter);
                boolean matchesRowFilters = this.matchesRow(eObject, activeRowFilterIds);
                boolean matchesColumnFilters = this.matchesColumn(eObject, columnFilters);

                return isAllAncestorsExpanded && matchesGlobalFilter
                        && matchesRowFilters && matchesColumnFilters;
            }

            return false;
        };

        return new CursorBasedNavigationServices().collect(self, cursor, direction, size,
                predicate);
    }

    private boolean isFunction(EObject eObject, List<ActionUsage> laPackageFunctions) {
        return eObject.getClass().equals(ActionUsageImpl.class)
                && laPackageFunctions.contains(eObject);
    }

    private boolean isAllAncestorsExpanded(EObject predicate,
            List<String> expandedIds, boolean expandAll) {

        Optional<ActionUsage> functionParent = this.transverseQueryService.getParentFunction(predicate);

        if (functionParent.isEmpty()) {
            return true;
        }

        String parentFunctionEntityId = this.identityService.getId(functionParent.get());

        return (!expandedIds.isEmpty()
                && expandedIds.contains(parentFunctionEntityId) || expandAll)
                && this.isAllAncestorsExpanded(functionParent.get(), expandedIds, expandAll);
    }

    private boolean matchesGlobal(EObject eObject, String globalFilter) {
        ActionUsage function = (ActionUsage) eObject;

        return globalFilter == null || globalFilter.isBlank() ||
                (function != null && this.contains(function.getDeclaredName(), globalFilter));
    }

    private boolean matchesRow(EObject eObject, List<String> activeRowFilterIds) {
        ActionUsage function = (ActionUsage) eObject;

        if (activeRowFilterIds.isEmpty()) {
            return true;
        }

        var status = this.transverseQueryService.getStatus(function);
        return activeRowFilterIds.stream().anyMatch(rowFilterId ->
                (status != null && rowFilterId.contains(status.getDeclaredName())) ||
                        (status == null && rowFilterId.equals("none-filter"))
        );
    }

    private boolean matchesColumn(EObject eObject, List<ColumnFilter> columnFilters) {
        ActionUsage function = (ActionUsage) eObject;

        if (columnFilters.isEmpty()) {
            return true;
        }

        return columnFilters.stream().allMatch(filter -> this.matchSingleColumn(function, filter));
    }

    private boolean matchSingleColumn(EObject eObject, ColumnFilter filter) {
        ActionUsage function = (ActionUsage) eObject;

        boolean result = false;
        String filterValue = filter.value().replace("\"", "").trim();

        if (filter.id().equals(COLUMN_URIS.get(SysmlPackage.eINSTANCE.getOwningMembership()))) {
            var optionalStatus = Optional.ofNullable(this.transverseQueryService.getStatus(function));
            var statusKind = optionalStatus.map(Element::getDeclaredName)
                    .orElse("");

            result = this.contains(statusKind, filterValue);

        } else if (filter.id().equals(COLUMN_URIS.get(SysmlPackage.eINSTANCE.getLiteralString()))) {
            var description = this.transverseQueryService.getArcadiaElementDescription(function);
            result = description != null && !description.isBlank() && this.contains(description, filterValue);

        } else if (filter.id().equals(COLUMN_URIS.get(SysmlPackage.eINSTANCE.getPartUsage()))) {
            Optional<PartUsage> allocatingComponent = this.transverseQueryService.getAllocatingComponent(function);

            result = allocatingComponent.isPresent()
                    && this.contains(allocatingComponent.get().getDeclaredName(), filterValue);

        } else if (filter.id().equals(COLUMN_URIS.get(SysmlPackage.eINSTANCE.getReferenceUsage()))) {
            List<Feature> functionPorts = this.transverseQueryService.getFunctionPorts(function);

            result = functionPorts != null
                    && !functionPorts.isEmpty()
                    && this.matchesListElement(functionPorts, filterValue);
        }

        return result;
    }

    private boolean contains(String s, String text) {
        if (s == null || text == null) {
            return false;
        }

        String cleanS = s.toLowerCase().replaceAll("[\\s_]+", "");
        String cleanText = text.toLowerCase().replaceAll("[\\s_]+", "");

        return cleanS.contains(cleanText);
    }

    private boolean matchesListElement(List<? extends Element> elementsList, String filterValue) {
        return elementsList.stream()
                .anyMatch(element -> this.contains(element.getDeclaredName(), filterValue));
    }

    private Integer getSemanticElementDepthLevel(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, EObject.class)
                .map(this::getFunctionLevel)
                .orElse(0);
    }

    private boolean hasChildren(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, EObject.class)
                .map(function -> !this.transverseQueryService.getSubFunctions(function).isEmpty())
                .orElse(false);
    }

    public int getFunctionLevel(EObject function) {
        int level = -1;
        var parent = this.transverseQueryService.getParentFunction(function);
        while (parent.isPresent()) {
            level++;
            parent = this.transverseQueryService.getParentFunction(parent.get());
        }
        return level;
    }

    private boolean canCreate(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class)
                .filter(Package.class::isInstance)
                .isPresent();
    }

    private List<ColumnDescription> getColumnDescriptions() {
        Function<VariableManager, String> headerLabelProvider = variableManager ->
                variableManager.get(VariableManager.SELF, ENamedElement.class)
                        .map(COLUMN_LABELS::get)
                        .orElse("");

        Function<VariableManager, List<String>> headerIconURLsProvider = variableManager -> List.of();

        Function<VariableManager, String> headerIndexLabelProvider = variableManager -> "";

        ColumnDescription columnDescription = ColumnDescription.newColumnDescription(UUID.nameUUIDFromBytes("Function attributes".getBytes()).toString())
                .semanticElementsProvider(variableManager -> COLUMN_LABELS.keySet().stream().map(Object.class::cast).toList())
                .headerLabelProvider(headerLabelProvider)
                .headerIconURLsProvider(headerIconURLsProvider)
                .headerIndexLabelProvider(headerIndexLabelProvider)
                .targetObjectIdProvider(new ColumnTargetObjectIdProvider())
                .targetObjectKindProvider(variableManager -> "")
                .initialWidthProvider(variableManager -> COLUMN_INITIAL_WIDTH)
                .isResizablePredicate(variableManager -> true)
                .isSortablePredicate(variableManager -> false)
                .filterVariantProvider(variableManager -> "text")
                .build();

        return List.of(columnDescription);
    }

    private List<ICellDescription> getCellDescriptions(IEditingContext editingContext) {
        List<ICellDescription> cellDescriptions = new ArrayList<>();

        cellDescriptions.add(TextareaCellDescription.newTextareaCellDescription("textareaCells")
                .canCreatePredicate(new CellTypePredicate().isTextareaCell())
                .targetObjectIdProvider(new TableTargetObjectIdProvider(this.identityService))
                .targetObjectKindProvider(new TableTargetObjectKindProvider(this.identityService))
                .cellValueProvider(new CellStringValueProvider())
                .cellTooltipValueProvider(new CellStringValueProvider())
                .build());

        cellDescriptions.add(SelectCellDescription.newSelectCellDescription("selectCells")
                .canCreatePredicate(new CellTypePredicate().isSelectCell())
                .targetObjectIdProvider(new TableTargetObjectIdProvider(this.identityService))
                .targetObjectKindProvider(new TableTargetObjectKindProvider(this.identityService))
                .cellValueProvider(new CellStringValueProvider())
                .cellOptionsIdProvider(new CellOptionIdProvider())
                .cellOptionsLabelProvider(new CellOptionLabelProvider())
                .cellOptionsProvider(new CellOptionsProvider(editingContext))
                .cellTooltipValueProvider(new CellStringValueProvider())
                .build());

        cellDescriptions.add(IconLabelCellDescription.newIconLabelCellDescription("labelCells")
                .canCreatePredicate(new CellTypePredicate().isLabelCell())
                .targetObjectIdProvider(new TableTargetObjectIdProvider(this.identityService))
                .targetObjectKindProvider(new TableTargetObjectKindProvider(this.identityService))
                .cellValueProvider(new CellStringValueProvider())
                .cellIconURLsProvider(new CellIconURLsProvider())
                .cellTooltipValueProvider(new CellStringValueProvider())
                .build()
        );

        return cellDescriptions;
    }
}
