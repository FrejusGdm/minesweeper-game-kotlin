package hu.ait.tictactoe.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.ait.tictactoe.R
import kotlinx.coroutines.delay

@Composable
fun MinesweeperScreen(
    modifier: Modifier = Modifier,
    viewModel: MinesweeperModel = viewModel()
) {
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showGameOverDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game mode toggle
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.flag_mode))
            Switch(
                checked = viewModel.isFlagMode,
                onCheckedChange = { viewModel.toggleFlagMode() }
            )
        }

        // Game board
        MinesweeperBoard(viewModel) { message ->
            snackbarMessage = message
            showSnackbar = true
        }

        // Reset button
        Button(
            onClick = {
                viewModel.resetGame()
                showGameOverDialog = false
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.reset_game))
        }
    }

    // Delayed Game Over Dialog
    LaunchedEffect(viewModel.isGameOver) {
        if (viewModel.isGameOver) {
            delay(2000) // Wait for Snackbar to show and dismiss
            showGameOverDialog = true
        }
    }

    if (showGameOverDialog) {
        GameOverDialog(
            isVictory = viewModel.isVictory,
            onDismiss = {
                viewModel.resetGame()
                showGameOverDialog = false
            }
        )
    }

    // Snackbar for messages
    if (showSnackbar) {
        LaunchedEffect(key1 = showSnackbar) {
            delay(1500) // Show for 1.5 seconds
            showSnackbar = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showSnackbar = false }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(snackbarMessage)
        }
    }
}

