/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors. All rights reserved.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package cc.polyfrost.polyui.unit

/** A box with a position and size (it's just 2x [Vec2]s)*/
data class Box<T : Unit>(val point: Point<T>, val size: Size<T>) {
    var x by point[0]::px
    var y by point[1]::px
    var width by size[0]::px
    var height by size[1]::px

    /** add the given amount of pixels to each edge of this box */
    fun expand(amount: Float): Box<T> {
        this.x -= amount
        this.y -= amount
        this.width += amount
        this.height += amount
        return this
    }
}
