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
package org.eclipse.capella.application.configuration.template;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.ICausalityChainVisitor;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.project.api.ICreateProjectInput;
import org.eclipse.sirius.web.application.project.services.api.ISemanticDataInitializer;
import org.eclipse.sirius.web.domain.boundedcontexts.project.events.ProjectCreatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.events.ProjectSemanticDataCreatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.events.SemanticDataCreatedEvent;
import org.eclipse.syson.application.sysmlv2.SysMLv2TemplatesInitialization;
import org.eclipse.syson.application.sysmlv2.api.IDefaultSysMLv2ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Provides capella semantic data templates initializer.
 *
 * @author frouene
 */
@Configuration
public class CapellaSemanticDataTemplateInitializer implements ISemanticDataInitializer {

    private final IEditingContextPersistenceService editingContextPersistenceService;

    private final IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider;

    private final ICausalityChainVisitor causalityChainVisitor;

    private final Logger logger = LoggerFactory.getLogger(CapellaSemanticDataTemplateInitializer.class);

    public CapellaSemanticDataTemplateInitializer(IEditingContextPersistenceService editingContextPersistenceService, IDefaultSysMLv2ResourceProvider defaultSysMLv2ResourceProvider,
            ICausalityChainVisitor causalityChainVisitor) {
        this.editingContextPersistenceService = Objects.requireNonNull(editingContextPersistenceService);
        this.defaultSysMLv2ResourceProvider = Objects.requireNonNull(defaultSysMLv2ResourceProvider);
        this.causalityChainVisitor = Objects.requireNonNull(causalityChainVisitor);
    }

    @Override
    public boolean canHandle(String templateId) {
        return CapellaLibraryProjectTemplateProvider.CAPELLA_LIBRARY_TEMPLATE_ID.equals(templateId) || CapellaProjectTemplateProvider.CAPELLA_TEMPLATE_ID.equals(templateId);
    }

    @Override
    public void handle(ICause cause, IEditingContext editingContext, String templateId) {
        if (CapellaLibraryProjectTemplateProvider.CAPELLA_LIBRARY_TEMPLATE_ID.equals(templateId) && editingContext instanceof IEMFEditingContext emfEditingContext) {
            this.initializeCapellaLibraryProject(cause, emfEditingContext);
        }
        if (CapellaProjectTemplateProvider.CAPELLA_TEMPLATE_ID.equals(templateId) && editingContext instanceof IEMFEditingContext emfEditingContext) {
            this.initializeCapellaProject(cause, emfEditingContext);
        }
    }

    private void initializeCapellaLibraryProject(ICause cause, IEMFEditingContext emfEditingContext) {
        var resourceSet = emfEditingContext.getDomain().getResourceSet();
        var resource = this.defaultSysMLv2ResourceProvider.getEmptyResource(UUID.randomUUID(), "capella-library");
        resourceSet.getResources().add(resource);
        this.editingContextPersistenceService.persist(new SysMLv2TemplatesInitialization(UUID.randomUUID(), emfEditingContext, resource, cause), emfEditingContext);
    }

    private void initializeCapellaProject(ICause cause, IEMFEditingContext emfEditingContext) {
        var resourceSet = emfEditingContext.getDomain().getResourceSet();
        var resourceLabel = "capella.sysml";
        if (cause instanceof ProjectSemanticDataCreatedEvent projectSemanticDataCreatedEvent) {
            resourceLabel = this.causalityChainVisitor.findFirstCauseOfType(projectSemanticDataCreatedEvent, SemanticDataCreatedEvent.class)
                    .flatMap(semanticDataCreatedEvent -> this.causalityChainVisitor.findFirstCauseOfType(semanticDataCreatedEvent, ProjectCreatedEvent.class))
                    .flatMap(projectCreatedEvent -> this.causalityChainVisitor.findFirstCauseOfType(projectCreatedEvent, ICreateProjectInput.class))
                    .map(createProjectInput -> createProjectInput.name() + ".sysml")
                    .orElse(resourceLabel);
        }
        var resource = this.defaultSysMLv2ResourceProvider.getEmptyResource(UUID.randomUUID(), resourceLabel);
        resourceSet.getResources().add(resource);
        this.loadCapellaTemplateResource(resource);
        this.editingContextPersistenceService.persist(new SysMLv2TemplatesInitialization(UUID.randomUUID(), emfEditingContext, resource, cause), emfEditingContext);
    }

    private void loadCapellaTemplateResource(Resource resource) {
        String templatePath = "arcadia/structure/arcadiaDefaultStructure.json";
        try (var inputStream = new ClassPathResource(templatePath).getInputStream()) {
            resource.load(inputStream, null);
        } catch (IOException exception) {
            this.logger.atWarn()
                    .setMessage("Cannot find project template")
                    .addKeyValue("templatePath", templatePath)
                    .setCause(exception)
                    .log();
        }
    }

}
