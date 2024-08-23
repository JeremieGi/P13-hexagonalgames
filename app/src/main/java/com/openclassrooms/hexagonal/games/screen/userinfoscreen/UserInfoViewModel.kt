package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userRepository : UserRepository
) : ViewModel() {


    /**
     * Return current user
     */
    fun getCurrentUser() : FirebaseUser? {
        return userRepository.getCurrentUser()
    }

    fun signOut(context : Context) : Task<Void> {
        return userRepository.signOut(context)
    }

    fun deleteUser(context : Context) : Flow<ResultCustom<String>> {
        return userRepository.deleteUser(context)
    }
}