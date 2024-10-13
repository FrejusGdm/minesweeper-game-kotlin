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

@Composable
fun MinesweeperScreen(
    modifier: Modifier = Modifier,
    viewModel: MinesweeperModel = viewModel()
) {
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

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
            onClick = { viewModel.resetGame() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.reset_game))
        }
    }

    // Game over dialog
    if (viewModel.isGameOver) {
        GameOverDialog(
            isVictory = viewModel.isVictory,
            onDismiss = { viewModel.resetGame() }
        )
    }

    // Snackbar for messages
    if (showSnackbar) {
        LaunchedEffect(key1 = showSnackbar) {
            kotlinx.coroutines.delay(2000) // Show for 2 seconds
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
                                        else -> {} // No message for other results
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
//                                        else -> {} // No message for other results
//                                    }
//                                }
//                        ) {
//                            when {
//                                cell.state == CellState.FLAGGED -> {
//                                    Text("🚩", modifier = Modifier.align(Alignment.Center))
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
//                                        modifier = Modifier.align(Alignment.Center)
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
//package hu.ait.tictactoe.ui.screen
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.drawText
//import androidx.compose.ui.text.rememberTextMeasurer
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import hu.ait.tictactoe.R
//import androidx.compose.ui.text.rememberTextMeasurer
//import androidx.compose.ui.text.TextMeasurer
//@Composable
//fun MinesweeperScreen(
//    modifier: Modifier = Modifier,
//    viewModel: MinesweeperModel = viewModel()
//) {
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
//        MinesweeperBoard(viewModel)
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
//}
//
//@Composable
//fun MinesweeperBoard(viewModel: MinesweeperModel) {
//    val backgroundImage = painterResource(id = R.drawable.dragon_ball)
//    val textMeasurer = rememberTextMeasurer()
//
//    Box(
//        modifier = Modifier
//            .aspectRatio(1f)
//            .fillMaxWidth(0.9f)
//    ) {
//        Image(
//            painter = backgroundImage,
//            contentDescription = stringResource(R.string.background_description),
//            modifier = Modifier.fillMaxSize()
//        )
//
//        MinesweeperCanvas(viewModel, textMeasurer)
//    }
//}
//
//@Composable
//fun MinesweeperCanvas(viewModel: MinesweeperModel, textMeasurer: TextMeasurer) {
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                detectTapGestures { offset ->
//                    val cellSize = size.width / 5
//                    val row = (offset.y / cellSize).toInt()
//                    val col = (offset.x / cellSize).toInt()
//                    viewModel.onCellClicked(row, col)
//                }
//            }
//    ) {
//        val cellSize = size.width / 5
//
//        // Draw grid lines
//        for (i in 1..4) {
//            drawLine(
//                Color.Black,
//                start = Offset(i * cellSize, 0f),
//                end = Offset(i * cellSize, size.height),
//                strokeWidth = 2f
//            )
//            drawLine(
//                Color.Black,
//                start = Offset(0f, i * cellSize),
//                end = Offset(size.width, i * cellSize),
//                strokeWidth = 2f
//            )
//        }
//
//        // Draw cells
//        for (row in viewModel.board.indices) {
//            for (col in viewModel.board[row].indices) {
//                val cell = viewModel.board[row][col]
//                val topLeft = Offset(col * cellSize, row * cellSize)
//
//                when (cell.state) {
//                    CellState.COVERED -> {
//                        drawRect(
//                            Color.Gray.copy(alpha = 0.5f),
//                            topLeft = topLeft,
//                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
//                        )
//                    }
//                    CellState.FLAGGED -> {
//                        drawRect(
//                            Color.Red.copy(alpha = 0.5f),
//                            topLeft = topLeft,
//                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
//                        )
//                        // Draw a simple flag
//                        drawLine(
//                            Color.Black,
//                            start = topLeft + Offset(cellSize * 0.2f, cellSize * 0.2f),
//                            end = topLeft + Offset(cellSize * 0.2f, cellSize * 0.8f),
//                            strokeWidth = 2f
//                        )
//                        drawLine(
//                            Color.Black,
//                            start = topLeft + Offset(cellSize * 0.2f, cellSize * 0.2f),
//                            end = topLeft + Offset(cellSize * 0.8f, cellSize * 0.4f),
//                            strokeWidth = 2f
//                        )
//                    }
//                    CellState.UNCOVERED -> {
//                        if (cell.hasMine) {
//                            // Draw a simple mine
//                            drawCircle(
//                                Color.Black,
//                                radius = cellSize * 0.3f,
//                                center = topLeft + Offset(cellSize * 0.5f, cellSize * 0.5f)
//                            )
//                        } else if (cell.neighboringMines > 0) {
//                            // Draw the number of neighboring mines
//                            drawText(
//                                textMeasurer = textMeasurer,
//                                text = cell.neighboringMines.toString(),
//                                topLeft = topLeft + Offset(cellSize * 0.3f, cellSize * 0.3f)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
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
