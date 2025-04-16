package com.example.myapplicationbodytd.towers

import android.graphics.Color
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.towers.Projectile
import com.example.myapplicationbodytd.towers.Tower
import com.example.myapplicationbodytd.towers.TowerType
import com.example.myapplicationbodytd.towers.strategies.MultiTargetStrategy

class RapidTower(position: PointF) : Tower(
    position = position,
    level = 1
) {
    override val type: TowerType = TowerType.RAPID
    override val range: Float = 150f
    override val damage: Float = 10f
    override val attackSpeed: Float = 5.0f
    override val upgradeCost: Int = 150
    override val maxLevel: Int = 3

    private var animationProgress = 0f
    private val attackStrategy = MultiTargetStrategy()

    override fun update(currentTime: Long, enemies: List<Enemy>) {
        super.update(currentTime, enemies)

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * 6) % 1f
    }

    override fun attack(enemy: Enemy) {
        if (enemy.position != null) {
            val projectile = Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = enemy.position,
                speed = 2000f,
                damage = damage,
                color = Color.RED
            )
            projectiles.add(projectile)
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 2000f,
            damage = damage,
            color = Color.RED
        )
    }
}
