package com.example.myapplicationbodytd.towers.strategies

import android.graphics.PointF
import com.example.myapplicationbodytd.enemies.Enemy
import kotlin.math.sqrt

interface AttackStrategy {
    fun selectTargets(towerPosition: PointF, range: Float, enemies: List<Enemy>): List<Enemy>
    
    fun calculateDamageMultiplier(enemy: Enemy): Float = 1f
    
    fun getProjectileColor(): Int = android.graphics.Color.WHITE
    
    fun getProjectileSpeed(): Float = 600f
} 