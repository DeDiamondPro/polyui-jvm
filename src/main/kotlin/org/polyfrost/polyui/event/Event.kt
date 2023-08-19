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

package org.polyfrost.polyui.event

import org.polyfrost.polyui.component.Component
import org.polyfrost.polyui.component.Drawable

/**
 * An event in the PolyUI system.
 * @see EventManager
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface Event {

    companion object {
        @JvmStatic
        fun <S : Drawable, E : Event> insertTrueInsn(action: (S.(E) -> Unit)): (S.(E) -> Boolean) {
            return {
                action(this, it)
                true
            }
        }
    }
}

/** marker class for preventing illegal nesting. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@DslMarker
annotation class EventDSLMarker

class EventDSL<S : Drawable>(val self: Drawable) {

    /** specify a handler for this event.
     *
     * in the given [handler], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * @see then
     * @since 0.19.2
     * */
    @Suppress("UNCHECKED_CAST")
    @OverloadResolutionByLambdaReturnType
    infix fun <E : Event> E.to(handler: S.(E) -> Boolean) {
        self.eventHandlers[this] = handler as Drawable.(Event) -> Boolean
    }

    /** specify a handler for this event.
     *
     * in the given [handler], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * @see then
     * @since 0.19.2
     * */
    @JvmName("To")
    @Suppress("UNCHECKED_CAST")
    @OverloadResolutionByLambdaReturnType
    infix fun <E : Event> E.to(handler: S.(E) -> Unit) {
        self.eventHandlers[this] = Event.insertTrueInsn(handler) as Drawable.(Event) -> Boolean
    }

    /** specify a handler for this event.
     *
     * in the given [handler], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * @see to
     * @since 0.19.2
     * */
    @Suppress("UNCHECKED_CAST")
    @OverloadResolutionByLambdaReturnType
    infix fun <E : Event> E.then(handler: S.(E) -> Boolean) {
        self.eventHandlers[this] = handler as Drawable.(Event) -> Boolean
    }

    /** specify a handler for this event.
     *
     * in the given [handler], you can perform things on this component, such as [Component.rotateBy], [Component.recolor], etc.
     *
     * @return returns a [Handler] for the event, which will return true when called, meaning it will **consume** the event. Return false to not consume this event.
     * @see to
     * @since 0.19.2
     * */
    @JvmName("Then")
    @Suppress("UNCHECKED_CAST")
    @OverloadResolutionByLambdaReturnType
    infix fun <E : Event> E.then(handler: S.(E) -> Unit) {
        self.eventHandlers[this] = Event.insertTrueInsn(handler) as Drawable.(Event) -> Boolean
    }
}
