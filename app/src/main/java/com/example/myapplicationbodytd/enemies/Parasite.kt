package com.example.myapplicationbodytd.enemies

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Parasite(position: PointF) : Enemy(
    position = position,
    health = 200f,
    maxHealth = 200f,
    speed = 50f,
    damage = 3f
) {
    override val type: EnemyType = EnemyType.PARASITE

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Effet de pulsation
        val pulseScale = 1f + 0.05f * sin(animationProgress * 2 * PI.toFloat())

        // Effet de brillance
        val glowPaint = Paint(paint).apply {
            color = Color.argb(100, 128, 0, 128)
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(position.x, position.y, 22f * pulseScale, glowPaint)

        // Corps principal
        paint.color = Color.rgb(128, 0, 128)
        canvas.drawCircle(position.x, position.y, 20f * pulseScale, paint)

        // Détails
        paint.color = Color.WHITE
        canvas.drawCircle(position.x, position.y, 5f * pulseScale, paint)

        // Tentacules
        val numTentacles = 8
        for (i in 0 until numTentacles) {
            val angle = i * (2 * PI.toFloat() / numTentacles) + animationProgress * PI.toFloat()
            val tentacleLength = 10f * pulseScale
            val startX = position.x + cos(angle) * 15f * pulseScale
            val startY = position.y + sin(angle) * 15f * pulseScale
            val endX = position.x + cos(angle) * (15f + tentacleLength) * pulseScale
            val endY = position.y + sin(angle) * (15f + tentacleLength) * pulseScale

            paint.strokeWidth = 2f * pulseScale
            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }

    override fun getEnemyRadius(): Float = 20f
}
