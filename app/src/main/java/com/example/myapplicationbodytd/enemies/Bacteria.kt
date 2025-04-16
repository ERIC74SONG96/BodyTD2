package com.example.myapplicationbodytd.enemies

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.sin

class Bacteria(position: PointF) : Enemy(
    position = position,
    health = 100f,
    maxHealth = 100f,
    speed = 100f,
    damage = 2f,
    reward = 20
) {
    override val type: EnemyType = EnemyType.BACTERIA

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Effet de pulsation
        val pulseScale = 1f + 0.08f * sin(animationProgress * 3 * PI.toFloat())

        // Effet de brillance
        val glowPaint = Paint(paint).apply {
            color = Color.argb(100, 0, 255, 0)
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(position.x, position.y, 17f * pulseScale, glowPaint)

        // Corps principal
        paint.color = Color.rgb(0, 255, 0)
        canvas.drawCircle(position.x, position.y, 15f * pulseScale, paint)

        // Détails
        paint.color = Color.WHITE
        canvas.drawCircle(position.x - 5f * pulseScale, position.y - 5f * pulseScale, 3f * pulseScale, paint)
        canvas.drawCircle(position.x + 5f * pulseScale, position.y + 5f * pulseScale, 3f * pulseScale, paint)
    }

    override fun getEnemyRadius(): Float = 15f
}
