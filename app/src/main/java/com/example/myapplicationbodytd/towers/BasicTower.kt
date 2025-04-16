package com.example.myapplicationbodytd.towers

import android.graphics.Color
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy

class BasicTower(position: PointF) : Tower(position) {
    override val type = TowerType.BASIC
    override val range = 200f
    override val damage = 20f
    override val attackSpeed = 1.5f
    override val upgradeCost = 100
    override val maxLevel = 3

    override fun attack(target: Enemy) {
        target.position.let { pos ->
            projectiles.add(Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = pos,
                speed = 1200f,
                damage = damage,
                color = Color.GRAY
            ))
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

