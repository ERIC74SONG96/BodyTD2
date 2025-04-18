package com.example.myapplicationbodytd.enemies

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class Virus(position: PointF) : Enemy(
    position = position,
    health = 50f,
    maxHealth = 50f,
    speed = 300f,
    damage = 2f
) {
    override val type: EnemyType = EnemyType.VIRUS

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Définir la couleur de l'ennemi
        paint.color = Color.rgb(255, 0, 0)
        paint.style = Paint.Style.FILL

        // Dessiner le cercle représentant l'ennemi
        canvas.drawCircle(position.x, position.y, getEnemyRadius(), paint)
    }

    override fun getEnemyRadius(): Float = 12f
}