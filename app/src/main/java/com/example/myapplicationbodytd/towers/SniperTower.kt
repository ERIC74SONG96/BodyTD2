package com.example.myapplicationbodytd.towers

import android.graphics.Color
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy

class SniperTower(position: PointF) : Tower(position) {
    override val type = TowerType.SNIPER
    override val range = 400f
    override val damage = 50f
    override val attackSpeed = 0.5f
    override val upgradeCost = 200
    override val maxLevel = 3

    override fun attack(target: Enemy) {
        target.position.let { pos ->
            projectiles.add(Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = pos,
                speed = 2500f,
                damage = damage,
                color = Color.GREEN
            ))
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 2500f,
            damage = damage,
            color = Color.GREEN
        )
    }
}
