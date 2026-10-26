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
package org.eclipse.capella.diagram.customization.styles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests deterministic allocation, survivor stability and palette changes.
 *
 * @author Jerome Gout
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class FunctionalChainColorAllocatorTests {

    private final FunctionalChainColorAllocator allocator = new FunctionalChainColorAllocator();

    @Test
    public void deletionPreservesSurvivorsAndAdditionReusesFirstReleasedColor() {
        var palette = FunctionalChainStyleConstants.COLORS;
        var initial = this.allocator.allocate(List.of("a", "b", "c"), Map.of(), palette);
        var survivors = this.allocator.allocate(List.of("b", "c"), initial, palette);
        assertThat(survivors).containsEntry("b", palette.get(1)).containsEntry("c", palette.get(2)).doesNotContainKey("a");
        assertThat(this.allocator.allocate(List.of("b", "c", "d"), survivors, palette))
                .containsEntry("b", palette.get(1)).containsEntry("c", palette.get(2)).containsEntry("d", palette.get(0));
    }

    @Test
    public void sharedColorRemainsReservedAndOverflowUsesCurrentCanonicalRank() {
        var colors = this.allocator.allocate(List.of("a", "b", "c", "d"), Map.of("b", "blue", "d", "blue"), List.of("blue", "green"));
        assertThat(colors).containsEntry("a", "green").containsEntry("b", "blue").containsEntry("c", "blue").containsEntry("d", "blue");
        assertThat(this.allocator.allocate(List.of("c", "d", "e"), Map.of("c", "blue", "d", "blue"), List.of("blue", "green")))
                .containsEntry("c", "blue").containsEntry("d", "blue").containsEntry("e", "green");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 4, 7 })
    public void paletteSizeControlsInitializationAndInvalidOldColorsAreReallocated(int size) {
        var palette = FunctionalChainStyleConstants.COLORS.subList(0, size);
        var colors = this.allocator.allocate(List.of("a", "b", "c"), Map.of("b", "removed"), palette);
        if (size == 0) {
            assertThat(colors).isEmpty();
        } else {
            assertThat(colors).containsEntry("a", palette.get(0)).containsEntry("b", palette.get(1 % size)).containsEntry("c", palette.get(2 % size));
        }
    }
}
