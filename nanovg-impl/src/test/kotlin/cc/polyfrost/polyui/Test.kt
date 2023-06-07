/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors. All rights reserved.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package cc.polyfrost.polyui

import cc.polyfrost.polyui.animate.Animations
import cc.polyfrost.polyui.animate.keyframes.keyframed
import cc.polyfrost.polyui.color.Color
import cc.polyfrost.polyui.color.DarkTheme
import cc.polyfrost.polyui.color.LightTheme
import cc.polyfrost.polyui.component.Drawable
import cc.polyfrost.polyui.component.impl.Block
import cc.polyfrost.polyui.component.impl.Button
import cc.polyfrost.polyui.component.impl.Image
import cc.polyfrost.polyui.component.impl.Text
import cc.polyfrost.polyui.event.Events
import cc.polyfrost.polyui.event.Events.Companion.events
import cc.polyfrost.polyui.input.Keys
import cc.polyfrost.polyui.input.Modifiers
import cc.polyfrost.polyui.input.Mouse
import cc.polyfrost.polyui.input.PolyTranslator.Companion.localised
import cc.polyfrost.polyui.layout.Layout.Companion.items
import cc.polyfrost.polyui.layout.impl.FlexLayout
import cc.polyfrost.polyui.layout.impl.PixelLayout
import cc.polyfrost.polyui.property.Properties
import cc.polyfrost.polyui.property.impl.BlockProperties
import cc.polyfrost.polyui.property.impl.ButtonProperties
import cc.polyfrost.polyui.renderer.data.PolyImage
import cc.polyfrost.polyui.renderer.impl.GLWindow
import cc.polyfrost.polyui.renderer.impl.NVGRenderer
import cc.polyfrost.polyui.renderer.impl.NoOpRenderer
import cc.polyfrost.polyui.renderer.impl.NoOpWindow
import cc.polyfrost.polyui.unit.*
import cc.polyfrost.polyui.utils.fastEach
import kotlin.random.Random

fun main() {
    // use the no-op rendering implementation (for profiling of the system code)
    val useNoOp = false

    val window = if (!useNoOp) GLWindow("Test", 800, 800) else NoOpWindow("Test", 800, 800)
    val things = Array<Drawable>(50) { // creates 50 rectangles with random sizes
        Block(
            properties = Properties.primaryProperties,
            at = flex(),
            size = Size((Random.Default.nextFloat() * 40f + 40f).px, (Random.Default.nextFloat() * 40f + 40f).px),
            events = events(
                Events.MouseClicked(0) to {
                    println("Mouse clicked! $it")
                    setProperties(Properties.successProperties)
                    rotateBy(120.0, Animations.EaseInOutCubic)
                },
                Events.MouseClicked(0, 2) to {
                    println("Mouse double-clicked!")
                    setProperties(Properties.warningProperties)
                },
                Events.MouseClicked(1) to {
                    println("Mouse right-clicked!")
                    setProperties(Properties.dangerProperties)
                    true
                }
            )
        )
    }
    val polyUI = PolyUI(
        renderer = if (!useNoOp) {
            NVGRenderer(
                window.width.toFloat(),
                window.height.toFloat()
            )
        } else {
            NoOpRenderer(window.width.toFloat(), window.height.toFloat())
        },
        items = items(
            Image(
                at = 20.px * 20.px,
                image = PolyImage("test.png")
            ),
            PixelLayout(
                at = 20.px * 570.px,
                items = items(
                    Text(
                        text = "polyui.test".localised("rainbow"),
                        fontSize = 32.px,
                        at = 0.px * 0.px
                    ),
                    Block(
                        properties = BlockProperties(
                            Color.Gradient(
                                Color(1f, 0f, 1f, 1f),
                                Color(0f, 1f, 1f, 1f)
                            )
                        ),
                        at = 0.px * 30.px,
                        size = 120.px * 120.px,
                        events = events(
                            Events.MouseClicked(0) to {
                                keyframed(2.seconds, Animations.EaseOutExpo) {
                                    20 {
                                        rotation = 20.0
                                        skewX = 30.0
                                    }
                                    32 {
                                        rotation = 35.0
                                    }
                                    50 {
                                        rotation = 180.0
                                        skewY = 20.0
                                        skewX = 20.0
                                    }
                                    100 {
                                        rotation = 0.0
                                        skewX = 0.0
                                        skewY = 0.0
                                    }
                                }
                            }
                        )
                    ),
                    Block(
                        properties = BlockProperties(Color.Chroma(5.seconds)),
                        at = 180.px * 30.px,
                        size = 120.px * 120.px,
                        events = events(
                            Events.MouseClicked(0) to {
                                rotateBy(120.0, Animations.EaseInOutCubic)
                            }
                        )
                    ),
                    Image(
                        PolyImage("/a.png", 120f, 120f),
                        at = 360.px * 30.px,
                        events = events(
                            Events.MouseClicked(0) to {
                                rotateBy(120.0, Animations.EaseOutBump)
                            }
                        )
                    ),
                    Button(
                        properties = ButtonProperties(),
                        at = 0.px * 160.px,
                        leftIcon = PolyImage("/ta.png", 15f, 15f),
                        text = "polyui.button".localised(),
                        rightIcon = PolyImage("/test.jpg", 15f, 15f),
                        events = events(
                            Events.MouseClicked(0) to {
                                rotateBy(120.0, Animations.EaseInOutCubic)
                            }
                        )
                    )
                )
            ).draggable(),
            FlexLayout(
                at = 20.px * 30.px,
                wrap = 80.percent,
                items = things
            ).scrolling(620.px * 300.px)
        )
    )
    polyUI.keyBinder.add(Mouse.LEFT_MOUSE, Modifiers.LCONTROL) {
        println("${polyUI.mouseX} x ${polyUI.mouseY}")
        polyUI.getComponentsIn(polyUI.mouseX - 25f, polyUI.mouseY - 25f, 50f, 50f).fastEach {
            it.recolor(Color(Random.Default.nextFloat(), Random.Default.nextFloat(), Random.Default.nextFloat(), 1f))
        }
    }
    var light = false
    polyUI.keyBinder.add(Keys.F1) {
        if (!light) {
            polyUI.colors = LightTheme()
        } else {
            polyUI.colors = DarkTheme()
        }
        light = !light
    }

    window.open(polyUI)
}