@Composable
fun MinesweeperBoard(viewModel: MinesweeperModel, onMessage: (String) -> Unit) {
    val backgroundImage = painterResource(id = R.drawable.dragon_ball)
    val mineImage = painterResource(id = R.drawable.mine)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(0.9f)
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = stringResource(R.string.background_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Grid lines
        Column(Modifier.fillMaxSize()) {
            repeat(6) { // 5 cells + 1 for the outer border
                Divider(color = Color.Black, thickness = 1.dp)
                if (it < 5) {
                    Row(Modifier.weight(1f)) {
                        repeat(6) {
                            if (it > 0) {
                                Divider(
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                )
                            }
                            if (it < 5) {
                                Box(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Cells
        Column {
            for (row in viewModel.board.indices) {
                Row {
                    for (col in viewModel.board[row].indices) {
                        val cell = viewModel.board[row][col]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    when (cell.state) {
                                        CellState.COVERED -> Color.Gray.copy(alpha = 0.5f)
                                        CellState.FLAGGED -> Color.Red.copy(alpha = 0.5f)
                                        CellState.UNCOVERED -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    val result = viewModel.onCellClicked(row, col)
                                    when (result) {
                                        is CellClickResult.GameOver -> onMessage(result.message)
                                        is CellClickResult.NumberRevealed -> onMessage("${result.number} mines nearby")
                                        is CellClickResult.Victory -> onMessage("You won!")
                                        is CellClickResult.CellsCleared -> onMessage("Cleared ${result.count} cells")
                                        CellClickResult.NoEffect -> {
                                            if (viewModel.isFlagMode) {
                                                onMessage("Toggled flag")
                                            } else {
                                                onMessage("Can't uncover a flagged cell")
                                            }
                                        }
                                    }
                                }
                        ) {
                            when {
                                cell.state == CellState.FLAGGED -> {
                                    Text("🚩", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
                                }
                                cell.state == CellState.UNCOVERED && cell.hasMine -> {
                                    Image(
                                        painter = mineImage,
                                        contentDescription = "Mine",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                cell.state == CellState.UNCOVERED && cell.neighboringMines > 0 -> {
                                    Text(
                                        text = cell.neighboringMines.toString(),
                                        modifier = Modifier.align(Alignment.Center),
                                        color = Color.White,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun GameOverDialog(isVictory: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isVictory) R.string.victory else R.string.game_over)) },
        text = { Text(stringResource(if (isVictory) R.string.you_won else R.string.you_hit_mine)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.play_again))
            }
        }
    )
}
//@Composable
//fun MinesweeperScreen(
//    modifier: Modifier = Modifier,
//    viewModel: MinesweeperModel = viewModel()
//) {
//    var showSnackbar by remember { mutableStateOf(false) }
//    var snackbarMessage by remember { mutableStateOf("") }
//
//    Column(
//        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Game mode toggle
//        Row(
//            modifier = Modifier.padding(bottom = 16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(stringResource(R.string.flag_mode))
//            Switch(
//                checked = viewModel.isFlagMode,
//                onCheckedChange = { viewModel.toggleFlagMode() }
//            )
//        }
//
//        // Game board
//        MinesweeperBoard(viewModel) { message ->
//            snackbarMessage = message
//            showSnackbar = true
//        }
//
//        // Reset button
//        Button(
//            onClick = { viewModel.resetGame() },
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Text(stringResource(R.string.reset_game))
//        }
//    }
//
//    // Game over dialog
//    if (viewModel.isGameOver) {
//        GameOverDialog(
//            isVictory = viewModel.isVictory,
//            onDismiss = { viewModel.resetGame() }
//        )
//    }
//
//    // Snackbar for messages
//    if (showSnackbar) {
//        LaunchedEffect(key1 = showSnackbar) {
//            kotlinx.coroutines.delay(2000) // Show for 2 seconds
//            showSnackbar = false
//        }
//        Snackbar(
//            modifier = Modifier.padding(16.dp),
//            action = {
//                TextButton(onClick = { showSnackbar = false }) {
//                    Text("Dismiss")
//                }
//            }
//        ) {
//            Text(snackbarMessage)
//        }
//    }
//}
//@Composable
//fun MinesweeperBoard(viewModel: MinesweeperModel, onMessage: (String) -> Unit) {
//    val backgroundImage = painterResource(id = R.drawable.dragon_ball)
//    val mineImage = painterResource(id = R.drawable.mine)
//
//    Box(
//        modifier = Modifier
//            .aspectRatio(1f)
//            .fillMaxWidth(0.9f)
//    ) {
//        Image(
//            painter = backgroundImage,
//            contentDescription = stringResource(R.string.background_description),
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//
//        // Grid lines
//        Column(Modifier.fillMaxSize()) {
//            repeat(6) { // 5 cells + 1 for the outer border
//                Divider(color = Color.Black, thickness = 1.dp)
//                if (it < 5) {
//                    Row(Modifier.weight(1f)) {
//                        repeat(6) {
//                            if (it > 0) {
//                                Divider(
//                                    color = Color.Black,
//                                    modifier = Modifier
//                                        .fillMaxHeight()
//                                        .width(1.dp)
//                                )
//                            }
//                            if (it < 5) {
//                                Box(Modifier.weight(1f))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Cells
//        Column {
//            for (row in viewModel.board.indices) {
//                Row {
//                    for (col in viewModel.board[row].indices) {
//                        val cell = viewModel.board[row][col]
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f)
//                                .background(
//                                    when (cell.state) {
//                                        CellState.COVERED -> Color.Gray.copy(alpha = 0.5f)
//                                        CellState.FLAGGED -> Color.Red.copy(alpha = 0.5f)
//                                        CellState.UNCOVERED -> Color.Transparent
//                                    }
//                                )
//                                .clickable {
//                                    val result = viewModel.onCellClicked(row, col)
//                                    when (result) {
//                                        is CellClickResult.GameOver -> onMessage(result.message)
//                                        is CellClickResult.NumberRevealed -> onMessage("${result.number} mines nearby")
//                                        is CellClickResult.Victory -> onMessage("You won!")
//                                        is CellClickResult.CellsCleared -> onMessage("Cleared ${result.count} cells")
//                                        CellClickResult.NoEffect -> {
//                                            if (viewModel.isFlagMode) {
//                                                onMessage("Toggled flag")
//                                            } else {
//                                                onMessage("Can't uncover a flagged cell")
//                                            }
//                                        }
//                                    }
//                                }
//                        ) {
//                            when {
//                                cell.state == CellState.FLAGGED -> {
//                                    Text("🚩", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
//                                }
//                                cell.state == CellState.UNCOVERED && cell.hasMine -> {
//                                    Image(
//                                        painter = mineImage,
//                                        contentDescription = "Mine",
//                                        modifier = Modifier.fillMaxSize()
//                                    )
//                                }
//                                cell.state == CellState.UNCOVERED && cell.neighboringMines > 0 -> {
//                                    Text(
//                                        text = cell.neighboringMines.toString(),
//                                        modifier = Modifier.align(Alignment.Center),
//                                        color = Color.White,
//                                        fontSize = 30.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun GameOverDialog(isVictory: Boolean, onDismiss: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(stringResource(if (isVictory) R.string.victory else R.string.game_over)) },
//        text = { Text(stringResource(if (isVictory) R.string.you_won else R.string.you_hit_mine)) },
//        confirmButton = {
//            Button(onClick = onDismiss) {
//                Text(stringResource(R.string.play_again))
//            }
//        }
//    )
//}




