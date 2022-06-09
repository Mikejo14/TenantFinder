package com.example.tenantfinder.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.tenantfinder.R
import com.example.tenantfinder.activities.TenantCallback
import com.example.tenantfinder.user
import com.example.tenantfinder.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    private lateinit var userId: String
    private lateinit var userDatabase: DatabaseReference
    private var callback: TenantCallback? =null

    fun setCallback(callback: TenantCallback){
        this.callback= callback
        userId = callback.onGetUserId()
        userDatabase = callback.getUserDatabase().child(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressLayout.setOnTouchListener{view, event -> true}

        populateInfo()

        photoIV.setOnClickListener{ callback?.startActivityForPhoto()}

        applyButton.setOnClickListener { onApply() }
        signoutButton.setOnClickListener { callback?.onSignout() }

    }

    fun populateInfo() {
        progressLayout.visibility=View.VISIBLE
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (isAdded){
                    val user = p0.getValue(user::class.java)
                    nameET.setText(user?.name,TextView.BufferType.EDITABLE)
                    emailET.setText(user?.email,TextView.BufferType.EDITABLE)
                    ageET.setText(user?.age,TextView.BufferType.EDITABLE)
                    if(user?.gender == GENDER_TENANT ){
                        radioMan1.isChecked = true
                    }
                    if(user?.gender == GENDER_LANDLORD ){
                        radioLand1.isChecked = true
                    }
                    if(user?.preferred == GENDER_TENANT){
                        radioMan2.isChecked=true
                    }
                    if(user?.preferred == GENDER_LANDLORD){
                        radioLand2.isChecked=true
                    }
                    if (!user?.imageUrl.isNullOrEmpty()){
                        populateImage(user?.imageUrl!!)
                    }
                    progressLayout.visibility=View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                progressLayout.visibility=View.GONE
            }

        })
    }

    fun onApply() {
        if (nameET.text.toString().isNullOrEmpty() ||
            emailET.text.toString().isNullOrEmpty() ||
            radioGroup1.checkedRadioButtonId == -1 ||
            radioGroup2.checkedRadioButtonId == -1){
            Toast.makeText(context,getString(R.string.error_profile_incomplete),Toast.LENGTH_SHORT).show()
        }
        else{
            val name = nameET.text.toString()
            val age = ageET.text.toString()
            val email = emailET.text.toString()
            val gender =
                if (radioMan1.isChecked) GENDER_TENANT
                else GENDER_LANDLORD
            val preferred=
                if(radioMan2.isChecked) GENDER_TENANT
                else GENDER_LANDLORD

            userDatabase.child(DATA_NAME).setValue(name)
            userDatabase.child(DATA_AGE).setValue(age)
            userDatabase.child(DATA_EMAIL).setValue(email)
            userDatabase.child(DATA_GENDER).setValue(gender)
            userDatabase.child(DATA_PREFERRED).setValue(preferred)

            callback?.profileComplete()
        }
    }
    fun updateImageUri(uri: String){
        userDatabase.child(DATA_IMAGE_URL).setValue(uri)
        populateImage(uri)
    }

    fun populateImage(uri: String){
        Glide.with(this)
            .load(uri)
            .into(photoIV)
    }

}