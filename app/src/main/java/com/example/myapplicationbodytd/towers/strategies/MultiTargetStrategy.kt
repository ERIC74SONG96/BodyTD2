package com.example.myapplicationbodytd.towers.strategies

import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.sqrt

class MultiTargetStrategy : AttackStrategy {
    override fun selectTargets(towerPosition: PointF, range: Float, enemies: List<Enemy>): List<Enemy> {
        return enemies.filter { enemy ->
            calculateDistance(towerPosition, enemy.position) <= range
        }.sortedBy { enemy ->
            calculateDistance(towerPosition, enemy.position)
        }.take(3) // Limite à 3 cibles maximum
    }

    override fun calculateDamageMultiplier(enemy: Enemy): Float {
        // Réduction des dégâts pour chaque cible supplémentaire
        return 0.7f
    }

    override fun getProjectileColor(): Int = android.graphics.Color.rgb(100, 200, 255)

    override fun getProjectileSpeed(): Float = 500f

    private fun calculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx * dx + dy * dy)
    }
} 