package com.example.myapplicationbodytd.managers

import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Bacteria
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.enemies.Parasite
import com.example.myapplicationbodytd.enemies.Virus

class WaveManager {
    private var currentWave = 0
    private val enemiesToSpawn = mutableListOf<Enemy>()
    private var spawnTimer = 0f
    private val spawnInterval = 0.7f

    fun startNextWave(spawnPoint: PointF) {
        // Ne pas dépasser la vague 5
        if (currentWave >= 5) {
            return
        }
        
        currentWave++
        enemiesToSpawn.clear()
        
        val numberOfEnemies = calculateEnemiesForWave()
        
        // Distribution des ennemis selon la vague
        when {
            currentWave <= 2 -> {
                // Vagues 1-2: Uniquement des Virus
                repeat(numberOfEnemies) {
                    val enemy = Virus(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(enemy)
                }
            }
            currentWave <= 4 -> {
                // Vagues 3-4: Virus et Bactéries
                repeat(numberOfEnemies / 2) {
                    val virus = Virus(PointF(spawnPoint.x, spawnPoint.y))
                    val bacteria = Bacteria(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(virus)
                    enemiesToSpawn.add(bacteria)
                }
                // Ajouter un ennemi supplémentaire si le nombre est impair
                if (numberOfEnemies % 2 != 0) {
                    val virus = Virus(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(virus)
                }
            }
            currentWave == 5 -> {
                // Vague 5: Tous les types d'ennemis
                val baseCount = numberOfEnemies / 3
                repeat(baseCount) {
                    val virus = Virus(PointF(spawnPoint.x, spawnPoint.y))
                    val bacteria = Bacteria(PointF(spawnPoint.x, spawnPoint.y))
                    val parasite = Parasite(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(virus)
                    enemiesToSpawn.add(bacteria)
                    enemiesToSpawn.add(parasite)
                }
                // Ajouter les ennemis restants
                val remainder = numberOfEnemies % 3
                if (remainder >= 1) {
                    val virus = Virus(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(virus)
                }
                if (remainder >= 2) {
                    val bacteria = Bacteria(PointF(spawnPoint.x, spawnPoint.y))
                    enemiesToSpawn.add(bacteria)
                }
            }
        }
    }

    fun update(deltaTime: Float): List<Enemy> {
        val spawnedEnemies = mutableListOf<Enemy>()
        
        if (enemiesToSpawn.isNotEmpty()) {
            spawnTimer += deltaTime
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                enemiesToSpawn.firstOrNull()?.let { enemy ->
                    spawnedEnemies.add(enemy)
                    enemiesToSpawn.removeAt(0)
                }
            }
        }
        
        return spawnedEnemies
    }

    fun isWaveComplete(): Boolean = enemiesToSpawn.isEmpty()

    fun getCurrentWave(): Int = currentWave

    private fun calculateEnemiesForWave(): Int {
        return 12 + currentWave * 4
    }

    fun reset() {
        currentWave = 0
        enemiesToSpawn.clear()
        spawnTimer = 0f
    }
}
