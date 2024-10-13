package hu.ait.tictactoe.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

enum class CellState {
    COVERED, UNCOVERED, FLAGGED
}

data class Cell(
    val hasMine: Boolean = false,
    var state: CellState = CellState.COVERED,
    var neighboringMines: Int = 0
)

sealed class CellClickResult {
    object NoEffect : CellClickResult()
    data class NumberRevealed(val number: Int) : CellClickResult()
    data class GameOver(val message: String) : CellClickResult()
    object Victory : CellClickResult()
    data class CellsCleared(val count: Int) : CellClickResult()
}

class MinesweeperModel : ViewModel() {
    var board by mutableStateOf(Array(5) { Array(5) { Cell() } })
    var isGameOver by mutableStateOf(false)
    var isVictory by mutableStateOf(false)
    var isFlagMode by mutableStateOf(false)
    private var minesCount = 3

    init {
        initializeBoard()
    }

    private fun updateBoard(action: (Array<Array<Cell>>) -> Unit) {
        val newBoard = board.map { it.copyOf() }.toTypedArray()
        action(newBoard)
        board = newBoard
    }

    fun initializeBoard() {
        updateBoard { newBoard ->
            for (row in newBoard.indices) {
                for (col in newBoard[row].indices) {
                    newBoard[row][col] = Cell()
                }
            }
        }
        placeMines()
        calculateNeighboringMines()
        isGameOver = false
        isVictory = false
    }

    private fun placeMines() {
        var minesPlaced = 0
        while (minesPlaced < minesCount) {
            val row = Random.nextInt(5)
            val col = Random.nextInt(5)
            if (!board[row][col].hasMine) {
                updateBoard { newBoard ->
                    newBoard[row][col] = newBoard[row][col].copy(hasMine = true)
                }
                minesPlaced++
            }
        }
    }

    private fun calculateNeighboringMines() {
        updateBoard { newBoard ->
            for (row in newBoard.indices) {
                for (col in newBoard[row].indices) {
                    if (!newBoard[row][col].hasMine) {
                        newBoard[row][col] = newBoard[row][col].copy(neighboringMines = countNeighboringMines(row, col))
                    }
                }
            }
        }
    }

    private fun countNeighboringMines(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in board.indices && newCol in board[0].indices && board[newRow][newCol].hasMine) {
                    count++
                }
            }
        }
        return count
    }

    fun onCellClicked(row: Int, col: Int): CellClickResult {
        if (isGameOver) return CellClickResult.NoEffect

        return if (isFlagMode) {
            handleFlagMode(row, col)
        } else {
            handleTryMode(row, col)
        }
    }

    private fun handleFlagMode(row: Int, col: Int): CellClickResult {
        var result: CellClickResult = CellClickResult.NoEffect
        updateBoard { newBoard ->
            val cell = newBoard[row][col]
            when (cell.state) {
                CellState.COVERED -> {
                    newBoard[row][col] = cell.copy(state = CellState.FLAGGED)
                    if (!cell.hasMine) {
                        isGameOver = true
                        result = CellClickResult.GameOver("Game over! You flagged a safe cell.")
                    } else if (checkVictory(newBoard)) {
                        isGameOver = true
                        isVictory = true
                        result = CellClickResult.Victory
                    }
                }
                CellState.FLAGGED -> newBoard[row][col] = cell.copy(state = CellState.COVERED)
                CellState.UNCOVERED -> {} // Do nothing if already uncovered
            }
        }
        return result
    }

    private fun handleTryMode(row: Int, col: Int): CellClickResult {
        var result: CellClickResult = CellClickResult.NoEffect
        updateBoard { newBoard ->
            val cell = newBoard[row][col]
            if (cell.state != CellState.COVERED) return@updateBoard

            newBoard[row][col] = cell.copy(state = CellState.UNCOVERED)

            result = when {
                cell.hasMine -> {
                    isGameOver = true
                    CellClickResult.GameOver("Game over! You hit a mine.")
                }
                cell.neighboringMines > 0 -> CellClickResult.NumberRevealed(cell.neighboringMines)
                else -> {
                    val clearedCells = uncoverAdjacentCells(row, col, newBoard)
                    if (checkVictory(newBoard)) {
                        isGameOver = true
                        isVictory = true
                        CellClickResult.Victory
                    } else {
                        CellClickResult.CellsCleared(clearedCells)
                    }
                }
            }
        }
        return result
    }

    private fun uncoverAdjacentCells(row: Int, col: Int, board: Array<Array<Cell>>): Int {
        var clearedCells = 1
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in board.indices && newCol in board[0].indices) {
                    val adjacentCell = board[newRow][newCol]
                    if (adjacentCell.state == CellState.COVERED && !adjacentCell.hasMine) {
                        board[newRow][newCol] = adjacentCell.copy(state = CellState.UNCOVERED)
                        clearedCells++
                        if (adjacentCell.neighboringMines == 0) {
                            clearedCells += uncoverAdjacentCells(newRow, newCol, board)
                        }
                    }
                }
            }
        }
        return clearedCells
    }

    private fun checkVictory(board: Array<Array<Cell>>): Boolean {
        return board.all { row ->
            row.all { cell ->
                (cell.hasMine && (cell.state == CellState.COVERED || cell.state == CellState.FLAGGED)) ||
                        (!cell.hasMine && cell.state == CellState.UNCOVERED)
            }
        }
    }

    fun toggleFlagMode() {
        isFlagMode = !isFlagMode
    }

    fun resetGame() {
        initializeBoard()
    }
}


