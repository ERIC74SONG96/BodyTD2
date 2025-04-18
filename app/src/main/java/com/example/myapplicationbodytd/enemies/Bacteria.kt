package com.example.myapplicationbodytd.enemies

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class Bacteria(position: PointF) : Enemy(
    position = position,
    health = 100f,
    maxHealth = 100f,
    speed = 150f,
    damage = 3f
) {
    override val type: EnemyType = EnemyType.BACTERIA

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Définir la couleur de l'ennemi
        paint.color = Color.rgb(0, 255, 0)
        paint.style = Paint.Style.FILL

        // Dessiner le cercle représentant l'ennemi
        canvas.drawCircle(position.x, position.y, getEnemyRadius(), paint)
    }


    override fun getEnemyRadius(): Float = 18f
}
