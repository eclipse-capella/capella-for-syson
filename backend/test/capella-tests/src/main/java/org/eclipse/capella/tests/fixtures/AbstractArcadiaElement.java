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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.syson.sysml.Element;

/**
 * Base class for elements exposed by the semantic data test fixture.
 *
 * @param <T> the type of the wrapped element
 * @author gdaniel
 */
public abstract class AbstractArcadiaElement<T extends Element> {

    private final T element;

    protected AbstractArcadiaElement(T element) {
        this.element = Objects.requireNonNull(element);
    }

    public T getElement() {
        return this.element;
    }

    protected <E extends Element> E getOwnedElement(String name, Class<E> type) {
        Optional<E> optionalOwnedElement = this.element.getOwnedElement().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(ownedElement -> Objects.equals(ownedElement.getDeclaredName(), name))
                .findFirst();
        if (optionalOwnedElement.isEmpty()) {
            fail("Could not find ownedElement '" + name + "' in '" + this.element.getDeclaredName() + "'");
        }
        return optionalOwnedElement.get();
    }
}
