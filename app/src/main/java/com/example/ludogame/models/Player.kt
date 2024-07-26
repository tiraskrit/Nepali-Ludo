package com.example.ludogame.models

import androidx.compose.ui.graphics.Color

data class Player(
    val id: Int,
    val color: Color,
    val name: String
) {
    val pieces: List<Piece> by lazy { List(4) { index -> Piece(index, this) } }
    var hasFinished: Boolean = false
}