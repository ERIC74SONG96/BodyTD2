package com.example.myapplicationbodytd.enemies

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI


abstract class Enemy(
    var position: PointF,
    var health: Float,
    var maxHealth: Float,
    var speed: Float,
    var damage: Float,
    var reward: Int
) {
    abstract val type: EnemyType
    private var currentWaypointIndex = 0
    private var progress = 0f
    private var isDead = false
    private var isReachedEnd = false
    protected var animationProgress = 0f
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
        val healthBarHeight = 5f
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

        // Barre de vie avec dégradé
        val gradient = LinearGradient(
            position.x - healthBarWidth/2,
            position.y - 20f,
            position.x + healthBarWidth/2,
            position.y - 15f,
            when {
                healthPercentage > 0.7f -> Color.GREEN
                healthPercentage > 0.3f -> Color.YELLOW
                else -> Color.RED
            },
            when {
                healthPercentage > 0.7f -> Color.rgb(0, 200, 0)
                healthPercentage > 0.3f -> Color.rgb(200, 200, 0)
                else -> Color.rgb(200, 0, 0)
            },
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(
            position.x - healthBarWidth/2,
            position.y - 20f,
            position.x - healthBarWidth/2 + healthBarWidth * healthPercentage,
            position.y - 15f,
            paint
        )
        paint.shader = null
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
    fun hasReachedEnd(): Boolean = isReachedEnd

    private fun calculateDistance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}

class Bacteria(position: PointF) : Enemy(
    position = position,
    health = 100f,
    maxHealth = 100f,
    speed = 150f,
    damage = 3f,
    reward = 30
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
