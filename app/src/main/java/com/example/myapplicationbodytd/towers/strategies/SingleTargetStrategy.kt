package com.example.myapplicationbodytd.towers.strategies

import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.sqrt

class SingleTargetStrategy : AttackStrategy {
    override fun selectTargets(towerPosition: PointF, range: Float, enemies: List<Enemy>): List<Enemy> {
        var closestEnemy: Enemy? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            val distance = calculateDistance(towerPosition, enemy.position)
            if (distance <= range && distance < minDistance) {
                minDistance = distance
                closestEnemy = enemy
            }
        }

        return if (closestEnemy != null) listOf(closestEnemy) else emptyList()
    }

    override fun calculateDamageMultiplier(enemy: Enemy): Float {
        // Bonus de dégâts contre les ennemis à faible santé
        return if (enemy.health / enemy.maxHealth < 0.3f) 1.5f else 1f
    }

    override fun getProjectileColor(): Int = android.graphics.Color.rgb(255, 100, 100)

    override fun getProjectileSpeed(): Float = 800f

    private fun calculateDistance(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx * dx + dy * dy)
    }
} 