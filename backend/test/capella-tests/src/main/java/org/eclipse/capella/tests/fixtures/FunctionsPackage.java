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
        // The root element is resolved by type rather than by name: depending on the
        // architecture it is named "Root Function" (System/Logical/Physical/EPBS) or
        // "Root Activity" (Operational Analysis), but a Functions package only ever owns
        // a single root ActionUsage.
        return new RootFunction(this.getOwnedElement(ActionUsage.class));
    }
}
