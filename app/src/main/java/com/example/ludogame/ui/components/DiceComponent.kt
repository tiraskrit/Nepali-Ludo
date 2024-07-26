package com.example.ludogame.ui.components


import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import com.example.ludogame.R
import androidx.compose.runtime.Composable

@Composable
fun DiceComponent(
    diceValue: Int,
    onRollDice: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isRolling by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = repeatable(
            iterations = 1, // how many spins
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        finishedListener = { isRolling = false }
    )

    val diceImageResource = when (diceValue) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        else -> R.drawable.dice_1
    }

    Image(
        painter = painterResource(id = diceImageResource),
        contentDescription = "Dice with value $diceValue",
        modifier = modifier
            .size(50.dp)
            .rotate(rotation)
            .clickable(enabled = enabled) {
                isRolling = true
                onRollDice()
            }
    )
}