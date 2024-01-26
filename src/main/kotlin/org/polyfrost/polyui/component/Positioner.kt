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

package org.polyfrost.polyui.component

import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.makeRelative
import org.polyfrost.polyui.utils.LinkedList
import kotlin.math.abs
import kotlin.math.max

/**
 * Positioning strategies are the methods used in PolyUI to place both components across the screen,
 * and components inside a layout.
 */
fun interface Positioner {
    fun position(drawable: Drawable)

    class Default : Positioner {
        override fun position(drawable: Drawable) {
            val children = drawable.children
            if (drawable.size.hasZero) {
                val out = drawable.calculateSize()
                if (out != null) drawable.size = out
            }
            val needsToCalcSize = drawable.size.hasZero
            if (needsToCalcSize) {
                require(!children.isNullOrEmpty()) { "Drawable $drawable has no size and no children" }
            } else if (children.isNullOrEmpty()) {
                fixVisibleSize(drawable)
                return
            }

            // asm: there are definitely children at this point, so we need to place them
            // we are unsure if there is a size at this point though
            val main = if (drawable.alignment.mode == Align.Mode.Horizontal) 0 else 1
            val crs = abs(main - 1)
            val padding = drawable.alignment.padding
            val polyUI = drawable.polyUI
            val totalSm = polyUI.size[main] / polyUI.iSize[main]
            val totalSc = polyUI.size[crs] / polyUI.iSize[crs]
            val mainPad = padding[main] * totalSm
            val crossPad = padding[crs] * totalSc

            if (children.size == 1) {
                // asm: fast path: set a square size with the object centered
                val it = children.first()
                it.at = it.at.makeRelative(drawable.at)
                if (it.size.hasZero) position(it)
                if (!it.at.isZero) return
                if (needsToCalcSize) {
                    drawable.size.x = it.visibleSize.x + mainPad * 2f
                    drawable.size.y = it.visibleSize.y + crossPad * 2f
                }
                fixVisibleSize(drawable)
                it.at[main] = when (drawable.alignment.main) {
                    Align.Main.Start -> mainPad
                    Align.Main.End -> drawable.visibleSize[main] - it.visibleSize[main] - mainPad
                    else -> (drawable.visibleSize[main] - it.visibleSize[main]) / 2f
                }
                it.at[crs] = when (drawable.alignment.cross) {
                    Align.Cross.Start -> crossPad
                    Align.Cross.End -> drawable.size[crs] - it.visibleSize[crs] - crossPad
                    else -> (drawable.visibleSize[crs] - it.visibleSize[crs]) / 2f
                }
                return
            }
            val willWrap = drawable.visibleSize[main] != 0f
            if (willWrap) {
                val rows = LinkedList<Pair<Pair<Float, Float>, LinkedList<Drawable>>>()
                val maxRowSize = drawable.alignment.maxRowSize
                require(maxRowSize > 0) { "Drawable $drawable has max row size of $maxRowSize, needs to be greater than 0" }
                var maxMain = 0f
                var maxCross = crossPad
                var rowMain = mainPad
                var rowCross = 0f
                var currentRow = LinkedList<Drawable>()
                children.fastEach {
                    it.at = it.at.makeRelative(drawable.at)
                    if (it.at.isNegative || !it.renders) return@fastEach
                    if (it.size.hasZero) position(it)
                    if (currentRow.isNotEmpty() && (rowMain + it.visibleSize[main] + mainPad > drawable.visibleSize[main] || currentRow.size == maxRowSize)) {
                        rows.add((rowMain to rowCross) to currentRow)
                        currentRow = LinkedList()
                        maxMain = max(maxMain, rowMain)
                        maxCross += rowCross + crossPad
                        rowMain = mainPad
                        rowCross = 0f
                    }
                    rowMain += it.visibleSize[main] + mainPad
                    rowCross = max(rowCross, it.visibleSize[crs])
                    currentRow.add(it)
                }
                if (currentRow.isNotEmpty()) {
                    rows.add((rowMain to rowCross) to currentRow)
                    maxMain = max(maxMain, rowMain)
                    maxCross += rowCross + crossPad
                }
                if (needsToCalcSize) {
                    drawable.size[main] = maxMain
                    drawable.size[crs] = maxCross
                }
                fixVisibleSize(drawable)
                rowCross = drawable.at[crs]
                if (rows.size == 1) {
                    // asm: in this situation, the user specified a size, and as there is only 1 row, so we should
                    // make it so the actual cross limit is the size of the drawable
                    val (rowData, row) = rows[0]
                    align(drawable.alignment.cross, drawable.visibleSize[crs], row, 0f, crossPad, crs)
                    justify(drawable.alignment.main, rowData.first, row, drawable.at[main], drawable.visibleSize[main], mainPad, main)
                } else {
                    rows.fastEach { (rowData, row) ->
                        val (theRowMain, theRowCross) = rowData
                        align(drawable.alignment.cross, theRowCross, row, rowCross, crossPad, crs)
                        justify(drawable.alignment.main, theRowMain, row, drawable.at[main], drawable.visibleSize[main], mainPad, main)
                        rowCross += theRowCross + crossPad
                    }
                }
            } else {
                var rowMain = mainPad
                var rowCross = 0f
                val pad = crossPad * 2f
                children.fastEach {
                    it.at = it.at.makeRelative(drawable.at)
                    if (it.size.isNegative || !it.renders) return@fastEach
                    if (it.size.hasZero) position(it)
                    rowCross = max(rowCross, it.size[crs] + pad)
                    rowMain += it.size[main] + mainPad
                }
                if (needsToCalcSize) {
                    drawable.size[main] = rowMain
                    drawable.size[crs] = rowCross
                }
                fixVisibleSize(drawable)
                align(drawable.alignment.cross, rowCross, children, 0f, crossPad, crs)
                justify(drawable.alignment.main, rowMain, children, drawable.at[main], drawable.size[main], mainPad, main)
            }
        }

        private fun align(mode: Align.Cross, rowCross: Float, drawables: LinkedList<Drawable>, min: Float, padding: Float, crs: Int) {
            when (mode) {
                Align.Cross.Start -> {
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[crs] = min + padding
                    }
                }

                Align.Cross.Center -> {
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[crs] = min + (rowCross / 2f) - (it.visibleSize[crs] / 2f)
                    }
                }

                Align.Cross.End -> {
                    val max = min + rowCross
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[crs] = (max - it.visibleSize[crs] - padding)
                    }
                }
            }
        }

        private fun justify(mode: Align.Main, rowMain: Float, drawables: LinkedList<Drawable>, min: Float, max: Float, padding: Float, main: Int) {
            when (mode) {
                Align.Main.Start -> {
                    var current = min + padding
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[main] = current
                        current += it.visibleSize[main] + padding
                    }
                }

                Align.Main.Center -> {
                    var current = min + (max / 2f) - (rowMain / 2f) + padding
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[main] = current
                        current += it.visibleSize[main] + padding
                    }
                }

                Align.Main.End -> {
                    var current = min + max
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        current -= it.visibleSize[main] + padding
                        it.at[main] = current
                    }
                }

                Align.Main.SpaceBetween -> {
                    val gapWidth = (max - rowMain) / (drawables.size - 1)
                    var current = min + padding
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[main] = current
                        current += gapWidth + it.visibleSize[main] + padding
                    }
                }

                Align.Main.SpaceEvenly -> {
                    val gapWidth = (max - rowMain) / (drawables.size + 1)
                    var current = min + padding + gapWidth
                    drawables.fastEach {
                        if (it.at.isNegative) return@fastEach
                        it.at[main] = current
                        current += gapWidth + it.visibleSize[main] + padding
                    }
                }
            }
        }

        private fun fixVisibleSize(drawable: Drawable): Drawable {
            if (drawable.visibleSize.x > drawable.size.x) drawable.visibleSize.x = drawable.size.x
            if (drawable.visibleSize.y > drawable.size.y) drawable.visibleSize.y = drawable.size.y
            return drawable
        }
    }
}
