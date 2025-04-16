package com.example.myapplicationbodytd.enemies


import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.enemies.EnemyType
import kotlin.math.PI
import kotlin.math.sin

class Virus(position: PointF) : Enemy(
    position = position,
    health = 50f,
    maxHealth = 50f,
    speed = 150f,
    damage = 1f,
    reward = 10
) {
    override val type: EnemyType = EnemyType.VIRUS

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Effet de pulsation
        val pulseScale = 1f + 0.1f * sin(animationProgress * 4 * PI.toFloat())

        // Effet de brillance
        val glowPaint = Paint(paint).apply {
            color = Color.argb(100, 255, 0, 0)
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(position.x, position.y, 12f * pulseScale, glowPaint)

        // Corps principal
        paint.color = Color.rgb(255, 0, 0)
        canvas.drawCircle(position.x, position.y, 10f * pulseScale, paint)

        // Détails
        paint.color = Color.WHITE
        canvas.drawCircle(position.x, position.y, 3f * pulseScale, paint)
    }

    override fun getEnemyRadius(): Float = 10f
}