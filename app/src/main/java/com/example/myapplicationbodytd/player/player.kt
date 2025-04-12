package com.example.myapplicationbodytd.player

data class PlayerStats(
    val highScore: Int,
    val totalGamesPlayed: Int,
    val totalEnemiesKilled: Int,
    val totalTowersBuilt: Int
)

class Player {
    private var highScore = 0
    private var totalGamesPlayed = 0
    private var totalEnemiesKilled = 0
    private var totalTowersBuilt = 0

    fun updateHighScore(score: Int) {
        if (score > highScore) {
            highScore = score
        }
    }

    fun incrementGamesPlayed() {
        totalGamesPlayed++
    }

    fun incrementEnemiesKilled() {
        totalEnemiesKilled++
    }

    fun incrementTowersBuilt() {
        totalTowersBuilt++
    }

    fun getStats(): PlayerStats {
        return PlayerStats(
            highScore = highScore,
            totalGamesPlayed = totalGamesPlayed,
            totalEnemiesKilled = totalEnemiesKilled,
            totalTowersBuilt = totalTowersBuilt
        )
    }
}
