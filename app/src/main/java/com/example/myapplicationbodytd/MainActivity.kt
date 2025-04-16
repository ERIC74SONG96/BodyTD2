package com.example.myapplicationbodytd

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationbodytd.managers.GameManager

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        gameView = GameView(this)
        setContentView(gameView)

        // Initialisation du gestionnaire de jeu
        gameManager = GameManager.getInstance(this)

        // Démarrer le jeu
        gameManager.startGame()
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.cleanup()
        gameManager.release()
    }
}
