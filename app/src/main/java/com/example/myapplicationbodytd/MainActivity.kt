package com.example.myapplicationbodytd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationbodytd.managers.GameManager

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        gameView = findViewById(R.id.gameView)

        // Initialisation du gestionnaire de jeu
        gameManager = GameManager.getInstance(this)

        // Démarrer le jeu
        gameManager.startGame()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.cleanup()
        gameManager.release()
    }
}
