/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *     PolyUI is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation,
 * AND the simple request that you adequately accredit us if you use PolyUI.
 * See details here <https://github.com/Polyfrost/polyui-jvm/ACCREDITATION.md>.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.polyfrost.polyui.property.impl

import org.polyfrost.polyui.utils.radii

/**
 * Button properties.
 *
 * @since 0.17.3
 */
open class ButtonProperties : BlockProperties(withStates = true) {
    override val cornerRadii: FloatArray = 8f.radii()

    /** This is the padding from the top to the items. */
    open val verticalPadding: Float = 8f

    /** padding between the icons and the text. */
    open val iconTextSpacing: Float = 16f

    /** padding from the left/right edges. */
    open val lateralPadding: Float = 10f

    /**
     * Enable centering of the items on the button.
     * @since 0.21.4
     */
    open val center = false

    /**
     * If true, the entire button will be recolored of hover/pressed.
     * @since 0.21.4
     * @see [org.polyfrost.polyui.component.ContainingComponent.recolorRecolorsAll]
     */
    open val recolorsAll = false

    /**
     * If true, the button will have a background.
     */
    open val hasBackground: Boolean = true
}
