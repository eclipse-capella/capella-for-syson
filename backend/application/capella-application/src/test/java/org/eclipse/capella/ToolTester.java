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
package org.eclipse.capella;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.InvokeSingleClickOnDiagramElementToolInput;
import org.eclipse.sirius.components.collaborative.diagrams.dto.InvokeSingleClickOnDiagramElementToolSuccessPayload;
import org.eclipse.sirius.components.diagrams.tests.graphql.InvokeSingleClickOnDiagramElementToolMutationRunner;
import org.eclipse.sirius.components.diagrams.tests.graphql.PaletteQueryRunner;
import org.springframework.stereotype.Service;

/**
 * Helper used by integration tests to invoke diagram tools.
 *
 * @author Jerome Gout
 */

@Service
public class ToolTester {

    private final InvokeSingleClickOnDiagramElementToolMutationRunner invokeSingleClickOnDiagramElementToolMutationRunner;

    private final PaletteQueryRunner paletteQueryRunner;

    public ToolTester(InvokeSingleClickOnDiagramElementToolMutationRunner invokeSingleClickOnDiagramElementToolMutationRunner, PaletteQueryRunner paletteQueryRunner) {
        this.invokeSingleClickOnDiagramElementToolMutationRunner = Objects.requireNonNull(invokeSingleClickOnDiagramElementToolMutationRunner);
        this.paletteQueryRunner = Objects.requireNonNull(paletteQueryRunner);
    }

    public void invokeDiagramTool(String editingContextId, String representationId, String toolLabel) {
        this.invokeTool(editingContextId, representationId, representationId, toolLabel);
    }

    public void invokeTool(String editingContextId, String representationId, String diagramElementId, String toolLabel) {
        String toolId = this.getToolId(editingContextId, representationId, diagramElementId, toolLabel);
        var input = new InvokeSingleClickOnDiagramElementToolInput(
                UUID.randomUUID(),
                editingContextId,
                representationId,
                List.of(diagramElementId),
                toolId,
                0,
                0,
                List.of());
        var result = this.invokeSingleClickOnDiagramElementToolMutationRunner.run(input).data();
        String typename = JsonPath.read(result, "$.data.invokeSingleClickOnDiagramElementTool.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(InvokeSingleClickOnDiagramElementToolSuccessPayload.class.getSimpleName());
        List<String> messages = JsonPath.read(result, "$.data.invokeSingleClickOnDiagramElementTool.messages[*].body");
        assertThat(messages).isEmpty();
    }

    private String getToolId(String editingContextId, String representationId, String diagramElementId, String toolLabel) {
        Map<String, Object> variables = Map.of(
                "editingContextId", editingContextId,
                "representationId", representationId,
                "diagramElementIds", List.of(diagramElementId)
        );
        var result = this.paletteQueryRunner.run(variables).data();
        List<String> toolIds = JsonPath.read(result,
                "$.data.viewer.editingContext.representation.description.palette.paletteEntries[?(@.label == '" + toolLabel + "')].id");
        assertThat(toolIds).as("The tool '%s' should be available", toolLabel).hasSize(1);
        return toolIds.getFirst();
    }
}
