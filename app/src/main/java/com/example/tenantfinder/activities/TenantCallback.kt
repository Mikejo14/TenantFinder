package com.example.tenantfinder.activities

import com.google.firebase.database.DatabaseReference

interface TenantCallback {

    fun onSignout()
    fun onGetUserId(): String
    fun getUserDatabase(): DatabaseReference
    fun getChatDatabase(): DatabaseReference
    fun profileComplete()
    fun startActivityForPhoto()
}