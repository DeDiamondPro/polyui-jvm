/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
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

import org.polyfrost.polyui.color.Color
import org.polyfrost.polyui.color.Colors
import org.polyfrost.polyui.property.Properties
import org.polyfrost.polyui.unit.Unit
import org.polyfrost.polyui.unit.px
import org.polyfrost.polyui.utils.radii

/**
 * @param sanitizationFunction A function that will be called every time the text is changed.
 * If it returns false, the input will be highlighted in [red][org.polyfrost.polyui.color.Colors.State.danger]
 * and the [listeners][org.polyfrost.polyui.component.impl.TextInput.ChangedEvent] will not be notified.
 */
open class TextInputProperties(open val text: TextProperties = TextProperties(), @Transient open val sanitizationFunction: (String) -> Boolean = { true }) : Properties() {
    val placeholderColor: Color get() = colors.text.primary.disabled
    override val palette: Colors.Palette get() = colors.component.bg
    open val lateralPadding: Unit = 12.px
    open val verticalPadding: Unit = 8.px
    open val cornerRadii: FloatArray = 6f.radii()
    open val outlineColor: Color get() = colors.page.border20
    open val outlineThickness: Unit = 0.px
}
