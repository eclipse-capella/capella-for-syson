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

package org.eclipse.capella.common.view.diagram.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.common.view.diagram.services.api.IShowContainedElementsService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.representations.RepresentationVariables;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * Services to show the contained elements in the provided context.
 *
 * @author gdaniel
 */
@Service
public class ShowContainedElementsService implements IShowContainedElementsService {

    private final DiagramMutationElementService diagramMutationElementService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    public ShowContainedElementsService(DiagramMutationElementService diagramMutationElementService, IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
    }

    public Element showAllContainedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<Element> containedElements = this.getContainedElements(self, selectedNode, editingContext, diagramContext, convertedNodes);
        for (Element containedElement : containedElements) {
            this.diagramMutationElementService.createView(containedElement, editingContext, diagramContext, selectedNode, convertedNodes);
        }
        return self;
    }

    public Element showAllContainedElementsRecursive(Element self, IEditingContext editingContext, DiagramContext diagramContext, Object graphicalParent,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<Element> containedElements = this.getContainedElements(self, graphicalParent, editingContext, diagramContext, convertedNodes);
        for (Element containedElement : containedElements) {
            ViewCreationRequest viewCreationRequest = this.diagramMutationElementService.createView(containedElement, editingContext, diagramContext, graphicalParent, convertedNodes);
            this.showAllContainedElementsRecursive(containedElement, editingContext, diagramContext, viewCreationRequest, convertedNodes);
        }
        return self;
    }

    private List<Element> getContainedElements(Element self, Object graphicalParent, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<Element> result = new ArrayList<>();

        VariableManager variableManager = new VariableManager();
        variableManager.put(RepresentationVariables.SELF.name(), self);
        variableManager.put(CoreVariables.EDITING_CONTEXT.name(), editingContext);
        variableManager.put(DiagramContext.DIAGRAM_CONTEXT, diagramContext);

        if (graphicalParent == null) {
            // There is no graphical parent: we are looking for the contained element on the diagram.
            // We retrieve them by iterating the semantic candidates of the top-level node descriptions.
            Optional<DiagramDescription> optionalDiagramDescription = this.representationDescriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId())
                    .filter(DiagramDescription.class::isInstance)
                    .map(DiagramDescription.class::cast);
            if (optionalDiagramDescription.isPresent()) {
                DiagramDescription diagramDescription = optionalDiagramDescription.get();
                for (NodeDescription nodeDescription : diagramDescription.getNodeDescriptions()) {
                    List<Element> elements = nodeDescription.getSemanticElementsProvider().apply(variableManager.createChild())
                            .stream()
                            .filter(Element.class::isInstance)
                            .map(Element.class::cast)
                            // Only include direct semantic children when looking for elements on the diagram.
                            // This implies that Requirements aren't considered by this method, since they are always defined in their own package.
                            .filter(element -> self.getOwnedElement().contains(element))
                            .toList();
                    result.addAll(elements);
                }
            }
        } else {
            Optional<NodeDescription> optionalNodeDescription = convertedNodes.values().stream()
                    .filter(nodeDescription -> Objects.equals(nodeDescription.getId(), this.getNodeDescriptionId(graphicalParent)))
                    .findFirst();
            if (optionalNodeDescription.isPresent()) {
                NodeDescription nodeDescription = optionalNodeDescription.get();
                List<NodeDescription> childNodeDescriptions = this.getAllChildNodeDescriptions(nodeDescription, convertedNodes);
                for (NodeDescription childNodeDescription : childNodeDescriptions) {
                    List<Element> elements = childNodeDescription.getSemanticElementsProvider().apply(variableManager.createChild())
                            .stream()
                            .filter(Element.class::isInstance)
                            .map(Element.class::cast)
                            .toList();
                    result.addAll(elements);
                }
            }
        }
        return result;
    }

    private String getNodeDescriptionId(Object graphicalElement) {
        String result = null;
        if (graphicalElement instanceof Node node) {
            result = node.getDescriptionId();
        } else if (graphicalElement instanceof ViewCreationRequest viewCreationRequest) {
            result = viewCreationRequest.getDescriptionId();
        }
        return result;
    }

    private List<NodeDescription> getAllChildNodeDescriptions(NodeDescription nodeDescription, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<NodeDescription> result = new ArrayList<>();
        result.addAll(nodeDescription.getChildNodeDescriptions());
        result.addAll(convertedNodes.values().stream()
                .filter(convertedNode -> nodeDescription.getReusedChildNodeDescriptionIds().contains(convertedNode.getId()))
                .toList());
        return result;
    }
}
