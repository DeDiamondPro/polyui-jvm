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

package org.polyfrost.polyui.renderer.data

import org.polyfrost.polyui.PolyUI

/**
 * Image representation in PolyUI. The image is lazily-loaded from the [resourcePath].
 * @param resourcePath the path to the [resource][org.polyfrost.polyui.utils.getResourceStream]
 * @param width the width of the image. Specify one to respect the aspect ratio of the image.
 * @param height the width of the image. Specify one to respect the aspect ratio of the image.
 * @param type the [image type][Type]. This is automatically inferred from the file extension normally, but you can manually select it.
 */
class PolyImage @JvmOverloads constructor(
    resourcePath: String,
    var width: Float = -1f, // uninitialized
    var height: Float = -1f,
    val type: Type = from(resourcePath),
) : Resource(resourcePath) {

    override fun toString(): String {
        return "$type Image(file=$resourcePath, ${width}x$height)"
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    @Suppress("ReplaceCallWithBinaryOperator")
    override fun equals(other: Any?): Boolean = resourcePath.equals(other)

    /**
     * Types for images in PolyUI.
     */
    enum class Type {
        /**
         * Raster image, such as PNG, JPEG, BMP, etc.
         */
        Raster,

        /**
         * Vector image, such as SVG.
         */
        Vector,

        /**
         * Unknown image type. Down to the rendering implementation what to do with this.
         */
        Unknown,
    }

    /**
     * Styles for [Google Material icons](https://github.com/google/material-design-icons)
     * @see getMaterialIcon
     */
    enum class MaterialStyle(val style: String) {
        OUTLINED("materialiconsoutlined"),
        ROUND("materialiconsround"),
        SHARP("materialiconssharp"),
        TWOTONE("materialiconstwotone"),
        NORMAL("materialicons"),
    }

    companion object {
        @JvmStatic
        fun from(fileName: String): Type {
            return when (fileName.substringAfterLast('.')) {
                "bmp", "png", "jpg", "jpeg", "jpe", "jif", "jfif", "jfi" -> Type.Raster
                "svg" -> Type.Vector
                else -> {
                    PolyUI.LOGGER.warn("Unknown image type for $fileName")
                    Type.Unknown
                }
            }
        }

        /**
         * Get an SVG icon from the [Google Material Icons repository](https://github.com/google/material-design-icons) on GitHub.
         *
         * Note that these icons are licensed under the [Apache 2.0 License](https://github.com/google/material-design-icons/blob/master/LICENSE).
         * @param icon the name of the icon, as a path as it is on the repo, using either dots or slashes, for example `camera.11mp` or `actions.abc`.
         * @param style the Material icon [style][MaterialStyle] to use.
         *
         * @since 0.16.0
         */
        @JvmStatic
        @JvmOverloads
        fun getMaterialIcon(
            icon: String,
            width: Float = -1f,
            height: Float = -1f,
            style: MaterialStyle = MaterialStyle.NORMAL,
        ) = PolyImage(
            "https://raw.githubusercontent.com/google/material-design-icons/master/src/" +
                "${icon.replace('.', '/')}/${style.style}/24px.svg",
            width,
            height,
            Type.Vector,
        )
    }
}
