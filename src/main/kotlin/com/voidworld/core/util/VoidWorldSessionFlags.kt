package com.voidworld.core.util

/**
 * Shared flags for VoidWorld session (creatable from both client and server).
 * Used when entering VoidWorld via the main menu button to trigger start logic.
 */
object VoidWorldSessionFlags {
    @JvmStatic
    var voidWorldSessionStarting: Boolean = false

    /** True when CreateWorldScreen is configured and we should auto-trigger createLevel on first draw. */
    @JvmStatic
    var voidWorldCreatePending: Boolean = false
}
