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
package org.eclipse.capella.application.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.capella.model.transverse.services.ArcadiaElementContentAdapter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.junit.jupiter.api.Test;

/**
 * Tests installation and isolation of the declared type index.
 *
 * @author Jerome Gout
 */
public class CapellaEditingContextProcessorTests {

    @Test
    public void postProcessShouldInstallExactlyOneIndexPerResourceSet() {
        var processor = new CapellaEditingContextProcessor();
        var first = mock(IEMFEditingContext.class, RETURNS_DEEP_STUBS);
        var second = mock(IEMFEditingContext.class, RETURNS_DEEP_STUBS);
        var firstResourceSet = new ResourceSetImpl();
        var secondResourceSet = new ResourceSetImpl();
        when(first.getDomain().getResourceSet()).thenReturn(firstResourceSet);
        when(second.getDomain().getResourceSet()).thenReturn(secondResourceSet);

        processor.postProcess(first);
        processor.postProcess(first);
        processor.postProcess(second);

        assertThat(firstResourceSet.eAdapters()).singleElement().isInstanceOf(ArcadiaElementContentAdapter.class);
        assertThat(secondResourceSet.eAdapters()).singleElement().isInstanceOf(ArcadiaElementContentAdapter.class);
        assertThat(firstResourceSet.eAdapters().getFirst()).isNotSameAs(secondResourceSet.eAdapters().getFirst());
    }

    @Test
    public void postProcessWhenContextIsNotEMFShouldIgnoreIt() {
        new CapellaEditingContextProcessor().postProcess(mock(IEditingContext.class));
    }
}
