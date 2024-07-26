package com.example.ludogame.models

class Board(private val numberOfPlayers: Int) {
    companion object {
        const val BOARD_SIZE = 52
        const val HOME_STRETCH = 6
        val STAR_POSITIONS = setOf(0, 8, 13, 21, 26, 34, 39, 47)
    }

    val spaces: List<Space> = List(BOARD_SIZE) { Space(it) }

    fun getNextPosition(currentPosition: Int, steps: Int, playerId: Int): Int {
        val homeEntrance = playerHomeEntrance(playerId)
        val homeStretchStart = BOARD_SIZE
        val homeStretchEnd = homeStretchStart + HOME_STRETCH - 1

        if (currentPosition >= homeStretchStart) {
            // Piece is in the home stretch
            val stepsIntoHomeStretch = currentPosition - homeStretchStart
            val newPosition = homeStretchStart + stepsIntoHomeStretch + steps
            val clampedPosition = if (newPosition > homeStretchEnd) homeStretchEnd else newPosition
            return clampedPosition
        }

        // Piece is on the main board
        var nextPosition = (currentPosition + steps) % BOARD_SIZE

        // Check if the piece has passed or landed on its home entrance
        val passedHomeEntrance = if (playerId == 0) {
            // Special case for green (player 0)
            (currentPosition <= homeEntrance && nextPosition > homeEntrance) ||
                    (currentPosition > homeEntrance) ||
                    (nextPosition < currentPosition)
        } else {
            homeEntrance in (currentPosition + 1)..nextPosition
        }

        if (passedHomeEntrance) {
            val stepsIntoHomeStretch = if (playerId == 0) {
                if (nextPosition > homeEntrance) {
                    nextPosition - homeEntrance -1
                } else {
                    (BOARD_SIZE - homeEntrance) + nextPosition -1
                }
            } else {
                nextPosition - homeEntrance
            }
            val newPosition = homeStretchStart + stepsIntoHomeStretch
            return newPosition
        }

        return nextPosition
    }


    fun playerStartPosition(playerId: Int): Int =
        when {
            numberOfPlayers == 2 && playerId == 1 -> 26  // Blue position for second player in 2-player game
            else -> when (playerId) {
                0 -> 0   // Green
                1 -> 13  // Red
                2 -> 26  // Blue
                3 -> 39  // Yellow
                else -> throw IllegalArgumentException("Invalid player ID")
            }
        }

    fun playerHomeEntrance(playerId: Int): Int =
        when {
            numberOfPlayers == 2 && playerId == 1 -> 25  // Blue position for second player in 2-player game
            else -> when (playerId) {
                0 -> 50  // Green
                1 -> 12  // Red
                2 -> 25  // Blue
                3 -> 38  // Yellow
                else -> throw IllegalArgumentException("Invalid player ID")
            }
        }
}

data class Space(val position: Int, var occupiedBy: Piece? = null)