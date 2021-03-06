package com.example.tenantfinder.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tenantfinder.R
import com.example.tenantfinder.fragments.MatchesFragment
import com.example.tenantfinder.fragments.ProfileFragment
import com.example.tenantfinder.fragments.SwipeFragment
import com.example.tenantfinder.util.DATA_CHATS
import com.example.tenantfinder.util.DATA_USERS
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOError
import java.io.IOException

const val REQUEST_CODE_PHOTO = 1234

class TenantActivity : AppCompatActivity(), TenantCallback {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private lateinit var userDatabase: DatabaseReference
    private lateinit var chatDatabase: DatabaseReference

    private var profileFragment:ProfileFragment? =null
    private var swipeFragment:SwipeFragment? =null
    private var matchesFragment:MatchesFragment? =null

    private var profileTab:TabLayout.Tab? = null
    private var swipeTab:TabLayout.Tab? = null
    private var matchesTab:TabLayout.Tab? =null

    private var resultImageUrl: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (userId.isNullOrEmpty()){
            onSignout()
        }

        userDatabase = FirebaseDatabase.getInstance().reference.child(DATA_USERS)
        chatDatabase = FirebaseDatabase.getInstance().reference.child(DATA_CHATS)

        profileTab=navigationTabs.newTab()
        swipeTab=navigationTabs.newTab()
        matchesTab=navigationTabs.newTab()

        profileTab?.icon=ContextCompat.getDrawable(this,R.drawable.tab_profile)
        swipeTab?.icon=ContextCompat.getDrawable(this,R.drawable.tab_swipe)
        matchesTab?.icon=ContextCompat.getDrawable(this,R.drawable.tab_matches)

        navigationTabs.addTab(profileTab!!)
        navigationTabs.addTab(swipeTab!!)
        navigationTabs.addTab(matchesTab!!)

        navigationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab){
                    profileTab ->{
                        if (profileFragment == null){
                            profileFragment = ProfileFragment()
                            profileFragment!!.setCallback(this@TenantActivity)
                        }
                        replaceFragment(profileFragment!!)
                    }
                    swipeTab ->{
                        if (swipeFragment == null){
                            swipeFragment = SwipeFragment()
                            swipeFragment!!.setCallback(this@TenantActivity)
                        }
                        replaceFragment(swipeFragment!!)
                    }
                    matchesTab ->{
                        if (matchesFragment == null){
                            matchesFragment = MatchesFragment()
                            matchesFragment!!.setCallback(this@TenantActivity)
                        }
                        replaceFragment(matchesFragment!!)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                onTabSelected(tab)
            }

        })
        profileTab?.select()
    }
    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode                                                                                                                               == REQUEST_CODE_PHOTO){
            resultImageUrl = data?.data
            storeImage()
        }
    }

    fun storeImage(){
        if (resultImageUrl !=null && userId != null){
            val filePath = FirebaseStorage.getInstance().reference.child("profileImage").child(userId)
            var bitmap: Bitmap? =  null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(application.contentResolver,resultImageUrl)
            }
            catch (e: IOException){
                e.printStackTrace()
            }

            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG,20,baos)
            val data =baos.toByteArray()

            val uploadTask = filePath.putBytes(data)
            uploadTask.addOnFailureListener{e -> e.printStackTrace()}
            uploadTask.addOnSuccessListener { tasksnapshot : UploadTask.TaskSnapshot? ->
                filePath.downloadUrl
                    .addOnSuccessListener { uri->
                        profileFragment?.updateImageUri(uri.toString())
                    }
                    .addOnFailureListener { e -> e.printStackTrace() }
            }
        }
    }

    override fun onSignout() {
        firebaseAuth.signOut()
        startActivity(StartupActivity.newIntent(this))
        finish()
    }

    override fun onGetUserId():String = userId!!

    override fun getUserDatabase(): DatabaseReference = userDatabase

    override fun getChatDatabase(): DatabaseReference = chatDatabase

    override fun profileComplete() {
        swipeTab?.select()
    }

    override fun startActivityForPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type ="image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }

    companion object{
        fun newIntent(context: Context?)= Intent(context, TenantActivity::class.java)
    }
}