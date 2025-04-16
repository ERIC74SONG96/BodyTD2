package com.example.myapplicationbodytd.towers

import android.graphics.Color
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.towers.Projectile
import com.example.myapplicationbodytd.towers.Tower
import com.example.myapplicationbodytd.towers.TowerType

class BasicTower(position: PointF) : Tower(
    position = position,
    level = 1
) {
    override val type: TowerType = TowerType.BASIC
    override val range: Float = 200f
    override val damage: Float = 50f
    override val attackSpeed: Float = 1.5f
    override val upgradeCost: Int = 100
    override val maxLevel: Int = 3

    private var animationProgress = 0f

    override fun update(currentTime: Long, enemies: List<Enemy>) {
        super.update(currentTime, enemies)

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * 2) % 1f
    }

    override fun attack(enemy: Enemy) {
        if (enemy.position != null) {
            val projectile = Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = enemy.position,
                speed = 1200f,
                damage = damage,
                color = Color.GRAY
            )
            projectiles.add(projectile)
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 1200f,
            damage = damage,
            color = Color.GRAY
        )
    }
}
