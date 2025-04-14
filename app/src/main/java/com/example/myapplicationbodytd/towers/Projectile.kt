package com.example.myapplicationbodytd.towers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.BlurMaskFilter
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

class Projectile(
    private val startPosition: PointF,
    private val targetPosition: PointF,
    private val speed: Float,
    val damage: Float,
    private val color: Int
) {
    private var currentPosition = PointF(startPosition.x, startPosition.y)
    private var _isActive = true
    val isActive: Boolean
        get() = _isActive
    private var distanceTraveled = 0f
    private val maxDistance = calculateDistance(startPosition, targetPosition)
    private var animationProgress = 0f

    private val direction = PointF(
        (targetPosition.x - startPosition.x) / maxDistance,
        (targetPosition.y - startPosition.y) / maxDistance
    )

    fun update(deltaTime: Float, enemies: List<Enemy>): Enemy? {
        if (!_isActive) return null

        // Chercher l'ennemi le plus proche de la position cible
        var closestEnemy: Enemy? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            if (enemy.position != null) {
                val distance = calculateDistance(targetPosition, enemy.position)
                if (distance < minDistance) {
                    minDistance = distance
                    closestEnemy = enemy
                }
            }
        }

        // Si un ennemi est trouvé dans un rayon raisonnable, on le touche directement
        if (closestEnemy != null && minDistance < 100f) { // 100f = rayon max de détection
            _isActive = false
            return closestEnemy
        }

        // Si aucun ennemi à portée, désactiver le projectile
        _isActive = false
        return null
    }


    private fun checkCollision(enemy: Enemy): Boolean {
        if (enemy.position == null) return false
        val distance = calculateDistance(currentPosition, enemy.position)
        return distance < 20f // Rayon de collision
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!_isActive) return

        // Effet de pulsation
        val pulseScale = 1f + 0.2f * sin(animationProgress * 2 * PI.toFloat())

        // Effet de traînée
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        // Traînée principale
        paint.color = Color.argb(150, r, g, b)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(currentPosition.x, currentPosition.y, 15f * pulseScale, paint)

        // Effet de brillance (sans BlurMaskFilter)
        paint.color = Color.argb(100, 255, 255, 255)
        canvas.drawCircle(currentPosition.x, currentPosition.y, 20f * pulseScale, paint)

        // Projectile principal
        paint.color = color
        canvas.drawCircle(currentPosition.x, currentPosition.y, 10f * pulseScale, paint)

        // Effet de particules
        paint.color = Color.argb(200, 255, 255, 255)
        val particleCount = 4
        for (i in 0 until particleCount) {
            val angle = (i * 2 * PI / particleCount).toFloat()
            val distance = 5f * pulseScale
            val particleX = currentPosition.x + cos(angle) * distance
            val particleY = currentPosition.y + sin(angle) * distance
            canvas.drawCircle(particleX, particleY, 3f * pulseScale, paint)
        }
    }

    private fun calculateDistance(start: PointF, end: PointF): Float {
        return sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y))
    }
}