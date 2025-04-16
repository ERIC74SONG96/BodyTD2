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
    speed = 150f,
    damage = 3f
) {
    override val type: EnemyType = EnemyType.BACTERIA

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Effet de pulsation plus rapide
        val pulseScale = 1f + 0.12f * sin(animationProgress * 4 * PI.toFloat())

        // Effet de brillance plus intense
        val glowPaint = Paint(paint).apply {
            color = Color.argb(150, 0, 255, 0)
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(position.x, position.y, 20f * pulseScale, glowPaint)

        // Corps principal
        paint.color = Color.rgb(0, 255, 0)
        canvas.drawCircle(position.x, position.y, 18f * pulseScale, paint)

        // Détails plus visibles
        paint.color = Color.WHITE
        canvas.drawCircle(position.x - 6f * pulseScale, position.y - 6f * pulseScale, 4f * pulseScale, paint)
        canvas.drawCircle(position.x + 6f * pulseScale, position.y + 6f * pulseScale, 4f * pulseScale, paint)
    }

    override fun getEnemyRadius(): Float = 18f
}
