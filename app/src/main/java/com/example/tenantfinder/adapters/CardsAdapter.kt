package com.example.tenantfinder.adapters

import android.content.Context
import android.security.identity.AccessControlProfileId
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.tenantfinder.R
import com.example.tenantfinder.activities.UserInfoActivity
import com.example.tenantfinder.fragments.ProfileFragment
import com.example.tenantfinder.user

class CardsAdapter(context: Context?, resourceId: Int, users:List<user>): ArrayAdapter<user>(context!!, resourceId, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var user = getItem(position)
        var finalView = convertView?:LayoutInflater.from(context).inflate(R.layout.item, parent, false)

        var name = finalView.findViewById<TextView>(R.id.nameTV)
        var image=finalView.findViewById<ImageView>(R.id.photoIV)
        var userinfo=finalView.findViewById<LinearLayout>(R.id.userInfoLayout)

        name.text = "${user?.name}, ${user?.gender} "
        Glide.with(context)
            .load(user?.imageUrl)
            .into(image)

        userinfo.setOnClickListener {
            finalView.context.startActivity(UserInfoActivity.newIntent(finalView.context,user?.uid))
        }

        return finalView

    }
}
