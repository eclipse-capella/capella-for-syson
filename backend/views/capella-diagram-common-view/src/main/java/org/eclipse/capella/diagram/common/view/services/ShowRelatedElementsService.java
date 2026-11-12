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

package org.eclipse.capella.diagram.common.view.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.representations.RepresentationVariables;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * Displays elements related to a given context on diagrams.
 *
 * @author Jerome Gout
 */
@Service
public class ShowRelatedElementsService {

    private final DiagramMutationElementService diagramMutationElementService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    public ShowRelatedElementsService(DiagramMutationElementService diagramMutationElementService, IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
    }

    /**
     * Displays elements contained in the diagram's target object.
     * <p>
     * This method is <i>not</i> recursive: it only displays the direct children of the diagram's target object ({@code self}), and place them on the diagram itself.
     * This method only works on diagrams for now (i.e. {@code selectedNode == null}): calling it on an existing node won't display anything.
     *
     * @param self
     *         the element containing elements to display
     * @param editingContext
     *         the editing context
     * @param diagramContext
     *         the diagram context
     * @param convertedNodes
     *         the mapping from view node descriptions to converted diagram node descriptions
     * @return {@code self}
     */
    public Element showAllContainedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<Element> containedElements = this.getContainedElements(self, selectedNode, editingContext, diagramContext);
        for (Element containedElement : containedElements) {
            if (selectedNode == null) {
                this.diagramMutationElementService.createView(containedElement, editingContext, diagramContext, null, convertedNodes);
            }
        }
        return self;
    }

    private List<Element> getContainedElements(Element self, Node selectedNode, IEditingContext editingContext, DiagramContext diagramContext) {
        List<Element> result = new ArrayList<>();
        if (selectedNode == null) {
            // There is no selected node: we are looking for the contained element on the diagram.
            // We retrieve them by iterating the semantic candidates of the top-level node descriptions.
            Optional<DiagramDescription> optionalDiagramDescription = this.representationDescriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId())
                    .filter(DiagramDescription.class::isInstance)
                    .map(DiagramDescription.class::cast);
            if (optionalDiagramDescription.isPresent()) {
                DiagramDescription diagramDescription = optionalDiagramDescription.get();
                VariableManager variableManager = new VariableManager();
                variableManager.put(RepresentationVariables.SELF.name(), self);
                variableManager.put(CoreVariables.EDITING_CONTEXT.name(), editingContext);
                variableManager.put(DiagramContext.DIAGRAM_CONTEXT, diagramContext);
                for (NodeDescription nodeDescription : diagramDescription.getNodeDescriptions()) {
                    List<Element> elements = nodeDescription.getSemanticElementsProvider().apply(variableManager.createChild())
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

}
