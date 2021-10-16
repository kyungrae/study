package me.hama

import org.springframework.data.annotation.Id

class User(
    @Id
    var id: String? = null,
    var name: String,
    var password: String,
    var roles: List<String>
)
