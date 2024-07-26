package com.example.ludogame.ui.components

import com.example.ludogame.models.Board
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ludogame.R
import com.example.ludogame.models.Piece
import com.example.ludogame.ui.GameViewModel

@Composable
fun BoardComponent(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(300.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ludo_board),
            contentDescription = "Ludo Board",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )

        viewModel.players.flatMap { it.pieces }.forEach { piece ->
            val (x, y) = calculatePiecePosition(piece, viewModel.numberOfPlayers)
            PieceComponent(
                piece = piece,
                isMoveable = piece in viewModel.moveablePieces,
                onPieceClick = {
                    if (piece in viewModel.moveablePieces) {
                        viewModel.movePiece(piece)
                    }
                },
                numberOfPlayers = viewModel.numberOfPlayers,
                modifier = Modifier.offset(x = x.dp, y = y.dp)
            )
        }
    }
}



fun calculatePiecePosition(piece: Piece, numberOfPlayers: Int): Pair<Float, Float> {
    val boardPixelSize = 2048f
    val boxSize = 300f
    val scaleFactor = boxSize / boardPixelSize
    val boxTileSize = 832f - 60f // Define the size of each box

    // Define box positions
    val greenBox = Pair(60f, 60f)
    val redBox = Pair(1215f, 60f)
    val blueBox = Pair(1215f, 1215f)
    val yellowBox = Pair(60f, 1215f)

    // Define the starting positions and home area offsets for each player
    val startPositions = listOf(greenBox, redBox, blueBox, yellowBox)

    val homeOffsets = listOf(
        listOf(Pair(0.4f, 0.22f), Pair(0.22f, 0.4f), Pair(0.57f, 0.4f), Pair(0.4f, 0.57f)),
        listOf(Pair(0.4f, 0.22f), Pair(0.22f, 0.4f), Pair(0.57f, 0.4f), Pair(0.4f, 0.57f)),
        listOf(Pair(0.4f, 0.22f), Pair(0.22f, 0.4f), Pair(0.57f, 0.4f), Pair(0.4f, 0.57f)),
        listOf(Pair(0.4f, 0.22f), Pair(0.22f, 0.4f), Pair(0.57f, 0.4f), Pair(0.4f, 0.57f))
    )

    val boardPathOffsets = listOf(
        // Green start (index 0)
        Pair(2.2f, 10.6f), Pair(3.9f, 10.6f), Pair(5.6f, 10.6f), Pair(7.3f, 10.6f), Pair(9f, 10.6f),
        Pair(10.7f, 8.9f), Pair(10.7f, 7.2f), Pair(10.7f, 5.5f), Pair(10.7f, 3.8f), Pair(10.7f, 2.1f), Pair(10.7f, 0.5f),
        Pair(12.4f, 0.5f), Pair(14.0f, 0.5f),
        // Red start (index 13)
        Pair(13.9f, 2.2f), Pair(13.9f, 3.9f), Pair(13.9f, 5.6f), Pair(13.9f, 7.3f), Pair(13.9f, 9f),
        Pair(15.6f, 10.7f), Pair(17.3f, 10.7f), Pair(19f, 10.7f), Pair(20.7f, 10.7f), Pair(22.3f, 10.7f), Pair(24f, 10.7f),
        Pair(24f, 12.3f), Pair(24f, 14f),
        // Blue start (index 26)
        Pair(22.2f, 13.9f), Pair(20.5f, 13.9f), Pair(18.8f, 13.9f), Pair(17.1f, 13.9f), Pair(15.4f, 13.9f),
        Pair(13.7f, 15.6f), Pair(13.7f, 17.3f), Pair(13.7f, 19f), Pair(13.7f, 20.7f), Pair(13.7f, 22.3f), Pair(13.7f, 24f),
        Pair(12f, 24f), Pair(10.3f, 24f),
        // Yellow start (index 39)
        Pair(10.5f, 22.2f), Pair(10.5f, 20.5f), Pair(10.5f, 18.8f), Pair(10.5f, 17.1f), Pair(10.5f, 15.4f),
        Pair(8.8f, 13.7f), Pair(7.1f, 13.7f), Pair(5.4f, 13.7f), Pair(3.7f, 13.7f), Pair(2.1f, 13.7f), Pair(0.5f, 13.7f),
        Pair(0.5f, 12f), Pair(0.5f, 10.3f)
    )

    val homeStretchOffsets = listOf(
        // Green (right)
        List(6) { Pair(2.2f + it * 1.7f, 12.3f) },
        // Red (down)
        List(6) { Pair(12.2f, 2.2f + it * 1.7f) },
        // Blue (left)
        List(6) { Pair(22.2f - it * 1.7f, 12.2f) },
        // Yellow (up)
        List(6) { Pair(12.2f, 22.2f - it * 1.7f) }
    )

    val (x, y) = when {
        piece.position == -1 -> {
            // Piece is in the starting area
            val startPositionIndex = if (numberOfPlayers == 2 && piece.player.id == 1) 2 else piece.player.id
            val (startX, startY) = startPositions[startPositionIndex]
            val (homeX, homeY) = homeOffsets[startPositionIndex][piece.id]
            Pair(
                (startX + homeX * boxTileSize) * scaleFactor,
                (startY + homeY * boxTileSize) * scaleFactor
            )
        }
        piece.position >= Board.BOARD_SIZE -> {
            // Piece is in the home stretch
            val homeStretchIndex = piece.position - Board.BOARD_SIZE
            val homeStretchPositionIndex = if (numberOfPlayers == 2 && piece.player.id == 1) 2 else piece.player.id
            val (pathX, pathY) = homeStretchOffsets[homeStretchPositionIndex][homeStretchIndex]
            Pair(
                (pathX * boxTileSize / 10f * scaleFactor),
                (pathY * boxTileSize / 10f * scaleFactor)
            )
        }
        else -> {
            // Piece is on the main board
            val (pathX, pathY) = boardPathOffsets[piece.position]
            Pair(
                (pathX * boxTileSize / 10f) * scaleFactor,
                (pathY * boxTileSize / 10f) * scaleFactor
            )
        }
    }
    return Pair(x, y)
}




