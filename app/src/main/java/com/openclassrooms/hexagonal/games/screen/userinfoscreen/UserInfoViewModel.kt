package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserInfoViewModel : ViewModel() {


    private val _user = FirebaseAuth.getInstance().currentUser

    val user : FirebaseUser?
        get() = _user


}