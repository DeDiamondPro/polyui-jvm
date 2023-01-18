package cc.polyfrost.polyui.components

import cc.polyfrost.polyui.layouts.Layout
import cc.polyfrost.polyui.renderer.Renderer
import cc.polyfrost.polyui.units.Box
import cc.polyfrost.polyui.units.Unit
import org.jetbrains.annotations.ApiStatus


/** The most basic component in the PolyUI system. <br>
 * This class is implemented for both [cc.polyfrost.polyui.layouts.Layout] and [Component]. <br>
 */
@ApiStatus.Internal
interface Drawable {
    val box: Box<Unit>
    val boundingBox: Box<Unit>

    /** pre-render functions, such as applying transforms. */
    fun preRender(renderer: Renderer)

    /** draw script for this drawable. */
    fun render(renderer: Renderer)

    /** post-render functions, such as removing transforms. */
    fun postRender(renderer: Renderer)

    fun calculateBounds(layout: Layout)

    /** boolean that should return true if this drawable needs redrawing (e.g. has an animation and needs updating) */
    fun needsRedraw(): Boolean

    /** boolean that should return true if this drawable needs its bounds and size recalculating. */
    fun needsRecalculation(): Boolean

    fun isInside(x: Float, y: Float): Boolean {
        return box.isInside(x, y)
    }
}