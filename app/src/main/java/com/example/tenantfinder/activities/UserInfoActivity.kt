package com.example.tenantfinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.tenantfinder.R
import com.example.tenantfinder.user
import com.example.tenantfinder.util.DATA_USERS
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        val userID = intent.extras?.getString(PARAM_USER_ID,"")
        if (userID.isNullOrEmpty()){
            finish()
        }

        val userDatabase = FirebaseDatabase.getInstance().reference.child(DATA_USERS)
        userDatabase.child(userID!!).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(user::class.java)
                userInfoName.text = user?.name
                userInfoAge.text = user?.age
                if (user?.imageUrl != null){
                    Glide.with(this@UserInfoActivity)
                        .load(user.imageUrl)
                        .into(userInfoIV)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    companion object{
        private val PARAM_USER_ID = "user id"

        fun newIntent(context: Context, userID: String?): Intent {
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra(PARAM_USER_ID, userID)
            return intent
        }
    }
}