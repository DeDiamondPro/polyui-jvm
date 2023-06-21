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

package cc.polyfrost.polyui.component.impl

import cc.polyfrost.polyui.PolyUI
import cc.polyfrost.polyui.component.Component
import cc.polyfrost.polyui.event.Events
import cc.polyfrost.polyui.input.PolyText
import cc.polyfrost.polyui.input.PolyTranslator.Companion.localised
import cc.polyfrost.polyui.property.Properties
import cc.polyfrost.polyui.property.impl.TextProperties
import cc.polyfrost.polyui.renderer.Renderer
import cc.polyfrost.polyui.renderer.data.Line
import cc.polyfrost.polyui.renderer.data.MultilineText
import cc.polyfrost.polyui.renderer.data.SingleText
import cc.polyfrost.polyui.renderer.data.Text
import cc.polyfrost.polyui.unit.*
import cc.polyfrost.polyui.unit.Unit
import cc.polyfrost.polyui.utils.cl1
import kotlin.math.floor

open class Text @JvmOverloads constructor(
    properties: Properties? = null,
    private val txt: PolyText,
    at: Vec2<Unit>,
    val sized: Size<Unit>? = null,
    fontSize: Unit? = null,
    val textAlign: TextAlign = TextAlign.Left,
    rawResize: Boolean = false,
    acceptInput: Boolean = false,
    vararg events: Events.Handler
) : Component(properties, at, null, rawResize, acceptInput, *events) {
    /** Internally [txt] is stored as a [PolyText] object, which supports localization and object substitution */
    @JvmOverloads
    constructor(
        txt: String,
        at: Vec2<Unit>,
        size: Size<Unit>? = null,
        fontSize: Unit? = null,
        textAlign: TextAlign = TextAlign.Left,
        rawResize: Boolean = false,
        acceptInput: Boolean = false,
        vararg events: Events.Handler
    ) : this(null, txt.localised(), at, size, fontSize, textAlign, rawResize, acceptInput, *events)

    constructor(properties: Properties? = null, text: PolyText, fontSize: Unit, at: Vec2<Unit>) :
        this(properties, text, at, null, fontSize)

    final override val properties: TextProperties
        get() = super.properties as TextProperties
    private val fs = fontSize ?: this.properties.fontSize
    internal lateinit var str: Text
    val fontSize get() = str.fontSize
    val lines get() = str.lines
    val full get() = str.full
    val font get() = this.properties.font

    override var size: Size<Unit>?
        get() = if (::str.isInitialized) str.size else null
        set(value) {
            if (value != null) {
                str.size.a.px = value.a.px
                str.size.b.px = value.b.px
            }
        }
    var text
        get() = str.text
        set(value) {
            str.text = value
            str.calculate(renderer)
            if (autoSized) size = str.size
        }

    override fun render() {
        @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
        if (text.string.length == 0) return
        if (str.textOffsetY != 0f || str.textOffsetX != 0f) renderer.pushScissor(x, y, width, height)
        str.render(x, y, color)
        if (str.textOffsetY != 0f || str.textOffsetX != 0f) renderer.popScissor()
    }

    operator fun get(index: Int) = str[index]

    override fun reset() = str.text.reset()

    /**
     * @return the [Line] that encapsulates this character, the index of the character relative to the start of the line, and the index of this line
     * @since 0.18.5
     */
    fun getLineByIndex(index: Int) = str.getLineByIndex(index)

    override fun rescale(scaleX: Float, scaleY: Float) {
        super.rescale(scaleX, scaleY)
        if (rawResize) {
            this.scaleX = str.rescale(scaleX, scaleY)
        } else {
            val scale = cl1(scaleX, scaleY)
            this.scaleX = str.rescale(scale, scale)
        }
    }

    override fun setup(renderer: Renderer, polyui: PolyUI) {
        super.setup(renderer, polyui)
        if (fs is Unit.Dynamic) fs.set(sized?.b ?: throw IllegalArgumentException("${this.simpleName} has a dynamic font size, but it has no height"))
        str = if (floor((sized?.height ?: 0f) / this.fs.px).toInt() > 1) {
            MultilineText(txt, this.properties.font, this.fs.px, textAlign, sized ?: origin)
        } else {
            SingleText(txt, this.properties.font, this.fs.px, textAlign, sized ?: origin)
        }
        str.renderer = renderer
        str.text.polyTranslator = polyui.translator

        if (layout.size != null) doDynamicSize(str.size)
        str.calculate(renderer)
        size = str.size
    }

    override fun calculateBounds() {
        doDynamicSize(str.size)
        calculateSize()
    }

    override fun calculateSize(): Vec2<Unit>? {
        doDynamicSize(str.size)
        return size
    }
}
