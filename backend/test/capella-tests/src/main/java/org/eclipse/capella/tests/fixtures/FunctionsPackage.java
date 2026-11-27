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

import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Package;

/**
 * Provides access to an architecture's Functions package.
 *
 * @author gdaniel
 */
public final class FunctionsPackage extends AbstractArcadiaElement<Package> {

    FunctionsPackage(Package element) {
        super(element);
    }

    public RootFunction getRootFunction() {
        String rootFunctionName = "Root Function";
        boolean hasRootFunction = this.getElement().getOwnedElement().stream()
                .anyMatch(element -> element instanceof ActionUsage && rootFunctionName.equals(element.getDeclaredName()));
        String rootName = rootFunctionName;
        if (!hasRootFunction) {
            rootName = "Root Activity";
        }
        return new RootFunction(this.getOwnedElement(rootName, ActionUsage.class));
    }
}
