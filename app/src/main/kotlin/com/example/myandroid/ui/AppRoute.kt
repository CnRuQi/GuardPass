package com.example.myandroid.ui

sealed interface AppRoute {
    val path: String

    data object Vault : AppRoute { override val path: String = "vault" }
    data object Favorites : AppRoute { override val path: String = "favorites" }
    data object Search : AppRoute { override val path: String = "search" }
    data object Categories : AppRoute { override val path: String = "categories" }
    data object Settings : AppRoute { override val path: String = "settings" }
    data class Editor(val id: Long? = null) : AppRoute {
        override val path: String = if (id == null) NEW_EDITOR_PATH else "$EDITOR_PATH/$id"

        companion object {
            const val NEW_EDITOR_PATH = "editor/new"
            const val EDITOR_PATH = "editor"
            const val EDITOR_ARGUMENT_PATH = "editor/{entryId}"
        }
    }
}
