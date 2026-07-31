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
package org.eclipse.capella.form.view.util;

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Used to provide the page description for the information displayed in the form.
 *
 * @technical-debt This implementation has been developed in the context of a POC.
 * As such, it focuses on validating functional ideas rather than providing a fully
 * generic, extensible, or optimized solution. Several parts of this service rely
 * on hard-coded concepts, assumptions on SysML/Arcadia structures, and duplicated
 * traversal logic, which may limit reuse and scalability. A future industrialization
 * phase should consider refactoring toward more generic mechanisms, improved separation
 * of concerns, and better configurability.
 *
 * @author ntinsalhi
 */
@Service
public class CapellaViewFormService {

    public static final String PIE_CHART_FUNCTIONS = "Functions";

    public static final String PIE_CHART_COMPONENTS = "Components";

    public static final String PIE_CHART_ACTORS = "Actors";

    public static final String PIE_CHART_REQUIREMENTS = "Requirements";

    public static final String PIE_CHART_INTERFACES = "Interfaces";

    public static final String PIE_CHART_EXCHANGES = "Exchanges";

    public static final String PIE_CHART_PORTS = "Ports";

    public static final String COLOR_RED = "#EE4B2B";

    public static final String COLOR_ORANGE = "#FF991C";

    public static final String COLOR_GREEN = "#50C878";

    private final TransverseQueryService transverseQueryService;

    private final LAQueryService laQueryService;

    public CapellaViewFormService() {
        this.transverseQueryService = new TransverseQueryService();
        this.laQueryService = new LAQueryService();
    }

    public List<String> getConceptsRepartitionPieChartKeys(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);
        return this.getConceptEntries(rootPackage).stream()
                .filter(e -> e.getValue().intValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Number> getConceptsRepartitionPieChartValues(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);
        return this.getConceptEntries(rootPackage).stream()
                .filter(e -> e.getValue().intValue() > 0)
                .map(Map.Entry::getValue)
                .toList();
    }

    private List<Map.Entry<String, Number>> getConceptEntries(Package rootPackage) {
        return List.of(
                Map.entry(PIE_CHART_FUNCTIONS, this.getFunctions(rootPackage).size()),
                Map.entry(PIE_CHART_COMPONENTS, this.getComponents(rootPackage).size()),
                Map.entry(PIE_CHART_ACTORS, this.getActors(rootPackage).size()),
                Map.entry(PIE_CHART_REQUIREMENTS, this.getRequirements(rootPackage).size()),
                Map.entry(PIE_CHART_INTERFACES, this.getInterfaces(rootPackage).size()),
                Map.entry(PIE_CHART_EXCHANGES, this.getExchanges(rootPackage).size()),
                Map.entry(PIE_CHART_PORTS, this.getPorts(rootPackage).size())
        );
    }

    public List<Number> getConceptsRepartitionPerLayerPieChartValues(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);
        List<EObject> arcadiaObjects = this.getAllArcadiaObjects(rootPackage);

        Number operationalObjects = this.countConceptsPerLayer(arcadiaObjects, ArcadiaEngineeringPerspective.OperationalAnalysis.getLabel());
        Number systemObjects = this.countConceptsPerLayer(arcadiaObjects, ArcadiaEngineeringPerspective.SystemAnalysis.getLabel());
        Number logicalObjects = this.countConceptsPerLayer(arcadiaObjects, ArcadiaEngineeringPerspective.LogicalArchitecture.getLabel());
        Number physicalObjects = this.countConceptsPerLayer(arcadiaObjects, ArcadiaEngineeringPerspective.PhysicalArchitecture.getLabel());

