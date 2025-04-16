package com.example.myapplicationbodytd.towers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Projectile(
    startPosition: PointF,
    val damage: Float,
    private val color: Int
) {
    private var currentPosition = PointF(startPosition.x, startPosition.y)
    private var _isActive = true
    val isActive: Boolean
        get() = _isActive
    private var animationProgress = 0f

    fun update(enemies: List<Enemy>): Enemy? {
        if (!_isActive) return null

        // Chercher l'ennemi le plus proche
        var closestEnemy: Enemy? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            val distance = calculateDistance(currentPosition, enemy.position) // Calculer la distance vers l'ennemi
            if (distance < minDistance) {
                minDistance = distance
                closestEnemy = enemy
            }
        }

        // Si un ennemi est trouvé et qu'il est assez proche, déplacer le projectile vers l'ennemi
        if (closestEnemy != null) {
            // Vecteur directionnel du projectile vers l'ennemi
            val direction = PointF(
                closestEnemy.position.x - currentPosition.x,
                closestEnemy.position.y - currentPosition.y
            )

            // Calculer la distance entre le projectile et l'ennemi
            val distanceToEnemy = calculateDistance(currentPosition, closestEnemy.position)

            // Eviter la division par zéro
            if (distanceToEnemy > 0) {
                // Normaliser la direction
                direction.x /= distanceToEnemy
                direction.y /= distanceToEnemy
            }

            // Déplacer le projectile dans la direction de l'ennemi (vitesse ajustée)
            val speed = 100f // Vous pouvez ajuster la vitesse à 50f ou à une valeur plus raisonnable
            currentPosition.x += direction.x * speed
            currentPosition.y += direction.y * speed

            // Vérifier si le projectile touche l'ennemi (si la distance est inférieure à un seuil)
            if (minDistance < 75f) { // Seuil de collision
                _isActive = false
                return closestEnemy // Retourner l'ennemi touché
            }
        }

        return null
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