package com.example.chess

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var board: ChessBoardView
    private lateinit var status: TextView
    private lateinit var restart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        board = findViewById(R.id.boardView)
        status = findViewById(R.id.statusTextView)
        restart = findViewById(R.id.restartButton)

        board.onMoveListener = { newStatus ->
            status.text = newStatus
        }

        restart.setOnClickListener {
            status.text = "White's Turn"
            board.resetBoard()
        }
    }
}
