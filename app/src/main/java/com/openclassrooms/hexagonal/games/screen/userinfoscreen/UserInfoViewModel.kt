package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.data.repository.ResultCustom
import com.openclassrooms.hexagonal.games.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userRepository : UserRepository
) : ViewModel() {

    // Si je veux récupérer le résultat d'une procédure comme deleteUser, je suis obligé de déclarer une variable MutableStateFlow ? OUI
    // UI state - Résultat du delete
    private val _uiStateUserDeleteResult = MutableStateFlow<ResultCustom<String>?>(null)
    val uiStateUserDeleteResult: StateFlow<ResultCustom<String>?> = _uiStateUserDeleteResult.asStateFlow() // Accès en lecture seule de l'extérieur


    /**
     * Return current user
     */
    fun getCurrentUser() : FirebaseUser? {
        return userRepository.getCurrentUser()
    }

    fun signOut(context : Context) : Task<Void> {
        return userRepository.signOut(context)
    }

    fun deleteUser(context : Context) {

        viewModelScope.launch {
            userRepository.deleteUser(context).collect { result ->
                _uiStateUserDeleteResult.value = result
            }
        }

    }
}