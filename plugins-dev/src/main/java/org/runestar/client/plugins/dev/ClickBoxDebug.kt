package org.runestar.client.plugins.dev

import org.runestar.client.api.util.DisposablePlugin
import org.runestar.client.game.api.Model
import org.runestar.client.game.api.SceneElement
import org.runestar.client.game.api.SceneElementKind
import org.runestar.client.game.api.live.Game
import org.runestar.client.game.api.live.Canvas
import org.runestar.client.game.api.live.Scene
import org.runestar.client.game.api.live.Viewport
import org.runestar.client.game.api.live.SceneElements
import org.runestar.client.game.api.live.VisibilityMap
import org.runestar.client.plugins.spi.PluginSettings
import java.awt.Color
import java.awt.Graphics2D

class ClickBoxDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    private val objs: MutableSet<SceneElement> = LinkedHashSet()

    override fun onStart() {
        add(SceneElements.cleared.subscribe { objs.clear() })
        add(SceneElements.removed.subscribe { objs.remove(it) })
        add(SceneElements.added.filter { it.tag.interactable }.subscribe { objs.add(it) })
        Scene.reload()

        add(Canvas.repaints.subscribe { g ->
            objs.forEach {
                g.color = colorFor(it)
                if (it.isLoc) {
                    it.models.forEach { drawObject(g, it) }
                } else {
                    it.models.forEach { drawOther(g, it) }
                }
            }
        })
    }

    override fun onStop() {
        objs.clear()
    }

    private fun colorFor(obj: SceneElement): Color {
        return when (obj.tag.kind) {
            SceneElementKind.OBJ -> Color.RED
            SceneElementKind.NPC -> Color.YELLOW
            SceneElementKind.PLAYER -> Color.WHITE
            SceneElementKind.LOC -> when (obj) {
                is SceneElement.Wall -> Color.MAGENTA
                is SceneElement.Scenery -> Color.BLUE
                is SceneElement.FloorDecoration -> Color.CYAN
                is SceneElement.WallDecoration -> Color.ORANGE
                else -> throw IllegalStateException()
            }
            else -> error(obj)
        }
    }

    private fun shouldDraw(model: Model): Boolean {
        val pos = model.base
        val tile = pos.sceneTile
        if (tile.plane != Game.plane) return false
        if (!tile.isLoaded || !VisibilityMap.isVisible(tile)) return false
        val pt = pos.toScreen() ?: return false
        if (pt !in Viewport) return false
        return true
    }

    private fun drawObject(g: Graphics2D, model: Model) {
        if (shouldDraw(model)) g.draw(model.objectClickBox())
    }

    private fun drawOther(g: Graphics2D, model: Model) {
        if (shouldDraw(model)) g.draw(model.boundingBox())
    }
}