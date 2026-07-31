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

import org.eclipse.syson.sysml.Package;

/**
 * Base class for Arcadia architecture packages.
 *
 * @author gdaniel
 */
public abstract class ArcadiaPerspective extends AbstractArcadiaElement<Package> {

    protected ArcadiaPerspective(Package element) {
        super(element);
    }

    public FunctionsPackage getFunctionsPackage() {
        return new FunctionsPackage(this.getOwnedElement("Functions", Package.class));
    }

    public CapabilitiesPackage getCapabilitiesPackage() {
        return new CapabilitiesPackage(this.getOwnedElement("Capabilities", Package.class));
    }

    public InterfacesPackage getInterfacesPackage() {
        return new InterfacesPackage(this.getOwnedElement("Interfaces", Package.class));
    }

    public DataPackage getDataPackage() {
        return new DataPackage(this.getOwnedElement("Data", Package.class));
    }

    public StructurePackage getStructurePackage() {
        return new StructurePackage(this.getOwnedElement("Structure", Package.class));
    }

    public RequirementsPackage getRequirementsPackage() {
        return new RequirementsPackage(this.getOwnedElement("Requirements", Package.class));
    }
}
