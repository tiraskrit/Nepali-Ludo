package com.example.ludogame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ludogame.ui.components.BoardComponent
import com.example.ludogame.ui.components.DiceComponent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val viewModel: GameViewModel = viewModel()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (viewModel.gameState == GameState.GameOver) {
            Text("Game Over!", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { viewModel.resetGame() }) {
                Text("Start New Game")
            }
        } else if (viewModel.players.isEmpty()) {
            PlayerSetupScreen(viewModel)
        } else {
            GameContent(viewModel)
        }
    }
}

@Composable
fun PlayerSetupScreen(viewModel: GameViewModel) {
    Text("Select number of players:", style = MaterialTheme.typography.headlineSmall)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (2..4).forEach { playerCount ->
            Button(
                onClick = { viewModel.setNumberOfPlayers(playerCount) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.numberOfPlayers == playerCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("$playerCount")
            }
        }
    }

    repeat(viewModel.numberOfPlayers) { index ->
        OutlinedTextField(
            value = viewModel.playerNames[index],
            onValueChange = { viewModel.setPlayerName(index, it) },
            label = { Text("Player ${index + 1} Name") }
        )
    }

    Button(onClick = { viewModel.startGame() }) {
        Text("Start Game")
    }
}

@Composable
fun GameContent(viewModel: GameViewModel) {
    Text("Current Player: ${viewModel.getCurrentPlayerName()}")
    BoardComponent(
        viewModel = viewModel,
        modifier = Modifier.size(300.dp)
    )
    DiceComponent(
        diceValue = viewModel.diceValue,
        onRollDice = { viewModel.rollDice() },
        enabled = viewModel.gameState == GameState.RollDice
    )

    viewModel.message.value?.let { message ->
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        LaunchedEffect(message) {
            delay(2000)
            viewModel.clearMessage()
        }
    }

    when (viewModel.gameState) {
        GameState.RollDice -> {
            Text("Click the dice to roll")
        }
        GameState.SelectPiece -> {
            if (viewModel.moveablePieces.isNotEmpty()) {
                Text("Select a piece to move")
            } else {
                Text("No movable pieces. Next player's turn.")
            }
        }
        GameState.PlayerFinished -> {
            Text("Player finished! Game continues for others.")
            Button(onClick = { viewModel.nextTurn() }) {
                Text("Continue Game")
            }
        }
        else -> {} // GameOver case is handled in the parent composable
    }
}