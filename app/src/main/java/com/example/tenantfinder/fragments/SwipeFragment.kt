package com.example.tenantfinder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.tenantfinder.R
import com.example.tenantfinder.activities.TenantCallback
import com.example.tenantfinder.adapters.CardsAdapter
import com.example.tenantfinder.user
import com.example.tenantfinder.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.fragment_swipe.*


class SwipeFragment : Fragment() {

    private var callback: TenantCallback? =null
    private lateinit var userID: String
    private lateinit var userDatabase:DatabaseReference
    private lateinit var chatDatabase: DatabaseReference
    private var cardsAdapter: ArrayAdapter<user>? = null
    private var rowItems = ArrayList<user>()
    private var preferred: String? =null
    private var userName: String? =null
    private var imageUrl: String? = null

    fun setCallback(callback: TenantCallback){
        this.callback= callback
        userID= callback.onGetUserId()
        userDatabase = callback.getUserDatabase()
        chatDatabase = callback.getChatDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_swipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userDatabase.child(userID).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user =p0.getValue(user::class.java)
                preferred = user?.gender
                userName = user?.name
                imageUrl = user?.imageUrl
                populateItems()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        cardsAdapter = CardsAdapter(context, R.layout.item, rowItems)

        frame.adapter = cardsAdapter
        frame.setFlingListener(object: SwipeFlingAdapterView.onFlingListener{

            override fun removeFirstObjectInAdapter() {
                rowItems.removeAt(0)
                cardsAdapter?.notifyDataSetChanged()
            }

            override fun onLeftCardExit(p0: Any?) {
                var user = p0 as user
                userDatabase.child(user.uid.toString()).child(DATA_SWIIPES_LEFT).child(userID).setValue(true)
            }

            override fun onRightCardExit(p0: Any?) {
                var selectedUser = p0 as user
                var selectedUserId = selectedUser.uid
                if(!selectedUserId.isNullOrEmpty()){
                    userDatabase.child(userID).child(DATA_SWIPES_RIGHT).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.hasChild(selectedUserId)){
                                Toast.makeText(context,"Match!",Toast.LENGTH_SHORT).show()

                                val chatKey = chatDatabase.push().key

                                if (chatKey != null) {

                                    userDatabase.child(userID).child(DATA_SWIPES_RIGHT)
                                        .child(selectedUserId).removeValue()
                                    userDatabase.child(userID).child(DATA_MATCHES)
                                        .child(selectedUserId).setValue(chatKey)
                                    userDatabase.child(selectedUserId).child(DATA_MATCHES)
                                        .child(userID).setValue(chatKey)

                                    chatDatabase.child(chatKey).child(userID).child(DATA_NAME).setValue(userName)
                                    chatDatabase.child(chatKey).child(userID).child(DATA_IMAGE_URL).setValue(imageUrl)

                                    chatDatabase.child(chatKey).child(selectedUserId).child(DATA_NAME)
                                        .setValue(selectedUser.name)
                                    chatDatabase.child(chatKey).child(selectedUserId).child(DATA_IMAGE_URL)
                                        .setValue(selectedUser.imageUrl)
                                }
                            }
                            else{
                                userDatabase.child(selectedUserId).child(DATA_SWIPES_RIGHT).child(userID).setValue(true)
                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })
                }
            }

            override fun onAdapterAboutToEmpty(p0: Int) {

            }

            override fun onScroll(p0: Float) {

            }
        })

        frame.setOnItemClickListener { position, data ->  }

        LikeButton.setOnClickListener{
            if (!rowItems.isEmpty()){
                frame.topCardListener.selectRight()
            }
        }

        disLikeButton.setOnClickListener{
            if (!rowItems.isEmpty()){
                frame.topCardListener.selectLeft()
            }
        }
    }

    fun populateItems(){
        noUsersLayout.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        val cardsQuery = userDatabase.orderByChild(DATA_PREFERRED).equalTo(preferred)
        cardsQuery.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{child ->
                    val user =child.getValue(user::class.java)
                    if(user != null){
                        var showUser = true
                        if(child.child(DATA_SWIIPES_LEFT).hasChild(userID) ||
                            child.child(DATA_SWIPES_RIGHT).hasChild(userID) ||
                            child.child(DATA_MATCHES).hasChild(userID)){
                            showUser = false
                            }
                        if (showUser){
                            rowItems.add(user)
                            cardsAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                progressLayout.visibility = View.GONE
                if (rowItems.isEmpty()){
                    noUsersLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}