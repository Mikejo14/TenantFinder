package com.example.tenantfinder

data class user(
    val uid:String?="",
    val name:String?="",
    val age:String?="",
    val email:String?="",
    val gender:String?="",
    val preferred:String?="",
    val imageUrl:String?=""
)

data class Chat(
    val userId: String? ="",
    val chatId: String? ="",
    val otherUserId: String? ="",
    val name: String? ="",
    val imageUrl:String?=""
)

data class Message(
    val sentBy: String?= null,
    val message: String?= null,
    val time: String?= null,
)