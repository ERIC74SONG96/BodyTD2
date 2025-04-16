package com.example.myapplicationbodytd.towers

import android.graphics.Color
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy

class RapidTower(position: PointF) : Tower(position) {
    override val type = TowerType.RAPID
    override val range = 150f
    override val damage = 10f
    override val attackSpeed = 5.0f
    override val upgradeCost = 150
    override val maxLevel = 3

    override fun attack(target: Enemy) {
        target.position.let {
            projectiles.add(
                Projectile(
                    startPosition = PointF(position.x, position.y),
                    damage = damage,
                    color = Color.RED
                )
            )
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            damage = damage,
            color = Color.RED
        )
    }
}
