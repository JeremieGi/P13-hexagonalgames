package com.openclassrooms.hexagonal.games.screen.userinfoscreen

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserInfoViewModel : ViewModel() {

    // TODO Denis : ViewModel utile ici ? pour gérer des évolutions ?

    private val _user = FirebaseAuth.getInstance().currentUser

    val user : FirebaseUser?
        get() = _user


}