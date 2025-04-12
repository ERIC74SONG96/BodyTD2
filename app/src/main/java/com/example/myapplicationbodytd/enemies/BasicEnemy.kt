package com.example.myapplicationbodytd.enemies

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class BasicEnemy(
    position: PointF,
    health: Float,
    maxHealth: Float = 100f,
    speed: Float = 2f,
    damage: Float = 1f
) : Enemy(position, health, maxHealth, speed, damage, 10) {
    
    override val type: EnemyType = EnemyType.BASIC

    override fun getEnemyRadius(): Float = 20f

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Dessiner l'ennemi
        paint.color = Color.RED
        canvas.drawCircle(position.x, position.y, getEnemyRadius(), paint)

        // Dessiner la barre de vie
        val healthBarWidth = 40f
        val healthBarHeight = 5f
        val healthPercentage = health / maxHealth

        // Fond de la barre de vie
        paint.color = Color.GRAY
        canvas.drawRect(
            position.x - healthBarWidth / 2,
            position.y - 30f,
            position.x + healthBarWidth / 2,
            position.y - 30f + healthBarHeight,
            paint
        )

        // Barre de vie actuelle
        paint.color = Color.GREEN
        canvas.drawRect(
            position.x - healthBarWidth / 2,
            position.y - 30f,
            position.x - healthBarWidth / 2 + healthBarWidth * healthPercentage,
            position.y - 30f + healthBarHeight,
            paint
        )
    }
} 