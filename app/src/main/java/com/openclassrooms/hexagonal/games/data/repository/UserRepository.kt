package com.openclassrooms.hexagonal.games.data.repository

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

}