package com.example.myapplicationbodytd

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationbodytd.managers.GameManager

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var gameManager: GameManager
    private lateinit var healthText: TextView
    private lateinit var moneyText: TextView
    private lateinit var waveText: TextView
    private lateinit var scoreText: TextView
    private var highScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        gameView = findViewById(R.id.gameView)
        healthText = findViewById(R.id.healthText)
        moneyText = findViewById(R.id.moneyText)
        waveText = findViewById(R.id.waveText)
        scoreText = findViewById(R.id.scoreText)

        // Initialisation du gestionnaire de jeu
        gameManager = GameManager.getInstance(this)

        // Configuration des callbacks
        setupGameCallbacks()

        // Initialiser l'affichage de la vague
        waveText.text = "Vague 1"

        // Démarrer le jeu
        gameManager.startGame()
    }

    private fun setupGameCallbacks() {
        gameManager.setOnMoneyChangedListener { money ->
            runOnUiThread {
                moneyText.text = "$money$"
                // Changer la couleur si l'argent est bas
                if (money < 50) {
                    moneyText.setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                } else {
                    moneyText.setTextColor(resources.getColor(android.R.color.white, theme))
                }
            }
        }

        gameManager.setOnHealthChangedListener { health ->
            runOnUiThread {
                healthText.text = "$health HP"
                // Changer la couleur si la santé est basse
                if (health < 30) {
                    healthText.setTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                } else if (health < 50) {
                    healthText.setTextColor(resources.getColor(android.R.color.holo_orange_light, theme))
                } else {
                    healthText.setTextColor(resources.getColor(android.R.color.white, theme))
                }
            }
        }

        gameManager.setOnWaveCompleteListener { wave ->
            runOnUiThread {
                // Afficher le numéro de vague correctement
                waveText.text = "Vague ${wave + 1}"
                // Mettre en évidence la nouvelle vague
                waveText.setTextColor(resources.getColor(android.R.color.holo_green_light, theme))
                // Revenir à la couleur normale après un délai
                waveText.postDelayed({
                    waveText.setTextColor(resources.getColor(android.R.color.white, theme))
                }, 1000)
            }
        }

        gameManager.setOnScoreChangedListener { score ->
            runOnUiThread {
                scoreText.text = "Score: $score"
                // Mise à jour du high score
                if (score > highScore) {
                    highScore = score
                    scoreText.setTextColor(resources.getColor(android.R.color.holo_green_light, theme))
                    // Revenir à la couleur normale après un délai
                    scoreText.postDelayed({
                        scoreText.setTextColor(resources.getColor(android.R.color.white, theme))
                    }, 1000)
                }
            }
        }

        gameManager.setOnGameOverListener {
            runOnUiThread {
                // Afficher le score final et le high score
                val finalScore = gameManager.getScore()
                scoreText.text = "Score Final: $finalScore (Meilleur: $highScore)"
                scoreText.setTextColor(resources.getColor(android.R.color.white, theme))
            }
        }
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
