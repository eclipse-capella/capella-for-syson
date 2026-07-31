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

package org.eclipse.capella.tests.fixtures;

import static org.assertj.core.api.Assertions.fail;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.OccurrenceDefinition;
import org.eclipse.syson.sysml.Package;

/**
 * Provides access to the contents of a Capella test model.
 *
 * @author gdaniel
 */
public final class CapellaModel extends AbstractArcadiaElement<OccurrenceDefinition> {

    private final Resource resource;

    private CapellaModel(Resource resource, OccurrenceDefinition element) {
        super(element);
        this.resource = resource;
    }

    static CapellaModel from(Resource resource) {
        var optionalModel = resource.getContents().stream()
                .filter(Namespace.class::isInstance)
                .map(Namespace.class::cast)
                .flatMap(namespace -> namespace.getOwnedElement().stream())
                .filter(OccurrenceDefinition.class::isInstance)
                .map(OccurrenceDefinition.class::cast)
                .findFirst();
        if (optionalModel.isEmpty()) {
            fail("Could not find the Arcadia model in " + resource.getURI());
        }
        return new CapellaModel(resource, optionalModel.get());
    }

    public Resource getResource() {
        return this.resource;
    }

    public OperationalAnalysisPerspective getOperationalAnalysisPerspective() {
        return new OperationalAnalysisPerspective(this.getOwnedElement("Operational Analysis", Package.class));
    }

    public SystemAnalysisPerspective getSystemAnalysisPerspective() {
        return new SystemAnalysisPerspective(this.getOwnedElement("System Analysis", Package.class));
    }

    public LogicalArchitecturePerspective getLogicalArchitecturePerspective() {
        return new LogicalArchitecturePerspective(this.getOwnedElement("Logical Architecture", Package.class));
    }

    public PhysicalArchitecturePerspective getPhysicalArchitecturePerspective() {
        return new PhysicalArchitecturePerspective(this.getOwnedElement("Physical Architecture", Package.class));
    }

    public EPBSArchitecturePerspective getEPBSArchitecturePerspective() {
        return new EPBSArchitecturePerspective(this.getOwnedElement("EPBS Architecture", Package.class));
    }
}
