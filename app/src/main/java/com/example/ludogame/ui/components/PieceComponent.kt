package com.example.ludogame.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ludogame.R
import com.example.ludogame.models.Piece

@Composable
fun PieceComponent(
    piece: Piece,
    isMoveable: Boolean,
    onPieceClick: (Piece) -> Unit,
    numberOfPlayers: Int,
    modifier: Modifier = Modifier
) {
    val pieceImage = when {
        numberOfPlayers == 2 && piece.player.id == 1 -> R.drawable.piece_blue
        else -> when (piece.player.id) {
            0 -> R.drawable.piece_green
            1 -> R.drawable.piece_red
            2 -> R.drawable.piece_blue
            3 -> R.drawable.piece_yellow
            else -> R.drawable.piece_red // fallback
        }
    }

    Box(modifier = modifier.size(24.dp)) {
        Image(
            painter = painterResource(id = pieceImage),
            contentDescription = "Player ${piece.player.id} Piece ${piece.id}",
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center)
                .clickable { onPieceClick(piece) }
        )

        if (isMoveable) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, Color.Yellow, CircleShape)
            )
        }
    }
}