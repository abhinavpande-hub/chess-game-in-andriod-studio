package com.example.chess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectedRow = -1
    private var selectedCol = -1
    
    var onMoveListener: ((String) -> Unit)? = null
    private var isWhiteTurn = true

    private val lightSquareColor = Color.parseColor("#F0D9B5")
    private val darkSquareColor = Color.parseColor("#B58863")
    private val highlightColor = Color.YELLOW

    private var pieces = Array(8) { row ->
        when (row) {
            0 -> charArrayOf('♜', '♞', '♝', '♛', '♚', '♝', '♞', '♜')
            1 -> charArrayOf('♟', '♟', '♟', '♟', '♟', '♟', '♟', '♟')
            6 -> charArrayOf('♙', '♙', '♙', '♙', '♙', '♙', '♙', '♙')
            7 -> charArrayOf('♖', '♘', '♗', '♕', '♔', '♗', '♘', '♖')
            else -> charArrayOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ')
        }
    }

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width > 0 && height > 0) Math.min(width, height) else width.coerceAtLeast(height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val squareSize = width / 8f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = squareSize * 0.8f

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                // Draw Square
                paint.color = if ((row + col) % 2 == 0) lightSquareColor else darkSquareColor
                if (row == selectedRow && col == selectedCol) paint.color = highlightColor
                
                canvas.drawRect(
                    col * squareSize, 
                    row * squareSize, 
                    (col + 1) * squareSize, 
                    (row + 1) * squareSize, 
                    paint
                )

                // Draw Piece
                val piece = pieces[row][col]
                if (piece != ' ') {
                    paint.color = Color.BLACK
                    // Centering text vertically
                    val x = col * squareSize + squareSize / 2f
                    val y = row * squareSize + squareSize / 2f - (paint.descent() + paint.ascent()) / 2f
                    canvas.drawText(piece.toString(), x, y, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        
        if (event.action == MotionEvent.ACTION_DOWN) {
            val squareSize = width / 8f
            val col = (event.x / squareSize).toInt()
            val row = (event.y / squareSize).toInt()

            if (row in 0..7 && col in 0..7) {
                handleTouch(row, col)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouch(row: Int, col: Int) {
        if (selectedRow == -1) {
            val piece = pieces[row][col]
            if (piece != ' ') {
                // White pieces are Unicode 0x2654 to 0x2659
                val isPieceWhite = piece.code in 0x2654..0x2659
                if (isPieceWhite == isWhiteTurn) {
                    selectedRow = row
                    selectedCol = col
                }
            }
        } else {
            if (selectedRow == row && selectedCol == col) {
                selectedRow = -1
                selectedCol = -1
            } else {
                // Move piece
                pieces[row][col] = pieces[selectedRow][selectedCol]
                pieces[selectedRow][selectedCol] = ' '
                selectedRow = -1
                selectedCol = -1
                isWhiteTurn = !isWhiteTurn
                onMoveListener?.invoke(if (isWhiteTurn) "White's Turn" else "Black's Turn")
            }
        }
        invalidate()
    }

    fun resetBoard() {
        pieces = Array(8) { row ->
            when (row) {
                0 -> charArrayOf('♜', '♞', '♝', '♛', '♚', '♝', '♞', '♜')
                1 -> charArrayOf('♟', '♟', '♟', '♟', '♟', '♟', '♟', '♟')
                6 -> charArrayOf('♙', '♙', '♙', '♙', '♙', '♙', '♙', '♙')
                7 -> charArrayOf('♖', '♘', '♗', '♕', '♔', '♗', '♘', '♖')
                else -> charArrayOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ')
            }
        }
        selectedRow = -1
        selectedCol = -1
        isWhiteTurn = true
        invalidate()
    }
}
