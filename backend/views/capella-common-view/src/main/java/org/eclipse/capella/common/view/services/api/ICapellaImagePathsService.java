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

package org.eclipse.capella.common.view.services.api;

import java.util.List;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.syson.sysml.Element;

/**
 * Computes the image paths of the Capella icons associated to the provided elements.
 * <p>
 * Capella for SysON supports both SysML and Capella viewpoints, as a result, the same element may have a different icon in a Capella and SysML context (e.g. the Capella explorer vs the SysON
 * explorer). This service is used to retrieve the Capella-related icons, SysML-related icons can be retrieved via Sirius Web's {@link org.eclipse.sirius.components.core.api.ILabelService}.
 *
 * @author gdaniel
 */
public interface ICapellaImagePathsService {

    /**
     * Provides the image paths for the given Capella object.
     *
     * @param object
     *         the Capella object
     * @return the image paths.
     */
    List<String> getImagePaths(Object object);

    /**
     * Provides the image path for the given arcadia element.
     *
     * @param perspective
     *         the {@link ArcadiaEngineeringPerspective} (can be null)
     * @param element
     *         the typed {@link Element} (can be null)
     * @param arcadiaType
     *         the Arcadia type.
     * @return the optional image path.
     */
    Optional<String> getImageFromArcadiaType(ArcadiaEngineeringPerspective perspective, Element element, String arcadiaType);

}
