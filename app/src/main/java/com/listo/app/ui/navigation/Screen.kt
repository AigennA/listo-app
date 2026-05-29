package com.listo.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash      : Screen("splash")
    data object Onboarding  : Screen("onboarding")
    data object Home        : Screen("home")
    data object CreateTask  : Screen("create_task?taskId={taskId}") {
        fun route(taskId: Long? = null) = if (taskId != null) "create_task?taskId=$taskId" else "create_task"
    }
    data object TaskDetail  : Screen("task_detail/{taskId}") {
        fun route(taskId: Long) = "task_detail/$taskId"
    }
    data object Categories  : Screen("categories")
    data object Statistics  : Screen("statistics")
    data object Settings    : Screen("settings")
    data object Premium     : Screen("premium")
}
