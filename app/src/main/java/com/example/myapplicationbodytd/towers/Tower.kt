package com.example.myapplicationbodytd.towers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.sqrt

abstract class Tower( //cette classe ne peut pas être instanciée directement
    val position: PointF,
    private var level: Int = 1
) {
    abstract val type: TowerType
    abstract val range: Float
    abstract val damage: Float
    abstract val attackSpeed: Float
    abstract val upgradeCost: Int
    abstract val maxLevel: Int

    private var lastAttackTime: Long = 0
    private var isSelected = false
    protected val projectiles = mutableListOf<Projectile>()
    private var animationProgress = 0f
    
    // Callback pour les ennemis touchés
    var onEnemyHitListener: ((Enemy) -> Unit)? = null

    open fun update(currentTime: Long, enemies: List<Enemy>) {
        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * attackSpeed) % 1f

        // Mise à jour des projectiles
        updateProjectiles(enemies)

        // Recherche d'une cible
        if (currentTime - lastAttackTime >= (1000 / attackSpeed).toLong()) {
            val currentTarget = findNearestEnemy(enemies)
            if (currentTarget != null) {
                attack(currentTarget)
                lastAttackTime = currentTime
            }
        }
    }

    private fun updateProjectiles(enemies: List<Enemy>) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            val hitEnemy = projectile.update(enemies)
            if (hitEnemy != null) {
                hitEnemy.takeDamage(projectile.damage)
                onEnemyHitListener?.invoke(hitEnemy)
                iterator.remove()
            } else if (!projectile.isActive) {
                iterator.remove()
            }
        }
    }

    private fun findNearestEnemy(enemies: List<Enemy>): Enemy? {
        return enemies
            .minByOrNull { calculateDistance(position, it.position) }
            //Pas besoin de vérifier si la position est null car au début position : pointF
            ?.takeIf { calculateDistance(position, it.position) <= range }
    }

    private fun calculateDistance(start: PointF, end: PointF): Float {
        val dx = end.x - start.x
        val dy = end.y - start.y
        return sqrt(dx * dx + dy * dy)
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
            paint.color = Color.argb(50, 255, 255, 255)
            canvas.drawCircle(position.x, position.y, range, paint)
        }

        // Dessiner le niveau
        paint.color = Color.WHITE
        paint.textSize = 30f
        canvas.drawText(level.toString(), position.x - 10f, position.y + 10f, paint)

        // Dessiner les projectiles
        projectiles.forEach { it.draw(canvas, paint) }
    }

    fun select() { isSelected = true }
    fun deselect() { isSelected = false }
}

