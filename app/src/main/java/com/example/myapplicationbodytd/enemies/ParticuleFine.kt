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
    speed = 300f,
    damage = 2f,
    reward = 20
) {
    override val type: EnemyType = EnemyType.VIRUS

    override fun drawEnemy(canvas: Canvas, paint: Paint) {
        // Effet de pulsation plus rapide
        val pulseScale = 1f + 0.15f * sin(animationProgress * 6 * PI.toFloat())

        // Effet de brillance plus intense
        val glowPaint = Paint(paint).apply {
            color = Color.argb(150, 255, 0, 0)
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(position.x, position.y, 15f * pulseScale, glowPaint)

        // Corps principal
        paint.color = Color.rgb(255, 0, 0)
        canvas.drawCircle(position.x, position.y, 12f * pulseScale, paint)

        // Détails plus visibles
        paint.color = Color.WHITE
        canvas.drawCircle(position.x, position.y, 4f * pulseScale, paint)
    }

    override fun getEnemyRadius(): Float = 12f
}