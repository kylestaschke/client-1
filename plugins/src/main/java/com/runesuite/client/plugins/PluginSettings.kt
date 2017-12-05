package com.runesuite.client.plugins

/**
 * Subclasses should only add immutable properties.
 */
open class PluginSettings {

    var enabled = false
        internal set
}