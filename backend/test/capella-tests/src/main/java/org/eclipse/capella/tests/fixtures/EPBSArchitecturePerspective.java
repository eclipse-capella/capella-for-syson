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
 * Provides access to the EPBS Architecture package.
 *
 * @author gdaniel
 */
public final class EPBSArchitecturePerspective extends ArcadiaPerspective {

    EPBSArchitecturePerspective(Package element) {
        super(element);
    }
}
