package hu.ait.tictactoe.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel


enum class Player{
    X, O
}

data class BoardCell(val row: Int, val col: Int)

class TicTacToeModel : ViewModel() {

    var board by mutableStateOf(
        Array(3) { Array(3) { null as Player? } })

    /*init {
        board[0][0] = Player.X
    }*/

    var currentPlayer by mutableStateOf(Player.X)
    // Winner of the game (null if no winner yet)
    var winner by mutableStateOf<Player?>(null)

    // Flag to indicate if the game is over
    var isGameOver by mutableStateOf(false)

    fun onCellClicked(cell: BoardCell) {
        // Only allow moves if the cell is empty and the game is not over
        if (board[cell.row][cell.col] == null && !isGameOver) {
            // Place the current player's mark on the board
            board[cell.row][cell.col] = currentPlayer

            // Check for a winner
            if (checkForWinner()) {
                winner = currentPlayer
                isGameOver = true
            } else if (isBoardFull()) {
                // If no winner and the board is full, it's a draw
                isGameOver = true
            } else {
                // Switch to the other player
                currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
            }
        }
    }

    // Check if there's a winner
    private fun checkForWinner(): Boolean {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != null && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return true
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[0][i] != null && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return true
            }
        }

        // Check diagonals
        if (board[0][0] != null && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return true
        }
        if (board[0][2] != null && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return true
        }

        return false
    }

    // Check if the board is full (draw condition)
    private fun isBoardFull(): Boolean {
        for (row in board) {
            for (cell in row) {
                if (cell == null) {
                    return false
                }
            }
        }
        return true
    }


//    fun onCellClicked(cell: BoardCell) {
//        if (board[cell.row][cell.col] == null) {
//            // VERSION 1 for updating the state: (slower)
//            //val newBoard = board.copyOf()
//            //newBoard[cell.row][cell.col] = currentPlayer
//            //board = newBoard
//
//            // better version with state hoisting in the composable
//            board[cell.row][cell.col] = currentPlayer
//            currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
//        }
//    }

    fun resetGame() {
        board = Array(3) { Array(3) { null as Player? } }
        currentPlayer = Player.X
        winner = null
        isGameOver = false
    }


}