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

package org.polyfrost.polyui.utils

/**
 * A simple class for timing of animations and things.
 * Literally a delta function.
 *
 * PolyUI internally uses nanoseconds as the time unit. There are a few reasons for this:
 * - animations will be smoother as frames are usually shorter than 1ms, around 0.007ms. This means that the delta will be a decimal, which is not possible with longs, so some accuracy is lost with milliseconds.
 * - [System.nanoTime] (the function we use to measure time) ignores changes to the system clock, so if the user changes their clock PolyUI won't crash or go very crazy because of a negative time.
 * - 9,223,372,036,854,775,808 is max for a long, so the program can run for 2,562,047.78802 hours, or 106,751.991167 days, or 292.471 years. So we can use these.
 *
 * @see FixedTimeExecutor
 * @see org.polyfrost.polyui.PolyUI.every
 */
class Clock {
    @PublishedApi
    internal var lastTime: Long = System.nanoTime()

    inline val now get() = lastTime
    val delta: Long
        get() {
            val currentTime = System.nanoTime()
            val delta = currentTime - lastTime
            lastTime = currentTime
            return delta
        }

    inline fun peek(): Long = System.nanoTime() - lastTime

    /**
     * Create a new Executor.
     * @param executeEveryNanos how often (in nanoseconds) to execute the given [function][func].
     * @param func the function to execute after the given time.
     * @see FixedTimeExecutor
     * @see ConditionalExecutor
     * @see UntilExecutor
     * @since 0.14.0
     */
    abstract class Executor(val executeEveryNanos: Long, protected val func: () -> Unit) {
        protected var time = 0L

        /**
         * Returns true if this Executor has finished, meaning it can be safely removed.
         */
        open val finished get() = false

        /**
         * Update this Executor, and execute it if the given amount of [time][deltaTimeNanos] has passed.
         * @return the same as [finished]
         */
        open fun tick(deltaTimeNanos: Long): Boolean {
            if (finished) return true
            time += deltaTimeNanos
            if (time >= executeEveryNanos) {
                func()
                time = 0L
            }
            return false
        }
    }

    /**
     * Create a new FixedTimeExecutor.
     * @param repeats how many times to repeat before stopping. If it is 0 (default) then it will last forever (see [finished])
     * @since 0.18.1
     */
    class FixedTimeExecutor @JvmOverloads constructor(executeEveryNanos: Long, val repeats: Int = 0, func: () -> Unit) : Executor(executeEveryNanos, func) {
        /** amount of times this has executed. Only incremented when [repeats] is not 0 (i.e. it does not last forever) to prevent numerical overflow and keep method short. */
        var cycles: Int = 0
            private set

        /** returns true if this FixedTimeExecutor has finished (it has cycled more than [repeats] times)
         *
         * Returns false if [repeats] is 0 (it lasts forever) */
        override val finished get() = cycles > repeats

        override fun tick(deltaTimeNanos: Long): Boolean {
            if (cycles > repeats) return true
            time += deltaTimeNanos
            if (time >= executeEveryNanos) {
                func()
                if (repeats != 0) cycles++
                time = 0L
            }
            return false
        }
    }

    /**
     * An executor that will do its function until the given amount of time has passed.
     *
     * @since 0.18.1
     */
    class UntilExecutor(executeEveryNanos: Long, val executeForNanos: Long, func: () -> Unit) : Executor(executeEveryNanos, func) {
        private var total = 0L
        override val finished get() = total >= executeForNanos

        override fun tick(deltaTimeNanos: Long): Boolean {
            total += deltaTimeNanos
            return super.tick(deltaTimeNanos)
        }
    }

    /**
     * An executor that will do its function until the given condition is true.
     *
     * @since 0.18.1
     */
    class ConditionalExecutor(executeEveryNanos: Long, private val condition: () -> Boolean, func: () -> Unit) : Executor(executeEveryNanos, func) {
        override val finished get() = condition()
    }

    /**
     * An executor that will do its function after the given amount of time has passed.
     * @since 0.18.3
     */
    class AfterExecutor(timeNanos: Long, func: () -> Unit) : Executor(timeNanos, func) {
        override val finished get() = time >= executeEveryNanos

        override fun tick(deltaTimeNanos: Long): Boolean {
            time += deltaTimeNanos
            return if (time >= executeEveryNanos) {
                func()
                true
            } else {
                false
            }
        }
    }
}
