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

@file:Suppress("UNCHECKED_CAST")

package cc.polyfrost.polyui.event

import cc.polyfrost.polyui.component.Component
import cc.polyfrost.polyui.component.Drawable
import cc.polyfrost.polyui.event.Events.*
import cc.polyfrost.polyui.input.Mouse
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

/** Events that components can receive, for example [MouseClicked], [Added], [Removed], and more. */
open class Events : Event {
    // imagine this is a rust enum okay
    /** acceptable by component and layout, when the mouse goes down on this drawable.
     * @see MouseReleased
     * @see MouseClicked
     * @see MouseEntered
     */
    data class MousePressed internal constructor(val button: Int, val x: Float, val y: Float, val mods: Short = 0) :
        Events() {
        constructor(button: Int) : this(button, 0f, 0f)

        override fun hashCode(): Int {
            var result = button + 500
            result = 31 * result + mods
            return result
        }

        override fun toString(): String =
            "MousePressed(($x, $y), ${Mouse.toStringPretty(Mouse.fromValue(button), mods)})"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MousePressed) return false

            if (button != other.button) return false
            return mods == other.mods
        }
    }

    /** acceptable by component and layout, when the mouse is released on this component.
     *
     * Note that this event is **not dispatched** if the mouse leaves the drawable while being pressed, as it technically is not released on that drawable.
     * @see MousePressed
     * @see MouseClicked
     * @see MouseEntered
     */
    data class MouseReleased internal constructor(val button: Int, val x: Float, val y: Float, val mods: Short = 0) :
        Events() {
        constructor(button: Int) : this(button, 0f, 0f)

        override fun hashCode(): Int {
            var result = button + 5000 // avoid conflicts with MousePressed
            result = 31 * result + mods
            return result
        }

        override fun toString(): String =
            "MouseReleased(($x, $y), ${Mouse.toStringPretty(Mouse.fromValue(button), mods)})"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MouseReleased) return false

            if (button != other.button) return false
            return mods == other.mods
        }
    }

    /** acceptable by components and layouts, and is dispatched when the mouse is clicked on this component.
     *
     * Note that this event is **not dispatched** if the mouse leaves the drawable while being pressed, as it technically is not released on that drawable.
     * @see MouseReleased
     * @see MouseClicked
     * @see MouseEntered
     */
    data class MouseClicked internal constructor(val button: Int, val mouseX: Float, val mouseY: Float, val clicks: Int, val mods: Short) :
        Events() {

        @JvmOverloads
        constructor(button: Int, amountClicks: Int = 1, mods: Short = 0) : this(button, 0f, 0f, amountClicks, mods)

        override fun toString(): String = "MouseClicked($mouseX x $mouseY, ${Mouse.toStringPretty(Mouse.fromValue(button), mods)})"
        override fun hashCode(): Int {
            var result = button
            result = 31 * result + clicks
            result = 31 * result + mods
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MouseClicked) return false

            if (button != other.button) return false
            if (clicks != other.clicks) return false
            return mods == other.mods
        }
    }

    /** acceptable by component and layout, when the mouse enters this drawable.
     * @see MouseExited */
    data object MouseEntered : Events()

    /** acceptable by component and layout, when the mouse leaves this drawable.
     * @see MouseEntered */
    data object MouseExited : Events()

    /**
     * acceptable by component and layout, when the mouse is moved on this drawable.
     *
     * This does not have any data attached from it so that it is not instanced every time the mouse moves. It can be accessed with [polyUI.mouseX][cc.polyfrost.polyui.PolyUI.mouseX] and [polyUI.mouseY][cc.polyfrost.polyui.PolyUI.mouseY].
     */
    data object MouseMoved : Events()

    /** Dispatched when the mouse is scrolled on this component/layout.
     *
     * acceptable by component and layout */
    data class MouseScrolled internal constructor(val amountX: Int, val amountY: Int, val mods: Short = 0) : Events() {
        constructor() : this(0, 0)

        override fun hashCode() = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MouseScrolled) return false

            if (amountX != other.amountX) return false
            if (amountY != other.amountY) return false
            return mods == other.mods
        }
    }

    /**
     * This event is dispatched when a component/layout is added **after** it has been initialized (i.e. not in the UI creation block), using the [addComponent][cc.polyfrost.polyui.layout.Layout.addComponent] function.
     *
     * acceptable by component and layout. */
    data object Added : Events()

    /**
     * This event is dispatched when a component/layout is removed, using the [removeComponent][cc.polyfrost.polyui.layout.Layout.removeComponent] function.
     *
     * acceptable by component and layout. */
    data object Removed : Events()

    /** specify a handler for this event.
     *
     * in the given [action], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @see then
     * @return return true to consume the event/cancel it, false to pass it on to other handlers.
     * */
    @OverloadResolutionByLambdaReturnType
    infix fun to(action: (Component.() -> Boolean)): Handler {
        return Handler(this, action as Drawable.() -> Boolean)
    }

    /** specify a handler for this event.
     *
     * in the given [action], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @see then
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * */
    @OverloadResolutionByLambdaReturnType
    @JvmName("To")
    infix fun to(action: (Component.() -> Unit)): Handler {
        return Handler(this, insertTrueInsn(action) as Drawable.() -> Boolean)
    }

    /** specify a handler for this event.
     *
     * in the given [action], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return return true to consume the event/cancel it, false to pass it on to other handlers.
     * @see to
     * @since 0.19.2
     * */
    @OverloadResolutionByLambdaReturnType
    infix fun then(action: (Event, Component) -> Boolean): EventHandler {
        return EventHandler(this, (action as (Event, Drawable) -> Boolean))
    }

    /** specify a handler for this event.
     *
     * in the given [action], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * @see to
     * @since 0.19.2
     * */
    @OverloadResolutionByLambdaReturnType
    @JvmName("Then")
    infix fun then(action: (Event, Component) -> Unit): Handler {
        return Handler(this, insertTrueInsnWithRef(action) as Drawable.() -> Boolean)
    }

    /**
     * Java compat version of [to]
     */
    fun to(action: Consumer<Component>): Handler = to { action.accept(this); true }

    /**
     * Java compat version of [to]
     */
    fun to(action: Function<Component, Boolean>): Handler = to { action.apply(this) }

    /**
     * Java compat version of [then]
     */
    fun then(action: BiConsumer<Event, Component>): EventHandler = then { event, component -> action.accept(event, component); true }

    /**
     * Java compat version of [then]
     */
    fun then(action: BiFunction<Event, Component, Boolean>): EventHandler = then { event, component -> action.apply(event, component) }

    companion object {
        /** wrapper for varargs, when arguments are in the wrong order */
        @JvmStatic
        @EventDSL
        fun events(vararg events: @EventDSL Handler): Array<out Handler> = events
    }

    class Handler(event: Events, handler: Drawable.() -> Boolean) : EventHandler(event, convert(handler))
    open class EventHandler(val event: Events, val handler: (Event, Drawable) -> Boolean)
}

private fun convert(func: Drawable.() -> Boolean): (Event, Drawable) -> Boolean {
    return { _, drawable -> drawable.func() }
}

private fun insertTrueInsn(action: (Component.() -> Unit)): (Component.() -> Boolean) {
    return {
        action(this)
        true
    }
}

private fun insertTrueInsnWithRef(action: (Event, Component) -> Unit): (Event, Component) -> Boolean {
    return { event, drawable ->
        action(event, drawable)
        true
    }
}

/** marker class for preventing illegal nesting. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@DslMarker
annotation class EventDSL
