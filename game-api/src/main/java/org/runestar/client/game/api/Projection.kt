package org.runestar.client.game.api

import java.awt.Point

interface Projection {

    fun toGame(x: Int, y: Int): Position?

    fun toScreen(localX: Int, localY: Int, height: Int, plane: Int, tileHeightLocalX: Int, tileHeightLocalY: Int): Point?

    fun toGame(point: Point): Position? {
        return toGame(point.x, point.y)
    }

    fun toScreen(position: Position): Point? {
        return toScreen(position, position)
    }

    fun toScreen(position: Position, tileHeight: Position): Point? {
        return toScreen(position.localX, position.localY, position.height, position.plane, tileHeight.localX, tileHeight.localY)
    }

    fun toScreen(localX: Int, localY: Int, height: Int, plane: Int): Point? {
        return toScreen(localX, localY, height, plane, localX, localY)
    }

    data class Minimap(
            val minimap: org.runestar.client.game.api.Minimap
    ) : Projection {

        override fun toScreen(localX: Int, localY: Int, height: Int, plane: Int, tileHeightLocalX: Int, tileHeightLocalY: Int): Point {
            val minimapReference = minimap.reference
            val dx = (localX - minimapReference.localX) shr 5
            val dy = (localY - minimapReference.localY) shr 5
            val minimapZoom = minimap.zoom + 256
            val minimapOrientation = minimap.orientation
            val sin = minimapOrientation.sinInternal * 256 / minimapZoom
            val cos = minimapOrientation.cosInternal * 256 / minimapZoom
            val x2 = (dy * sin + dx * cos) shr 16
            val y2 = (dy * cos - dx * sin) shr 16
            val minimapCenter = minimap.center
            return Point(minimapCenter.x + x2, minimapCenter.y - y2)
        }

        override fun toGame(x: Int, y: Int): Position {
            val minimapCenter = minimap.center
            val x2 = minimapCenter.x - x
            val y2 = minimapCenter.y - y
            val minimapOrientation = minimap.orientation
            val minimapZoom = minimap.zoom + 256
            val sin = minimapOrientation.sinInternal * minimapZoom shr 8
            val cos = minimapOrientation.cosInternal * minimapZoom shr 8
            val dx = (y2 * sin + x2 * cos) shr 11
            val dy = (y2 * cos - x2 * sin) shr 11
            return minimap.reference.plusLocal(dx * -1, dy).copy(height = 0)
        }
    }

    data class Viewport(
            val camera: Camera,
            val viewport: org.runestar.client.game.api.Viewport,
            val scene: Scene
    ) : Projection {

        override fun toScreen(localX: Int, localY: Int, height: Int, plane: Int, tileHeightLocalX: Int, tileHeightLocalY: Int): Point? {
            var x1 = localX
            var y1 = localY
            var z1 = scene.getTileHeight(tileHeightLocalX, tileHeightLocalY, plane) - height
            val camLocalX = camera.localX
            val camLocalY = camera.localY
            x1 -= camLocalX
            y1 -= camLocalY
            z1 -= scene.getTileHeight(camLocalX, camLocalY, plane) - camera.height
            val cameraPitch = camera.pitch
            val sinY = cameraPitch.sinInternal
            val cosY = cameraPitch.cosInternal
            val cameraYaw = camera.yaw
            val sinX = cameraYaw.sinInternal
            val cosX = cameraYaw.cosInternal
            val x2 = (y1 * sinX + x1 * cosX) shr 16
            y1 = (y1 * cosX - sinX * x1) shr 16
            x1 = x2
            val z2 = (cosY * z1 - y1 * sinY) shr 16
            y1 = (z1 * sinY + y1 * cosY) shr 16
            z1 = z2
            if (y1 < 50) {
                return null
            }
            val viewportZoom = viewport.zoom
            return Point(
                    viewport.width / 2 + x1 * viewportZoom / y1 + viewport.x,
                    viewport.height / 2 + z1 * viewportZoom / y1 + viewport.y
            )
        }

        override fun toGame(x: Int, y: Int): Position? {
            // todo
            val plane = camera.plane
            for (xi in 0 until Scene.SIZE) {
                for (yi in 0 until Scene.SIZE) {
                    val tile = SceneTile(xi, yi, plane)
                    val bounds = tile.outline(this)
                    if (bounds.contains(x, y)) {
                        return tile.center
                    }
                }
            }
            return null
        }
    }
}