        return List.of(operationalObjects, systemObjects, logicalObjects, physicalObjects);
    }

    public List<Number> getConceptsRepartitionPerStatusLayerPieChartValues(VariableManager variableManager, String status) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);
        List<EObject> arcadiaObjects = this.getAllArcadiaObjects(rootPackage);

        List<EObject> filtered = this.filterByStatus(arcadiaObjects, status);

        Number operationalObjects = this.countConceptsPerLayer(filtered, ArcadiaEngineeringPerspective.OperationalAnalysis.getLabel());
        Number systemObjects = this.countConceptsPerLayer(filtered, ArcadiaEngineeringPerspective.SystemAnalysis.getLabel());
        Number logicalObjects = this.countConceptsPerLayer(filtered, ArcadiaEngineeringPerspective.LogicalArchitecture.getLabel());
        Number physicalObjects = this.countConceptsPerLayer(filtered, ArcadiaEngineeringPerspective.PhysicalArchitecture.getLabel());

        return List.of(operationalObjects, systemObjects, logicalObjects, physicalObjects);
    }

    private List<EObject> getAllArcadiaObjects(Package rootPackage) {
        List<EObject> arcadiaObjects = new ArrayList<>();

        arcadiaObjects.addAll(this.getFunctions(rootPackage));
        arcadiaObjects.addAll(this.getComponents(rootPackage));
        arcadiaObjects.addAll(this.getRequirements(rootPackage));
        arcadiaObjects.addAll(this.getInterfaces(rootPackage));
        arcadiaObjects.addAll(this.getExchanges(rootPackage));
        arcadiaObjects.addAll(this.getPorts(rootPackage));

        return arcadiaObjects;
    }

    private Number countConceptsPerLayer(List<EObject> objects, String layerName) {
        return (int) objects.stream()
                .map(Element.class::cast)
                .filter(obj -> this.transverseQueryService
                        .getArcadiaPerspectivePackage(obj).get()
                        .getDeclaredName()
                        .contains(layerName))
                .count();
    }

    private List<EObject> filterByStatus(List<EObject> objects, String status) {
        return objects.stream()
                .filter(obj -> {
                    Element s = this.laQueryService.getStatus((Feature) obj);
                    return s != null && status.equals(s.getDeclaredName());
                })
                .toList();
    }

    private List<EObject> getFunctions(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getActionUsage())
                .stream()
                .filter(this.laQueryService::isFunction)
                .toList();
    }

    private List<EObject> getComponents(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getPartUsage())
                .stream()
                .filter(this.transverseQueryService::isComponent)
                .toList();
    }

    private List<EObject> getActors(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getPartUsage())
                .stream()
                .filter(this.transverseQueryService::isComponentActor)
                .toList();
    }

    private List<EObject> getRequirements(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getRequirementUsage())
                .stream()
                .filter(this.transverseQueryService::isRequirement)
                .toList();
    }

    private List<EObject> getInterfaces(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getInterfaceUsage())
                .stream()
                .filter(this.transverseQueryService::isComponentExchange)
                .toList();
    }

    private List<EObject> getExchanges(Package rootPackage) {
        return this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getFlowUsage())
                .stream()
                .filter(this.laQueryService::isFunctionalExchange)
                .toList();
    }

    private List<EObject> getPorts(Package rootPackage) {
        List<EObject> ports = new ArrayList<>();
        ports.addAll(this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getItemUsage())
                .stream()
                .filter(this.laQueryService::isFunctionPort)
                .toList());
        ports.addAll(this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getPortUsage())
                .stream()
                .filter(this.transverseQueryService::isComponentPort)
                .toList());
        return ports;
    }

    public ComponentProgress getNonAllocatedFunctionWidgetValue(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);

        List<ActionUsage> totalFunctions = this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getActionUsage())
                .stream()
                .filter(eObject -> eObject instanceof ActionUsage)
                .map(ActionUsage.class::cast)
                .toList();

        List<ActionUsage> nonAllocatedFunction = totalFunctions.stream()
                .filter(function -> this.laQueryService.getAllocatingComponent(function).isEmpty())
                .toList();

        return new ComponentProgress(nonAllocatedFunction.size(), totalFunctions.size());
    }

    public ComponentProgress getPortsWithNoExchangeWidgetValue(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);

        List<ItemUsage> totalFunctionPorts = this.transverseQueryService
                .getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getItemUsage())
                .stream()
                .filter(this.laQueryService::isFunctionPort)
                .map(ItemUsage.class::cast)
                .toList();

        List<PortUsage> totalComponentPorts = this.transverseQueryService
                .getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getPortUsage())
                .stream()
                .filter(this.transverseQueryService::isComponentPort)
                .map(PortUsage.class::cast)
                .toList();

        List<ItemUsage> nonAssignedFunctionPorts = this.getNonAssignedFunctionalExchangePorts(rootPackage, totalFunctionPorts);
        List<PortUsage> nonAssignedComponentPorts = this.getNonAssignedComponentExchangePorts(rootPackage, totalComponentPorts);

        return new ComponentProgress(
                nonAssignedFunctionPorts.size() + nonAssignedComponentPorts.size(),
                totalFunctionPorts.size() + totalComponentPorts.size()
        );
    }

    private List<ItemUsage> getNonAssignedFunctionalExchangePorts(Package rootPackage, List<ItemUsage> totalFunctionPorts) {

        List<ItemUsage> assignedPorts = this.laQueryService.getFunctionalExchanges(rootPackage).stream()
                .flatMap(fe -> this.laQueryService.getExchangePorts(fe).stream())
                .filter(ItemUsage.class::isInstance)
                .map(ItemUsage.class::cast)
                .toList();

        return totalFunctionPorts.stream()
                .filter(port -> !assignedPorts.contains(port))
                .toList();
    }

    private List<PortUsage> getNonAssignedComponentExchangePorts(Package rootPackage, List<PortUsage> totalComponentPorts) {

        List<PortUsage> assignedPorts = this.transverseQueryService.getComponentExchanges(rootPackage).stream()
                .flatMap(fe -> this.laQueryService.getExchangePorts(fe).stream())
                .filter(PortUsage.class::isInstance)
                .map(PortUsage.class::cast)
                .toList();

        return totalComponentPorts.stream()
                .filter(port -> !assignedPorts.contains(port))
                .toList();
    }

    public ComponentProgress getNonAllocatedFunctionalExchangesWidgetValue(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);

        List<FlowUsage> totalFunctionalExchanges = this.laQueryService.getFunctionalExchanges(rootPackage);
        List<FlowUsage> nonAllocatedFunctionalExchanges = totalFunctionalExchanges.stream()
                .filter(functionalExchange -> this.getAllocatingComponentExchanges(functionalExchange).isEmpty())
                .toList();

        return new ComponentProgress(nonAllocatedFunctionalExchanges.size(), totalFunctionalExchanges.size());
    }

    private List<InterfaceUsage> getAllocatingComponentExchanges(FlowUsage functionalExchange) {
        return functionalExchange.eAdapters().stream()
                .filter(ECrossReferenceAdapter.class::isInstance)
                .map(ECrossReferenceAdapter.class::cast)
                .findFirst()
                .map(adapter ->
                        adapter.getInverseReferences(functionalExchange).stream()
                                .map(EStructuralFeature.Setting::getEObject)
                                .filter(Membership.class::isInstance)
                                .map(this::findParentInterfaceUsage)
                                .filter(Objects::nonNull)
                                .toList())
                .orElseGet(List::of);
    }

    private InterfaceUsage findParentInterfaceUsage(EObject eObject) {
        InterfaceUsage result = null;
        if (eObject != null) {
            if (eObject instanceof InterfaceUsage interfaceUsage) {
                result = interfaceUsage;
            } else {
                result = this.findParentInterfaceUsage(eObject.eContainer());
            }
        }
        return result;
    }

    public ComponentProgress getValidatedComponents(VariableManager variableManager) {
        Package rootPackage = (Package) variableManager.getVariables().get(VariableManager.SELF);

        List<PartUsage> totalComponents = this.transverseQueryService.getAllReachableInResource(rootPackage, SysmlPackage.eINSTANCE.getPartUsage())
                .stream()
                .filter(this.transverseQueryService::isComponent)
                .map(PartUsage.class::cast)
                .toList();

        List<PartUsage> doneComponents = totalComponents.stream()
                .filter(component -> {
                    Element componentStatus = this.laQueryService.getStatus(component);
                    return Objects.nonNull(componentStatus) && componentStatus.getDeclaredName().equals("done");
                })
                .toList();


        return new ComponentProgress(doneComponents.size(), totalComponents.size());
    }

    public String getNonAllocatedFunctionsColor(VariableManager variableManager) {
        double componentProgressRatio = this.getNonAllocatedFunctionWidgetValue(variableManager).getRatio();
        return this.computeColor(componentProgressRatio, true);
    }

    public String getPortsWithNoExchangeColor(VariableManager variableManager) {
        double componentProgressRatio = this.getPortsWithNoExchangeWidgetValue(variableManager).getRatio();
        return this.computeColor(componentProgressRatio, true);
    }

    public String getNonAllocatedFunctionalExchanges(VariableManager variableManager) {
        double componentProgressRatio = this.getNonAllocatedFunctionalExchangesWidgetValue(variableManager).getRatio();
        return this.computeColor(componentProgressRatio, true);
    }

    public String getValidatedComponentsColor(VariableManager variableManager) {
        double componentProgressRatio = this.getValidatedComponents(variableManager).getRatio();
        return this.computeColor(componentProgressRatio, false);
    }

    private String computeColor(double ratio, boolean invert) {
        String color;
        if (invert) {
            if (ratio == 1.0) {
                color = COLOR_RED;
            } else if (ratio >= 0.5) {
                color = COLOR_ORANGE;
            } else {
                color = COLOR_GREEN;
            }
        } else {
            if (ratio == 1.0) {
                color = COLOR_GREEN;
            } else if (ratio >= 0.5) {
                color = COLOR_ORANGE;
            } else {
                color = COLOR_RED;
            }
        }
        return color;
    }
}
