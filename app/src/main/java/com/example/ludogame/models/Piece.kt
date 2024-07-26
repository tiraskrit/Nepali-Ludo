package com.example.ludogame.models

data class Piece(
    val id: Int,
    val player: Player,
    var position: Int = -1,
    var isHome: Boolean = false
)