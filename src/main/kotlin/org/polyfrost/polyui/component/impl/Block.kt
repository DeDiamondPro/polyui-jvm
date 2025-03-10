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

package org.polyfrost.polyui.component.impl

import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.color.Colors
import org.polyfrost.polyui.component.Component
import org.polyfrost.polyui.event.EventDSL
import org.polyfrost.polyui.property.impl.BlockProperties
import org.polyfrost.polyui.renderer.Renderer
import org.polyfrost.polyui.unit.Size
import org.polyfrost.polyui.unit.Unit
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.color.PolyColor as Color

/**
 * # Block
 *
 * A simple block component, supporting the full PolyUI API.
 */
@Suppress("UNCHECKED_CAST")
open class Block @JvmOverloads constructor(
    properties: BlockProperties? = null,
    at: Vec2<Unit>,
    size: Size<Unit>?,
    rawResize: Boolean = false,
    acceptInput: Boolean = true,
    events: EventDSL<Block>.() -> kotlin.Unit = {},
) : Component(properties, at, size, rawResize, acceptInput, events as EventDSL<Component>.() -> kotlin.Unit) {
    override val properties
        get() = super.properties as BlockProperties

    @Transient
    protected lateinit var outlineColor: Color.Animated

    @Transient
    lateinit var cornerRadii: FloatArray

    override fun render() {
        renderer.rect(x, y, width, height, color, cornerRadii)
        if (properties.outlineThickness != 0f) {
            renderer.hollowRect(x, y, width, height, outlineColor, properties.outlineThickness, cornerRadii)
        }
    }

    override fun setup(renderer: Renderer, polyUI: PolyUI) {
        super.setup(renderer, polyUI)
        outlineColor = properties.outlineColor.toAnimatable()
        cornerRadii = properties.cornerRadii
    }

    override fun onColorsChanged(colors: Colors) {
        super.onColorsChanged(colors)
        outlineColor.recolor(properties.outlineColor)
    }
}
