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
import org.polyfrost.polyui.component.Component
import org.polyfrost.polyui.component.ContainingComponent
import org.polyfrost.polyui.event.EventDSL
import org.polyfrost.polyui.input.PolyText
import org.polyfrost.polyui.property.impl.ButtonProperties
import org.polyfrost.polyui.renderer.data.PolyImage
import org.polyfrost.polyui.unit.*
import org.polyfrost.polyui.unit.Unit
import org.polyfrost.polyui.utils.truncate
import kotlin.math.max

/**
 * A button, that supports two images and a text object.
 * @param properties The properties of the button (inherits from [Block]). There are various padding properties here you may want to tweak.
 * @param size You can specify this if you want to have more control over the height of this component, as if it is `null` it is controlled by the largest item.
 * @param left The image to be displayed on the left side of the button
 * @param text The text to be displayed in the button
 * @param fontSize The font size of the text
 *
 * @since 0.17.3
 */
@Suppress("UNCHECKED_CAST")
open class Button(
    properties: ButtonProperties? = null,
    at: Vec2<Unit>,
    size: Size<Unit>? = null,
    left: PolyImage? = null,
    text: PolyText? = null,
    fontSize: Unit.Pixel = 12.px,
    right: PolyImage? = null,
    acceptsInput: Boolean = true,
    events: EventDSL<Button>.() -> kotlin.Unit = {},
) : ContainingComponent(properties, at, null, false, acceptsInput, arrayOf(), events as EventDSL<ContainingComponent>.() -> kotlin.Unit) {
    protected val fixedSize = size
    override val properties get() = super.properties as ButtonProperties

    @Transient val leftImage: Image? = if (left != null) Image(image = left, at = origin, acceptInput = false) else null

    @Transient val rightImage: Image? = if (right != null) Image(image = right, at = origin, acceptInput = false) else null

    @Transient val text: Text? =
        if (text != null) Text(initialText = text, fontSize = fontSize, at = origin, acceptInput = false) else null
    var string
        get() = text?.string
        set(value) {
            if (value == null || text == null) return
            text.string = value.truncate(renderer, text.font, text.fontSize, this.width - properties.lateralPadding * 4f - (leftImage?.width ?: 0f) - (rightImage?.width ?: 0f))
            calculateBounds()
        }

    init {
        if (text == null) {
            if (left != null && right != null) {
                throw IllegalArgumentException("${this.simpleName} cannot have both left and right icons and no text!")
            } else if (left == null && right == null) throw IllegalArgumentException("${this.simpleName} cannot have no text or icons!")
        }
        addComponents(leftImage, rightImage, this.text)
    }

    override fun recolorRecolorsAll() = properties.recolorsAll

    override fun render() {
        if (properties.hasBackground) {
            if (properties.outlineThickness != 0f) {
                renderer.hollowRect(0f, 0f, width, height, color, properties.outlineThickness, properties.cornerRadii)
            }
            renderer.rect(0f, 0f, width, height, color, properties.cornerRadii)
        }
        super.render()
    }

    override fun calculateBounds() {
        if (fixedSize?.dynamic == true) doDynamicSize(fixedSize)
        if (leftImage?.size == null) leftImage?.size = leftImage?.calculateSize()
        if (rightImage?.size == null) rightImage?.size = rightImage?.calculateSize()
        if (text?.size == null) text?.size = text?.calculateSize()

        // asm: short-path: only an image, so just center it and dip
        if (leftImage != null && text == null) {
            if (fixedSize == null) {
                leftImage.x = properties.lateralPadding
                leftImage.y = properties.lateralPadding
                if (size == null) {
                    size = leftImage.size!!.clone()
                    width += properties.lateralPadding * 2f
                    height += properties.verticalPadding * 2f
                }
            } else {
                leftImage.x = fixedSize.width / 2f - leftImage.width / 2f
                leftImage.y = fixedSize.height / 2f - leftImage.height / 2f
                size = fixedSize
            }
        }

        leftImage?.x = properties.lateralPadding
        if (fixedSize != null) {
            size = fixedSize
            imageFixedSize(leftImage)
            imageFixedSize(rightImage)
            rightImage?.x = width - properties.lateralPadding - rightImage!!.width
            if (properties.center) {
                text?.x = width / 2f - text!!.width / 2f
            } else {
                text?.x = (if (leftImage != null) leftImage.x + leftImage.width else 0f) + properties.iconTextSpacing
            }
            text?.y = fixedSize.height / 2f - text!!.height / 2f
        } else {
            var height = getTallestComponent(text, leftImage, rightImage)
            if (text != null && text.height != height) {
                text.height = height
                text.str.fontSize = height
            }
            var width = properties.lateralPadding
            if (leftImage != null) {
                leftImage.y = if (properties.center) height / 2f - leftImage.height / 2f else properties.verticalPadding
                leftImage.x = width
                width += leftImage.width + properties.iconTextSpacing
            }
            text?.x = width
            width += text?.width ?: 0f
            if (rightImage != null) {
                rightImage.y = if (properties.center) height / 2f - rightImage.height / 2f else properties.verticalPadding
                rightImage.x = width + properties.iconTextSpacing
                width += rightImage.width
            }
            width += properties.lateralPadding
            height += properties.verticalPadding * 2f
            text?.y = height / 2f - text!!.height / 2f
            if (size == null) size = width.px * height.px
        }
        super.calculateBounds()
    }

    protected fun getTallestComponent(vararg cs: Component?): Float {
        var largest = 0f
        cs.forEach { if (it == null) return@forEach else largest = max(largest, it.height) }
        return largest
    }

    protected fun imageFixedSize(img: Image?) {
        if (img == null) return
        if (img.fixedSize) {
            img.y = (fixedSize!!.height - img.height) / 2f
        } else {
            PolyUI.LOGGER.warn("Image in ${this.simpleName} is not fixed size inside fixed-size button, and will be scaled to fit the button.")
            val ratio = img.height / fixedSize!!.height - properties.verticalPadding * 2f
            img.rescale(ratio, ratio)
            img.y = properties.verticalPadding
        }
    }
}
