package com.ptpws.ikikasir.screens.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Extension functions for safe, spam-proof navigation across the app.
 */

/**
 * Pops the back stack safely:
 * 1. Checks if current destination matches [expectedRoute] (if provided).
 *    This prevents spammed clicks on a screen's back button from popping multiple screens.
 * 2. Never pops the root destination (Dashboard), preventing blank screens.
 * 3. Checks if there is a previous back stack entry before popping.
 */
fun NavController.popBackStackSafely(expectedRoute: String? = null): Boolean {
    val current = currentDestination?.route?.substringBefore("?")
    if (expectedRoute != null && current != expectedRoute.substringBefore("?")) {
        return false
    }
    val startRoute = runCatching { graph.findStartDestination().route?.substringBefore("?") }.getOrNull()
        ?: AppScreen.Dashboard.route
    if (current == startRoute) {
        return false
    }
    if (previousBackStackEntry == null) {
        return false
    }
    return popBackStack()
}

/**
 * Navigates safely from a specific route to a target route, preventing duplicate
 * navigations if an action button is spammed.
 */
fun NavController.navigateSafely(
    fromRoute: String? = null,
    targetRoute: String,
    builder: (androidx.navigation.NavOptionsBuilder.() -> Unit)? = null
) {
    val current = currentDestination?.route?.substringBefore("?")
    if (fromRoute != null && current != fromRoute.substringBefore("?")) {
        return
    }
    if (current == targetRoute.substringBefore("?")) {
        return
    }
    if (builder != null) {
        navigate(targetRoute, builder)
    } else {
        navigate(targetRoute) {
            launchSingleTop = true
        }
    }
}
