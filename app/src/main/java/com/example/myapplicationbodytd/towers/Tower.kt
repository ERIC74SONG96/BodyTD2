package com.example.myapplicationbodytd.towers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
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
    protected var animationProgress = 0f
    
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

    protected fun updateProjectiles(enemies: List<Enemy>) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            val hitEnemy = projectile.update(0.016f, enemies)
            if (hitEnemy != null) {
                hitEnemy.takeDamage(projectile.damage)
                onEnemyHitListener?.invoke(hitEnemy)
                iterator.remove()
            } else if (!projectile.isActive) {
                iterator.remove()
            }
        }
    }

    protected fun findNearestEnemy(enemies: List<Enemy>): Enemy? {
        return enemies
            .filter { it.position != null }
            .minByOrNull { calculateDistance(position, it.position!!) }
            ?.takeIf { calculateDistance(position, it.position!!) <= range }
    }

    protected fun calculateDistance(start: PointF, end: PointF): Float {
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
    fun canUpgrade(): Boolean = level < maxLevel
    fun calculateUpgradeCost(): Int = upgradeCost * level
    fun upgrade() { if (canUpgrade()) level++ }
}

class BasicTower(position: PointF) : Tower(position) {
    override val type = TowerType.BASIC
    override val range = 200f
    override val damage = 20f
    override val attackSpeed = 1.5f
    override val upgradeCost = 100
    override val maxLevel = 3

    override fun attack(enemy: Enemy) {
        enemy.position?.let { pos ->
            projectiles.add(Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = pos,
                speed = 1200f,
                damage = damage,
                color = Color.GRAY
            ))
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 1200f,
            damage = damage,
            color = Color.GRAY
        )
    }
}

class SniperTower(position: PointF) : Tower(position) {
    override val type = TowerType.SNIPER
    override val range = 400f
    override val damage = 50f
    override val attackSpeed = 0.5f
    override val upgradeCost = 200
    override val maxLevel = 3

    override fun attack(enemy: Enemy) {
        enemy.position?.let { pos ->
            projectiles.add(Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = pos,
                speed = 2500f,
                damage = damage,
                color = Color.GREEN
            ))
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 2500f,
            damage = damage,
            color = Color.GREEN
        )
    }
}

class RapidTower(position: PointF) : Tower(position) {
    override val type = TowerType.RAPID
    override val range = 150f
    override val damage = 10f
    override val attackSpeed = 5.0f
    override val upgradeCost = 150
    override val maxLevel = 3

    override fun attack(enemy: Enemy) {
        enemy.position?.let { pos ->
            projectiles.add(Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = pos,
                speed = 2000f,
                damage = damage,
                color = Color.RED
            ))
        }
    }

    override fun createProjectile(targetPosition: PointF): Projectile {
        return Projectile(
            startPosition = PointF(position.x, position.y),
            targetPosition = targetPosition,
            speed = 2000f,
            damage = damage,
            color = Color.RED
        )
    }
}
