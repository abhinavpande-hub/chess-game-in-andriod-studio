package com.example.chessgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val light = Paint().apply { color = Color.parseColor("#F0D9B5") }
    private val dark = Paint().apply { color = Color.parseColor("#B58863") }
    private val piecePaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val board = Array(8) { Array(8) { "" } }

    private var selectedRow = -1
    private var selectedCol = -1
    private var whiteTurn = true

    init {
        resetBoard()
    }

    fun resetBoard() {
        val black = arrayOf("♜","♞","♝","♛","♚","♝","♞","♜")
        val white = arrayOf("♖","♘","♗","♕","♔","♗","♘","♖")

        for (i in 0..7) {
            board[0][i] = black[i]
            board[1][i] = "♟"
            board[6][i] = "♙"
            board[7][i] = white[i]
        }

        for (r in 2..5)
            for (c in 0..7)
                board[r][c] = ""

        whiteTurn = true
        selectedRow = -1
        selectedCol = -1
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val cell = size / 8f

        piecePaint.textSize = cell * 0.7f

        for (r in 0..7) {
            for (c in 0..7) {

                val left = c * cell
                val top = r * cell

                canvas.drawRect(
                    left,
                    top,
                    left + cell,
                    top + cell,
                    if ((r + c) % 2 == 0) light else dark
                )

                if (r == selectedRow && c == selectedCol) {
                    val p = Paint().apply {
                        color = Color.YELLOW
                        alpha = 120
                    }
                    canvas.drawRect(left, top, left + cell, top + cell, p)
                }

                val piece = board[r][c]
                if (piece.isNotEmpty()) {
                    piecePaint.color =
                        if (piece.first() in listOf('♔','♕','♖','♗','♘','♙'))
                            Color.WHITE
                        else
                            Color.BLACK

                    canvas.drawText(
                        piece,
                        left + cell / 2,
                        top + cell * 0.72f,
                        piecePaint
                    )
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_DOWN)
            return true

        val cell = min(width, height) / 8f
        val row = (event.y / cell).toInt()
        val col = (event.x / cell).toInt()

        if (row !in 0..7 || col !in 0..7)
            return true

        if (selectedRow == -1) {
            val piece = board[row][col]
            if (piece.isNotEmpty() && isCurrentPlayerPiece(piece)) {
                selectedRow = row
                selectedCol = col
                invalidate()
            }
        } else {

            if (isValidMove(selectedRow, selectedCol, row, col)) {

                board[row][col] = board[selectedRow][selectedCol]
                board[selectedRow][selectedCol] = ""

                whiteTurn = !whiteTurn
            }

            selectedRow = -1
            selectedCol = -1
            invalidate()
        }

        return true
    }

    private fun isCurrentPlayerPiece(piece: String): Boolean {
        val whitePiece = piece.first() in listOf('♔','♕','♖','♗','♘','♙')
        return whitePiece == whiteTurn
    }

    private fun isEnemy(sr:Int, sc:Int, tr:Int, tc:Int): Boolean {
        if (board[tr][tc].isEmpty()) return false
        return isCurrentPlayerPiece(board[sr][sc]) != isCurrentPlayerPiece(board[tr][tc])
    }

    private fun clearStraight(sr:Int, sc:Int, tr:Int, tc:Int): Boolean {

        if (sr == tr) {
            val step = if (tc > sc) 1 else -1
            var c = sc + step
            while (c != tc) {
                if (board[sr][c].isNotEmpty()) return false
                c += step
            }
        } else {
            val step = if (tr > sr) 1 else -1
            var r = sr + step
            while (r != tr) {
                if (board[r][sc].isNotEmpty()) return false
                r += step
            }
        }

        return true
    }

    private fun clearDiagonal(sr:Int, sc:Int, tr:Int, tc:Int): Boolean {

        val rStep = if (tr > sr) 1 else -1
        val cStep = if (tc > sc) 1 else -1

        var r = sr + rStep
        var c = sc + cStep

        while (r != tr && c != tc) {
            if (board[r][c].isNotEmpty()) return false
            r += rStep
            c += cStep
        }

        return true
    }

    private fun isValidMove(sr:Int, sc:Int, tr:Int, tc:Int): Boolean {

        if (sr == tr && sc == tc) return false

        val piece = board[sr][sc]
        val dr = tr - sr
        val dc = tc - sc

        if (board[tr][tc].isNotEmpty() &&
            isCurrentPlayerPiece(board[tr][tc]) == isCurrentPlayerPiece(piece))
            return false

        return when(piece) {

            "♙" -> {
                dc == 0 && dr == -1 && board[tr][tc].isEmpty() ||
                sr == 6 && dc == 0 && dr == -2 &&
                        board[5][sc].isEmpty() &&
                        board[4][sc].isEmpty() ||
                kotlin.math.abs(dc)==1 && dr==-1 && isEnemy(sr,sc,tr,tc)
            }

            "♟" -> {
                dc == 0 && dr == 1 && board[tr][tc].isEmpty() ||
                sr == 1 && dc == 0 && dr == 2 &&
                        board[2][sc].isEmpty() &&
                        board[3][sc].isEmpty() ||
                kotlin.math.abs(dc)==1 && dr==1 && isEnemy(sr,sc,tr,tc)
            }

            "♖","♜" ->
                (sr==tr || sc==tc) && clearStraight(sr,sc,tr,tc)

            "♗","♝" ->
                kotlin.math.abs(dr)==kotlin.math.abs(dc) &&
                        clearDiagonal(sr,sc,tr,tc)

            "♕","♛" ->
                ((sr==tr || sc==tc) && clearStraight(sr,sc,tr,tc)) ||
                (kotlin.math.abs(dr)==kotlin.math.abs(dc) &&
                        clearDiagonal(sr,sc,tr,tc))

            "♘","♞" ->
                (kotlin.math.abs(dr)==2 && kotlin.math.abs(dc)==1) ||
                (kotlin.math.abs(dr)==1 && kotlin.math.abs(dc)==2)

            "♔","♚" ->
                kotlin.math.abs(dr)<=1 && kotlin.math.abs(dc)<=1

            else -> false
        }
    }
}
