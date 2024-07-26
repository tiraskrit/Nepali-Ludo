package com.example.ludogame.ui

import com.example.ludogame.models.Board
import com.example.ludogame.models.Dice
import com.example.ludogame.models.Player
import com.example.ludogame.models.Piece
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private var board: Board = Board(4)
    private val dice = Dice()
    private var playersFinished = 0

    private var consecutiveSixes = 0
    private var consecutiveOnes = 0

    private var justMovedDueToSpecialCase = false

    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    private var _isGameOverMessageDisplayed = mutableStateOf(false)
    val isGameOverMessageDisplayed: State<Boolean> = _isGameOverMessageDisplayed

    var diceValue by mutableStateOf(0)
        private set
    var gameState by mutableStateOf<GameState>(GameState.RollDice)
        private set
    var moveablePieces by mutableStateOf<List<Piece>>(emptyList())
        private set

    private var _numberOfPlayers by mutableStateOf(4)
    val numberOfPlayers: Int get() = _numberOfPlayers

    private var _playerNames by mutableStateOf(List(4) { "Player ${it + 1}" })
    val playerNames: List<String> get() = _playerNames

    private var _currentPlayerIndex by mutableStateOf(0)
    var currentPlayerIndex: Int
        get() = _currentPlayerIndex
        private set(value) {
            _currentPlayerIndex = value
        }

    private var _players by mutableStateOf<List<Player>>(emptyList())
    val players: List<Player> get() = _players


    fun setNumberOfPlayers(count: Int) {
        require(count in 2..4) { "Number of players must be between 2 and 4" }
        _numberOfPlayers = count
        board = Board(count)  // Update the board with the new number of players
    }

    fun setPlayerName(index: Int, name: String) {
        _playerNames = _playerNames.toMutableList().also { it[index] = name }
    }

    fun startGame() {
        setNumberOfPlayers(_numberOfPlayers)
        _players = when (_numberOfPlayers) {
            2 -> listOf(
                Player(0, Color.Green, _playerNames[0]),
                Player(1, Color.Blue, _playerNames[1])
            )
            3 -> listOf(
                Player(0, Color.Green, _playerNames[0]),
                Player(1, Color.Red, _playerNames[1]),
                Player(2, Color.Blue, _playerNames[2])
            )
            else -> listOf(
                Player(0, Color.Green, _playerNames[0]),
                Player(1, Color.Red, _playerNames[1]),
                Player(2, Color.Blue, _playerNames[2]),
                Player(3, Color.Yellow, _playerNames[3])
            )
        }
        currentPlayerIndex = 0
        resetGame()
    }

    fun resetGame() {
        currentPlayerIndex = 0
        diceValue = 0
        gameState = GameState.RollDice
        moveablePieces = emptyList()
        playersFinished = 0
        players.forEach { player ->
            player.hasFinished = false
            player.pieces.forEach { piece ->
                piece.position = -1
                piece.isHome = false
            }
        }
        board.spaces.forEach { space ->
            space.occupiedBy = null
        }
    }

