package com.openclassrooms.hexagonal.games.data.repository

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    /**
     * Return current user
     */
    fun getCurrentUser() : FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun isCurrentUserLogged() : Boolean {
        return getCurrentUser()!=null
    }

    fun signOut(context : Context) : Task<Void> {
        return AuthUI.getInstance().signOut(context)
    }

    fun deleteUser(context : Context) : Task<Void> {
        return AuthUI.getInstance().delete(context)
    }

}