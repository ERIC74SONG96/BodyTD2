package com.example.myapplicationbodytd.enemies

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import kotlin.math.hypot

abstract class Enemy(
    var position: PointF,
    var health: Float,
    var maxHealth: Float,
    var speed: Float,
    var damage: Float
) {
    abstract val type: EnemyType
    private var currentWaypointIndex = 0
    private var progress = 0f
    private var isDead = false
    private var isReachedEnd = false
    private var animationProgress = 0f
    private var damageAnimationProgress = 0f
    private var lastDamageTime = 0f
    private var deathAnimationProgress = 0f
    private var isDying = false

    fun update(waypoints: List<PointF>, deltaTime: Float): Boolean {
        if (isDead || isReachedEnd) return false

        // Mise à jour de l'animation
        animationProgress = (animationProgress + deltaTime * speed * 0.01f) % 1f
        
        // Mise à jour de l'animation de dégâts
        if (damageAnimationProgress > 0) {
            damageAnimationProgress = (damageAnimationProgress - deltaTime * 2f).coerceAtLeast(0f)
        }
        
        // Mise à jour de l'animation de mort
        if (isDying) {
            deathAnimationProgress = (deathAnimationProgress + deltaTime * 2f).coerceAtMost(1f)
            if (deathAnimationProgress >= 1f) {
                isDead = true
            }
        }

        if (currentWaypointIndex >= waypoints.size - 1) {
            isReachedEnd = true
            return true
        }

        val currentPoint = waypoints[currentWaypointIndex]
        val nextPoint = waypoints[currentWaypointIndex + 1]
        
        progress += speed * deltaTime / calculateDistance(currentPoint, nextPoint)
        
        if (progress >= 1f) {
            progress = 0f
            currentWaypointIndex++
            if (currentWaypointIndex >= waypoints.size - 1) {
                isReachedEnd = true
                return true
            }
        } else {
            position.x = currentPoint.x + (nextPoint.x - currentPoint.x) * progress
            position.y = currentPoint.y + (nextPoint.y - currentPoint.y) * progress
        }

        return false
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (isDead) return
        
        // Effet de mort
        if (isDying) {
            drawDeathEffect(canvas, paint)
            return
        }
        
        // Effet de dégâts
        if (damageAnimationProgress > 0) {
            drawDamageEffect(canvas, paint)
        }
        
        // Dessiner la barre de vie
        drawHealthBar(canvas, paint)

        // Dessiner l'ennemi
        drawEnemy(canvas, paint)
    }

    private fun drawHealthBar(canvas: Canvas, paint: Paint) {
        val healthBarWidth = 30f
        val healthPercentage = health / maxHealth

        // Fond de la barre de vie
        paint.color = Color.argb(150, 0, 0, 0)
        paint.style = Paint.Style.FILL
        canvas.drawRect(
            position.x - healthBarWidth/2,
            position.y - 20f,
            position.x + healthBarWidth/2,
            position.y - 15f,
            paint
        )

        // Détermine la couleur selon la vie restante
        paint.color = when {
            healthPercentage > 0.7f -> Color.GREEN
            healthPercentage > 0.3f -> Color.YELLOW
            else -> Color.RED
        }
        canvas.drawRect(
            position.x - healthBarWidth / 2,
            position.y - 20f,
            position.x - healthBarWidth / 2 + healthBarWidth * healthPercentage,
            position.y - 15f,
            paint
        )
    }

    private fun drawDamageEffect(canvas: Canvas, paint: Paint) {
        // Effet de flash blanc
        paint.color = Color.argb((damageAnimationProgress * 150).toInt(), 255, 255, 255)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(position.x, position.y, getEnemyRadius() * 1.5f, paint)
    }

    private fun drawDeathEffect(canvas: Canvas, paint: Paint) {
        // Effet de disparition progressive
        val scale = 1f - deathAnimationProgress
        val alpha = (255 * (1 - deathAnimationProgress)).toInt()
        
        // Effet de rotation
        val rotation = deathAnimationProgress * 360f
        
        canvas.save()
        canvas.rotate(rotation, position.x, position.y)
        canvas.scale(scale, scale, position.x, position.y)
        
        // Dessiner l'ennemi avec transparence
        paint.alpha = alpha
        drawEnemy(canvas, paint)
        
        canvas.restore()
    }

    abstract fun drawEnemy(canvas: Canvas, paint: Paint)
    
    protected abstract fun getEnemyRadius(): Float

    fun takeDamage(amount: Float): Boolean {
        health -= amount
        damageAnimationProgress = 1f
        lastDamageTime = System.currentTimeMillis() / 1000f
        
        if (health <= 0 && !isDying) {
            isDying = true
            return true
        }
        return false
    }

    fun isDead(): Boolean = isDead

    private fun calculateDistance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return hypot(dx, dy)
    }
}