/*    fun getCurrentPlayerColor(): String {
        return when (currentPlayerIndex) {
            0 -> "Green"
            1 -> "Red"
            2 -> "Blue"
            3 -> "Yellow"
            else -> "Unknown"
        }
    }*/

    private fun updateMoveablePieces() {
        moveablePieces = if (!justMovedDueToSpecialCase) {
            getCurrentPlayer().pieces.filter { piece ->
                canMovePiece(piece) || (piece.position == -1 && diceValue == 1)
            }
        } else {
            emptyList()
        }
    }

    fun rollDice() {
        if (gameState == GameState.RollDice) {
            diceValue = dice.roll()
            justMovedDueToSpecialCase = false
            handleDiceRoll()
        }
    }

    private fun handleDiceRoll() {
        when (diceValue) {
            1 -> {
                consecutiveOnes++
                consecutiveSixes = 0
                if (consecutiveOnes == 3) {
                    moveLongestPieceBack()
                    justMovedDueToSpecialCase = true
                } else if (consecutiveOnes == 7) {
                    handleSevenConsecutiveOnes()
                    return  // End the turn immediately
                }
            }
            6 -> {
                consecutiveSixes++
                consecutiveOnes = 0
                if (consecutiveSixes == 3) {
                    handleThreeConsecutiveSixes()
                    val allPiecesInBox = getCurrentPlayer().pieces.all { it.position == -1 }
                    if (allPiecesInBox) {
                        justMovedDueToSpecialCase = true
                    }
                }
            }
            else -> {
                consecutiveOnes = 0
                consecutiveSixes = 0
            }
        }

        if (justMovedDueToSpecialCase) {
            gameState = GameState.RollDice
        } else {
            updateMoveablePieces()
            if (moveablePieces.isEmpty()) {
                if (diceValue == 1 || diceValue == 6) {
                    gameState = GameState.RollDice
                } else {
                    nextTurn()
                }
            } else {
                gameState = GameState.SelectPiece
            }
        }
    }

    private fun handleSevenConsecutiveOnes() {
        val currentPlayer = getCurrentPlayer()
        playerWins(currentPlayer, "with 7 consecutive 1s")
    }

    private fun handleThreeConsecutiveSixes() {
        val currentPlayer = getCurrentPlayer()
        val piecesInBox = currentPlayer.pieces.filter { it.position == -1 }
        val piecesOnBoard = currentPlayer.pieces.filter { it.position != -1 }

        val noMoveablePiecesOnBoard = piecesOnBoard.none { canMovePiece(it) }

        if (piecesInBox.isNotEmpty() && noMoveablePiecesOnBoard) {
            moveNewPieceToStart()
        }
    }

    private fun moveLongestPieceBack() {
        val longestMovedPiece = getCurrentPlayer().pieces
            .filter { it.position in 0 until Board.BOARD_SIZE }
            .maxByOrNull { it.position }

        longestMovedPiece?.let { piece ->
            board.spaces[piece.position].occupiedBy = null
            piece.position = -1
            _message.value = "Oops! Three consecutive 1s. Your longest moved piece returned to start."
        } ?: run {
            _message.value = "Three consecutive 1s, but no piece to move back."
        }
    }

    private fun moveNewPieceToStart() {
        val pieceAtStart = getCurrentPlayer().pieces.find { it.position == -1 }
        pieceAtStart?.let { piece ->
            piece.position = board.playerStartPosition(currentPlayerIndex)
            board.spaces[piece.position].occupiedBy = piece
            _message.value = "Three consecutive 6s! A new piece moved to the starting position."
        }
        justMovedDueToSpecialCase = true
    }


    fun getCurrentPlayerName(): String {
        return players[currentPlayerIndex].name
    }

    fun nextTurn() {
        consecutiveOnes = 0
        consecutiveSixes = 0
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (players[currentPlayerIndex].hasFinished)

        if (players.count { !it.hasFinished } <= 1) {
            endGame()
        } else {
            gameState = GameState.RollDice
            moveablePieces = emptyList()
        }
    }


    fun movePiece(piece: Piece) {
        if (gameState == GameState.SelectPiece && piece in moveablePieces) {
            val oldPosition = piece.position
            val diceRoll = diceValue
            val newPosition = if (oldPosition == -1) {
                board.playerStartPosition(currentPlayerIndex)
            } else {
                board.getNextPosition(oldPosition, diceRoll, currentPlayerIndex)
            }

            // Update the piece's position
            piece.position = newPosition

            // Handle capture (only on main board)
            if (newPosition < Board.BOARD_SIZE) {
                val capturedPiece = board.spaces[newPosition].occupiedBy
                if (capturedPiece != null && capturedPiece.player != getCurrentPlayer() && !Board.STAR_POSITIONS.contains(newPosition)) {
                    capturedPiece.position = -1
                }
            }

            // Update board state
            if (oldPosition != -1 && oldPosition < Board.BOARD_SIZE) {
                board.spaces[oldPosition].occupiedBy = null
            }
            if (newPosition < Board.BOARD_SIZE) {
                board.spaces[newPosition].occupiedBy = piece
            }

            // Check if the piece has reached home stretch and handle it accordingly
            if (piece.position >= Board.BOARD_SIZE) {
                val stepsIntoHomeStretch = piece.position - Board.BOARD_SIZE
                if (stepsIntoHomeStretch >= Board.HOME_STRETCH) {
                    piece.isHome = true
                }
            }

            if (piece.position >= Board.BOARD_SIZE + Board.HOME_STRETCH - 1) {
                piece.isHome = true
                _message.value = "Bravo, the piece is home!"
            }

            updateMoveablePieces()
            checkWinCondition()
            if (gameState != GameState.GameOver && gameState != GameState.PlayerFinished) {
                if (diceValue == 1 || diceValue == 6) {
                    gameState = GameState.RollDice
                } else {
                    nextTurn()
                }
            }
        }
    }


/*    private fun handleDiceRollResult() {
        if (diceValue == 6 || diceValue == 1) {
            gameState = GameState.RollDice
//            _message.value = "You rolled a ${diceValue}! Roll again."
        } else {
            nextTurn()
        }
    }*/

    private fun canMovePiece(piece: Piece): Boolean {
        if (piece.isHome) return false
        if (piece.position == -1 && diceValue != 1) return false
        if (piece.position >= Board.BOARD_SIZE) {
            // Check if the piece can move within the home stretch
            return piece.position + diceValue < Board.BOARD_SIZE + Board.HOME_STRETCH
        }
        return true
    }

    private fun checkWinCondition(): Boolean {
        val currentPlayer = getCurrentPlayer()
        val hasWon = currentPlayer.pieces.all { it.isHome }
        if (hasWon && !currentPlayer.hasFinished) {
            playerWins(currentPlayer, "by getting all pieces home")
        }
        return hasWon
    }

    private fun playerWins(player: Player, reason: String) {
        player.hasFinished = true
        playersFinished++
        _message.value = "${player.name} has won the game $reason!"

        if (players.count { !it.hasFinished } == 1) {
            val lastPlayer = players.first { !it.hasFinished }
            _message.value += " ${lastPlayer.name} is the last player remaining."
            displayGameOverMessage()
        } else {
            gameState = GameState.PlayerFinished
        }
    }

    private fun displayGameOverMessage() {
        viewModelScope.launch {
            _isGameOverMessageDisplayed.value = true
            delay(2000) // 2 seconds delay
            _isGameOverMessageDisplayed.value = false
            endGame()
        }
    }


    private fun endGame() {
        gameState = GameState.GameOver
        _message.value += " Game Over!"
    }

    private fun getCurrentPlayer(): Player = players[currentPlayerIndex]

    fun clearMessage() {
        _message.value = null
    }
}

enum class GameState {
    RollDice, SelectPiece, PlayerFinished, GameOver
}