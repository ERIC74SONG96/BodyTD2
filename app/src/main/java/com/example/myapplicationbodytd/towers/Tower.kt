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

class BasicTower(position: PointF) : Tower(
    position = position,
    level = 1
) {
    override val type: TowerType = TowerType.BASIC
    override val range: Float = 200f
    override val damage: Float = 20f
    override val attackSpeed: Float = 1.5f
    override val upgradeCost: Int = 100
    override val maxLevel: Int = 3

    private var animationProgress = 0f

    override fun update(currentTime: Long, enemies: List<Enemy>) {
        super.update(currentTime, enemies)

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * 2) % 1f
    }

    override fun attack(enemy: Enemy) {
        if (enemy.position != null) {
            val projectile = Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = enemy.position,
                speed = 1200f,
                damage = damage,
                color = Color.GRAY
            )
            projectiles.add(projectile)
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

class SniperTower(position: PointF) : Tower(
    position = position,
    level = 1
) {
    override val type: TowerType = TowerType.SNIPER
    override val range: Float = 400f
    override val damage: Float = 50f
    override val attackSpeed: Float = 0.5f
    override val upgradeCost: Int = 200
    override val maxLevel: Int = 3

    private var animationProgress = 0f
    private val attackStrategy = SingleTargetStrategy()

    override fun update(currentTime: Long, enemies: List<Enemy>) {
        super.update(currentTime, enemies)

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * 3) % 1f
    }

    override fun attack(enemy: Enemy) {
        if (enemy.position != null) {
            val projectile = Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = enemy.position,
                speed = 2500f,
                damage = damage,
                color = Color.GREEN
            )
            projectiles.add(projectile)
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

class RapidTower(position: PointF) : Tower(
    position = position,
    level = 1
) {
    override val type: TowerType = TowerType.RAPID
    override val range: Float = 150f
    override val damage: Float = 10f
    override val attackSpeed: Float = 5.0f
    override val upgradeCost: Int = 150
    override val maxLevel: Int = 3

    private var animationProgress = 0f
    private val attackStrategy = MultiTargetStrategy()

    override fun update(currentTime: Long, enemies: List<Enemy>) {
        super.update(currentTime, enemies)

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.016f * 6) % 1f
    }

    override fun attack(enemy: Enemy) {
        if (enemy.position != null) {
            val projectile = Projectile(
                startPosition = PointF(position.x, position.y),
                targetPosition = enemy.position,
                speed = 2000f,
                damage = damage,
                color = Color.RED
            )
            projectiles.add(projectile)
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
