package cc.polyfrost.polyui.components.impls

import cc.polyfrost.polyui.components.Component
import cc.polyfrost.polyui.events.ComponentEvent
import cc.polyfrost.polyui.properties.Properties
import cc.polyfrost.polyui.properties.impls.TextProperties
import cc.polyfrost.polyui.units.Size
import cc.polyfrost.polyui.units.Unit
import cc.polyfrost.polyui.units.Vec2
import cc.polyfrost.polyui.utils.px

class Text(
    properties: Properties = Properties.get("cc.polyfrost.polyui.components.impls.Text"),
    var text: String, val fontSize: Unit.Pixel = 12.px(), val wrapWidth: Unit? = null,
    at: Vec2<Unit>, size: Size<Unit>? = null,
    vararg events: ComponentEvent.Handler
) : Component(properties, at, size, *events) {
    override val properties: TextProperties = properties as TextProperties

    override fun render() {
        renderer.drawText(properties.font, x(), y(), width(), text, properties.color, fontSize.get())
    }

    override fun getSize(): Vec2<Unit> {
        if (wrapWidth == null) throw Exception("Auto-sizing for text block failed: You must specify a wrap width")
        return renderer.textBounds(properties.font, text, fontSize.get(), wrapWidth.get()) as Vec2<Unit>
    }

}