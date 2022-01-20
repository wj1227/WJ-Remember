package com.jay.data.model

data class DataUser(
    val name: String,
    val image: String?,
    var hasLiked: Boolean = false
) : DataModel