package com.example.myapplicationbodytd.towers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.enemies.BasicEnemy
import com.example.myapplicationbodytd.towers.strategies.AttackStrategy
import com.example.myapplicationbodytd.towers.strategies.MultiTargetStrategy
import com.example.myapplicationbodytd.towers.strategies.SingleTargetStrategy
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sqrt

abstract class Tower(
    val position: PointF,
    protected var level: Int = 1
) {
    abstract val type: TowerType
    abstract val range: Float
    abstract val damage: Float
    abstract val attackSpeed: Float
    abstract val upgradeCost: Int
    abstract val maxLevel: Int

    protected var lastAttackTime: Long = 0
    private var isSelected = false
    protected val projectiles = mutableListOf<Projectile>()
    protected var target: Enemy? = null
    
    // Callback pour les ennemis touchés
    var onEnemyHitListener: ((Enemy) -> Unit)? = null

    open fun update(currentTime: Long, enemies: List<Enemy>) {
        // Mise à jour des projectiles
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            val hitEnemy = projectile.update(0.016f, enemies)
            if (hitEnemy != null) {
                hitEnemy.takeDamage(projectile.damage)
                // Notifier que l'ennemi a été touché
                onEnemyHitListener?.invoke(hitEnemy)
                iterator.remove()
            } else if (!projectile.isActive) {
                iterator.remove()
            }
        }

        // Recherche d'une cible
        if (currentTime - lastAttackTime >= (1000 / attackSpeed).toLong()) {
            val currentTarget = findNearestEnemy(enemies)
            if (currentTarget != null) {
                attack(currentTarget)
                lastAttackTime = currentTime
            }
        }
    }

    protected fun findNearestEnemy(enemies: List<Enemy>): Enemy? {
        var nearestEnemy: Enemy? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            if (enemy.position == null) continue
            val distance = calculateDistance(position, enemy.position)
            if (distance <= range && distance < minDistance) {
                minDistance = distance
                nearestEnemy = enemy
            }
        }

        return nearestEnemy
    }

    protected fun calculateDistance(start: PointF, end: PointF): Float {
        return sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y))
    }

    protected abstract fun attack(target: Enemy)

    protected abstract fun createProjectile(targetPosition: PointF): Projectile

    open fun draw(canvas: Canvas, paint: Paint) {
        // Dessiner la tour
        paint.color = type.color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(position.x, position.y, 30f, paint)

        // Dessiner la portée si sélectionnée
        if (isSelected) {
            paint.color = android.graphics.Color.argb(50, 255, 255, 255)
            canvas.drawCircle(position.x, position.y, range, paint)
        }

        // Dessiner le niveau
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 30f
        canvas.drawText(level.toString(), position.x - 10f, position.y + 10f, paint)

        // Dessiner les projectiles
        for (projectile in projectiles) {
            projectile.draw(canvas, paint)
        }
    }

    fun select() {
        isSelected = true
    }

    fun deselect() {
        isSelected = false
    }

    fun canUpgrade(): Boolean = level < maxLevel

    fun calculateUpgradeCost(): Int = upgradeCost * level

    fun upgrade() {
        if (canUpgrade()) {
            level++
        }
    }
}

