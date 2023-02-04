package cc.polyfrost.polyui.units

/**
 * Class to represent a 2D vector of units
 *
 * @param T The type of unit to use
 */
data class Vec2<T : Unit>(
    val a: T,
    val b: T,
) : Cloneable {
    val x get() = a.px
    val y get() = b.px
    val width get() = a.px
    val height get() = b.px

    operator fun minus(value: Float): Vec2<T> {
        this.a.px - value
        this.b.px - value
        return this
    }

    operator fun get(index: Int): T {
        return when (index) {
            0 -> a
            1 -> b
            else -> throw IndexOutOfBoundsException()
        }
    }

    operator fun plus(value: Float): Vec2<T> {
        this.a.px + value
        this.b.px + value
        return this
    }

    fun move(x: Float, y: Float): Vec2<T> {
        this.a.px += x
        this.b.px += y
        return this
    }

    fun type(): Unit.Type {
        return a.type
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): Vec2<T> {
        return Vec2(a.clone() as T, b.clone() as T)
    }

    override fun toString(): String {
        return "Vec2.${type()}(${a.px} x ${b.px})"
    }

    fun scale(scaleX: Float, scaleY: Float) {
        this.a.px *= scaleX
        this.b.px *= scaleY
    }
}

typealias Point<T> = Vec2<T>
typealias Size<T> = Vec2<T>

