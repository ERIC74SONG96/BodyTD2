package com.example.myapplicationbodytd.managers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.content.res.Resources
import android.content.Context
import android.util.Log
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.enemies.Virus
import com.example.myapplicationbodytd.enemies.Bacteria
import com.example.myapplicationbodytd.enemies.Parasite
import com.example.myapplicationbodytd.towers.Tower
import com.example.myapplicationbodytd.towers.TowerType
import com.example.myapplicationbodytd.towers.BasicTower
import com.example.myapplicationbodytd.towers.SniperTower
import com.example.myapplicationbodytd.towers.RapidTower
import com.example.myapplicationbodytd.ui.Map
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos

class GameManager private constructor(private val context: Context) {
    private var money = 100
    private var health = 100
    private var score = 0
    private var currentWave = 0
    private var isGameOver = false
    private var isGameStarted = false
    private var selectedTowerType: TowerType? = null
    private var selectedTower: Tower? = null
    
    private var screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels
    private var screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels
    
    private val soundManager = SoundManager.getInstance(context)
    
    companion object {
        // Constantes pour les zones de l'interface (en pourcentage de l'écran)
        const val TOWER_MENU_HEIGHT_PERCENT = 0.15f  // 15% de la hauteur de l'écran
        
        @Volatile
        private var instance: GameManager? = null

        fun getInstance(context: Context): GameManager {
            return instance ?: synchronized(this) {
                instance ?: GameManager(context).also { instance = it }
            }
        }
    }
    
    // Variables pour les dimensions réelles
    private var towerMenuHeight: Float = 0f
    private var gameAreaTop: Float = 0f
    private var gameAreaBottom: Float = 0f
    
    private val towers = mutableListOf<Tower>()
    private val enemies = mutableListOf<Enemy>()
    private val map = Map(this)
    private val waveManager = WaveManager()
    
    private var onGameOverListener: (() -> Unit)? = null
    private var onWaveCompleteListener: ((Int) -> Unit)? = null
    private var onMoneyChangedListener: ((Int) -> Unit)? = null
    private var onHealthChangedListener: ((Int) -> Unit)? = null
    private var onScoreChangedListener: ((Int) -> Unit)? = null

    private var lastTappedPosition: PointF? = null

    private var waveBreakTime = 10.0f
    private var currentWaveBreakTime = 0.0f
    private var isWaveBreak = false
    private var bonusMoneyMultiplier = 1.0f
    private var bonusHealthReward = 10

    // Getters publics
    fun isGameOverState(): Boolean = isGameOver
    fun getCurrentMoney(): Int = money
    fun getCurrentHealth(): Int = health
    fun getCurrentScore(): Int = score
    fun getCurrentWaveNumber(): Int = currentWave
    fun getWaveManager(): WaveManager = waveManager
    fun getSelectedTowerType(): TowerType? = selectedTowerType

    @Synchronized
    fun startGame() {
        try {
            isGameStarted = true
            money = 100
            health = 100
            score = 0
            currentWave = 0
            isGameOver = false
            isWaveBreak = false
            currentWaveBreakTime = 0f
            bonusMoneyMultiplier = 1.0f
            bonusHealthReward = 10
            selectedTowerType = null
            selectedTower = null
            
            towers.clear()
            enemies.clear()
            
            soundManager.startBackgroundMusic()
            
            val waypoints = map.getWayPoints()
            if (waypoints.isNotEmpty()) {
                waveManager.startNextWave(waypoints.first())
                soundManager.playSound(SoundType.WAVE_START)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resetGameState()
        }
    }

    private fun resetGameState() {
        isGameStarted = false
        money = 100
        health = 100
        score = 0
        currentWave = 0
        isGameOver = false
        isWaveBreak = false
        selectedTowerType = null
        selectedTower = null
        towers.clear()
        enemies.clear()
    }

    @Synchronized
    fun update(deltaTime: Float) {
        if (!isGameStarted || isGameOver || deltaTime <= 0 || deltaTime > 1.0f) return

        try {
            synchronized(this) {
                // Mise à jour des ennemis
                updateEnemies(deltaTime)
                
                // Mise à jour des tours avec la liste des ennemis
                updateTowers()
                
                // Mise à jour de la vague
                updateWave(deltaTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resetGameState()
        }
    }

    private fun updateEnemies(deltaTime: Float) {
        if (enemies.isEmpty()) return

        val waypoints = map.getWayPoints()
        if (waypoints.isEmpty()) return

        synchronized(enemies) {
            val iterator = enemies.iterator()
            while (iterator.hasNext()) {
                try {
                    val enemy = iterator.next()
                    if (enemy.position == null) {
                        iterator.remove()
                        continue
                    }

                    if (enemy.isDead()) {
                        handleEnemyDeath(enemy)
                        iterator.remove()
                        continue
                    }

                    val reachedEnd = enemy.update(waypoints, deltaTime)
                    if (reachedEnd) {
                        handleEnemyReachedEnd(enemy)
                        iterator.remove()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    iterator.remove()
                }
            }
        }
    }

    private fun updateTowers() {
        synchronized(this) {
            try {
                val currentTime = System.currentTimeMillis()
                towers.forEach { tower ->
                    // Mise à jour de la tour avec le temps actuel
                    tower.update(currentTime, enemies)
                    
                    // Configuration du callback pour les hits
                    tower.onEnemyHitListener = { enemy ->
                        handleEnemyHit(enemy)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GameManager", "Erreur lors de la mise à jour des tours", e)
            }
        }
    }

    private fun handleEnemyDeath(enemy: Enemy) {
        if (isGameOver) return

        synchronized(this) {
            try {
                // Calcul de la récompense en argent avec bonus de vague
                val baseReward = when (enemy) {
                    is Virus -> 30
                    is Bacteria -> 25
                    is Parasite -> 40
                    else -> 20
                }
                val moneyReward = (baseReward * bonusMoneyMultiplier).toInt()
                addMoney(moneyReward)

                Log.d("GameManager", "Enemy tué: ${enemy.javaClass.simpleName}, Argent: +$moneyReward")
                soundManager.playSound(SoundType.ENEMY_DEATH)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GameManager", "Erreur lors de la mise à jour de l'argent", e)
            }
        }
    }

    private fun handleEnemyReachedEnd(enemy: Enemy) {
        if (isGameOver) return

        synchronized(this) {
            try {
                val damage = enemy.damage.toInt().coerceAtLeast(0)
                health = (health - damage).coerceIn(0, 100)
                onHealthChangedListener?.invoke(health)
                soundManager.playSound(SoundType.ENEMY_HIT)
                if (health <= 0) {
                    gameOver()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateWave(deltaTime: Float) {
        if (isGameOver) return

        synchronized(this) {
            try {
                if (isWaveBreak) {
                    handleWaveBreak(deltaTime)
                    return
                }

                val waypoints = map.getWayPoints()
                if (waypoints.isEmpty()) return

                val newEnemies = waveManager.update(deltaTime)
                if (newEnemies.isNotEmpty()) {
                    synchronized(enemies) {
                        enemies.addAll(newEnemies.filter { it.position != null })
                    }
                }

                if (waveManager.isWaveComplete() && enemies.isEmpty()) {
                    startWaveBreak()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleWaveBreak(deltaTime: Float) {
        try {
            currentWaveBreakTime += deltaTime
            if (currentWaveBreakTime >= waveBreakTime) {
                endWaveBreak()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            endWaveBreak()
        }
    }

    private fun startWaveBreak() {
        if (isGameOver) return

        try {
            isWaveBreak = true
            currentWaveBreakTime = 0f
            
            // Bonus de fin de vague
            val waveBonus = calculateWaveBonus()
            addMoney(waveBonus)
            
            // Ajout du score à la fin de la vague
            val waveScore = calculateWaveScore()
            score += waveScore
            onScoreChangedListener?.invoke(score)
            Log.d("GameManager", "Score de vague ajouté: +$waveScore (Total: $score)")
            
            // Bonus de santé si le joueur est en difficulté
            if (health < 50) {
                health = minOf(100, health + bonusHealthReward)
                onHealthChangedListener?.invoke(health)
            }
            
            soundManager.playSound(SoundType.WAVE_COMPLETE)
            onWaveCompleteListener?.invoke(currentWave)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateWaveBonus(): Int {
        val baseBonus = 100 + (currentWave * 25)  // Bonus de vague plus important
        return (baseBonus * bonusMoneyMultiplier).toInt()
    }

    private fun calculateWaveScore(): Int {
        // Score de base pour la vague
        val baseScore = 100 + (currentWave * 50)
        // Bonus pour chaque ennemi tué dans la vague
        val enemyBonus = enemies.size * 10
        return baseScore + enemyBonus
    }

    private fun endWaveBreak() {
        try {
            isWaveBreak = false
            currentWave++
            
            // Augmenter progressivement les bonus
            bonusMoneyMultiplier += 0.2f
            bonusHealthReward += 5
            
            // Démarrer la nouvelle vague
            waveManager.startNextWave(map.getWayPoints().first())
            soundManager.playSound(SoundType.WAVE_START)
            
            // Notifier le changement de vague
            onWaveCompleteListener?.invoke(currentWave)
        } catch (e: Exception) {
            e.printStackTrace()
            isWaveBreak = false
            waveManager.startNextWave(map.getWayPoints().first())
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        // Dessiner la zone de jeu
        canvas.save()
        canvas.clipRect(0f, gameAreaTop, screenWidth.toFloat(), gameAreaBottom)
        map.draw(canvas, paint)
        
        // Ne dessiner les entités que si le jeu a commencé
        if (isGameStarted) {
            drawEntities(canvas, paint)
            drawPreview(canvas, paint)
        }
        
        canvas.restore()
    }

    private fun drawEntities(canvas: Canvas, paint: Paint) {
        towers.forEach { it.draw(canvas, paint) }
        enemies.forEach { it.draw(canvas, paint) }
    }

    private fun drawPreview(canvas: Canvas, paint: Paint) {
        selectedTowerType?.let { type ->
            lastTappedPosition?.let { pos ->
                if (map.isValidTowerLocation(pos.x, pos.y)) {
                    val previewTower = when (type) {
                        TowerType.BASIC -> BasicTower(pos)
                        TowerType.SNIPER -> SniperTower(pos)
                        TowerType.RAPID -> RapidTower(pos)
                    }
                    paint.alpha = 128
                    previewTower.draw(canvas, paint)
                    paint.alpha = 255
                }
            }
        }
    }

    @Synchronized
    fun handleTap(x: Float, y: Float): Boolean {
        if (isGameOver) return false

        try {
            synchronized(this) {
                Log.d("GameManager", "handleTap: x=$x, y=$y, selectedTowerType=$selectedTowerType")
                
                if (!isValidTapPosition(x, y)) {
                    Log.d("GameManager", "Position invalide")
                    return false
                }

                lastTappedPosition = PointF(x, y)

                if (selectedTowerType != null) {
                    Log.d("GameManager", "Tentative de placement de tour: type=$selectedTowerType, money=$money")
                    if (canPlaceTower(x, y)) {
                        Log.d("GameManager", "Placement de tour possible")
                        placeTower(x, y)
                        return true
                    } else {
                        Log.d("GameManager", "Placement de tour impossible")
                        return false
                    }
                } else {
                    Log.d("GameManager", "Aucun type de tour sélectionné")
                    return trySelectExistingTower(x, y)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            deselectAll()
            return false
        }
    }

    private fun isValidTapPosition(x: Float, y: Float): Boolean {
        val isValid = x in 0f..screenWidth.toFloat() && y in 0f..screenHeight.toFloat()
        Log.d("GameManager", "isValidTapPosition: $isValid (x=$x, y=$y, screenWidth=$screenWidth, screenHeight=$screenHeight)")
        return isValid
    }

    private fun canPlaceTower(x: Float, y: Float): Boolean {
        val canPlace = selectedTowerType != null && 
                      map.isValidTowerLocation(x, y) && 
                      money >= (selectedTowerType?.cost ?: Int.MAX_VALUE)
        Log.d("GameManager", "canPlaceTower: $canPlace (type=$selectedTowerType, money=$money, validLocation=${map.isValidTowerLocation(x, y)}, x=$x, y=$y)")
        return canPlace
    }

    private fun trySelectExistingTower(x: Float, y: Float): Boolean {
        val clickedTower = towers.firstOrNull { tower ->
            val dx = tower.position.x - x
            val dy = tower.position.y - y
            sqrt(dx * dx + dy * dy) < 40f
        }

        if (clickedTower != null) {
            selectTower(clickedTower)
            return true
        }
        return false
    }

    private fun placeTower(x: Float, y: Float) {
        selectedTowerType?.let { type ->
            synchronized(this) {
                try {
                    if (money >= type.cost && map.isValidTowerLocation(x, y)) {
                        val newTower = when (type) {
                            TowerType.BASIC -> BasicTower(PointF(x, y))
                            TowerType.SNIPER -> SniperTower(PointF(x, y))
                            TowerType.RAPID -> RapidTower(PointF(x, y))
                        }
                        synchronized(towers) {
                            towers.add(newTower)
                        }
                        money = (money - type.cost).coerceAtLeast(0)
                        onMoneyChangedListener?.invoke(money)
                        map.addTowerPlacement(PointF(x, y))
                        soundManager.playSound(SoundType.TOWER_PLACED)
                        Log.d("GameManager", "Tour placée avec succès: type=$type, position=($x,$y)")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        selectedTowerType = null
        lastTappedPosition = null
    }

    private fun selectTower(tower: Tower) {
        synchronized(this) {
            selectedTower?.deselect()
            
            if (tower === selectedTower) {
                selectedTower = null
            } else {
                selectedTower = tower
                tower.select()
            }
            selectedTowerType = null
            lastTappedPosition = null
        }
    }

    private fun deselectAll() {
        selectedTower?.deselect()
        selectedTower = null
        selectedTowerType = null
        lastTappedPosition = null
    }

    fun selectTowerType(type: TowerType?) {
        selectedTowerType = type
        // Désélectionner la tour sélectionnée quand on choisit un type
        selectedTower?.deselect()
        selectedTower = null
    }

    @Synchronized
    fun upgradeSelectedTower() {
        selectedTower?.let { tower ->
            try {
                val upgradeCost = tower.calculateUpgradeCost()
                if (money >= upgradeCost && tower.canUpgrade()) {
                    money = (money - upgradeCost).coerceAtLeast(0)
                    onMoneyChangedListener?.invoke(money)
                    tower.upgrade()
                    soundManager.playSound(SoundType.TOWER_UPGRADED)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun gameOver() {
        isGameOver = true
        soundManager.playSound(SoundType.GAME_OVER)
        soundManager.pauseBackgroundMusic()
        onGameOverListener?.invoke()
    }

    fun setOnGameOverListener(listener: () -> Unit) {
        onGameOverListener = listener
    }

    fun setOnWaveCompleteListener(listener: (Int) -> Unit) {
        onWaveCompleteListener = listener
    }

    fun setOnMoneyChangedListener(listener: (Int) -> Unit) {
        onMoneyChangedListener = listener
    }

    fun setOnHealthChangedListener(listener: (Int) -> Unit) {
        onHealthChangedListener = listener
    }

    fun setOnScoreChangedListener(listener: (Int) -> Unit) {
        onScoreChangedListener = listener
    }

    fun setScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        
        // Calculer les dimensions réelles basées sur les pourcentages
        towerMenuHeight = height * TOWER_MENU_HEIGHT_PERCENT
        gameAreaTop = 0f
        gameAreaBottom = height - towerMenuHeight
        
        Log.d("GameManager", "Screen dimensions updated: $width x $height")
        Log.d("GameManager", "Tower menu height: $towerMenuHeight")
        Log.d("GameManager", "Game area: top=$gameAreaTop, bottom=$gameAreaBottom")
        
        map.updatePath()
    }

    fun getScreenWidth(): Int = screenWidth
    fun getScreenHeight(): Int = screenHeight
    fun getMoney(): Int = money
    fun getHealth(): Int = health
    fun getScore(): Int = score
    fun getCurrentWave(): Int = currentWave
    fun isGameOver(): Boolean = isGameOver

    @Synchronized
    fun addTower(type: TowerType, x: Float, y: Float): Boolean {
        if (isGameOver || !map.isValidTowerLocation(x, y) || money < type.cost) {
            return false
        }

        val position = PointF(x, y)
        val newTower = when (type) {
            TowerType.BASIC -> BasicTower(position)
            TowerType.SNIPER -> SniperTower(position)
            TowerType.RAPID -> RapidTower(position)
        }

        synchronized(towers) {
            towers.add(newTower)
        }

        money -= type.cost
        onMoneyChangedListener?.invoke(money)
        return true
    }

    @Synchronized
    fun addMoney(amount: Int) {
        if (amount <= 0 || isGameOver) return
        try {
            val oldMoney = money
            money = (money + amount).coerceIn(0, 9999)
            if (money != oldMoney) {
                onMoneyChangedListener?.invoke(money)
                Log.d("GameManager", "Argent mis à jour: $oldMoney -> $money (+$amount)")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("GameManager", "Erreur lors de l'ajout d'argent", e)
        }
    }

    @Synchronized
    fun takeDamage(amount: Int) {
        if (amount <= 0 || isGameOver) return
        try {
            health = (health - amount).coerceIn(0, 100)
            onHealthChangedListener?.invoke(health)
            if (health <= 0) {
                gameOver()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getWaveBreakProgress(): Float {
        return if (isWaveBreak) currentWaveBreakTime / waveBreakTime else 0f
    }

    fun isInWaveBreak(): Boolean = isWaveBreak

    fun skipWaveBreak() {
        if (isWaveBreak) {
            endWaveBreak()
        }
    }

    // Getters pour les dimensions
    fun getTowerMenuHeight(): Float = towerMenuHeight
    fun getGameAreaTop(): Float = gameAreaTop
    fun getGameAreaBottom(): Float = gameAreaBottom

    fun handleGameAreaTap(x: Float, y: Float) {
        if (isGameOver) return

        try {
            synchronized(this) {
                Log.d("GameManager", "handleGameAreaTap: x=$x, y=$y, selectedTowerType=$selectedTowerType")
                
                if (!isValidTapPosition(x, y)) {
                    Log.d("GameManager", "Position invalide")
                    return
                }

                lastTappedPosition = PointF(x, y)

                if (selectedTowerType != null) {
                    Log.d("GameManager", "Tentative de placement de tour: type=$selectedTowerType, money=$money")
                    if (canPlaceTower(x, y)) {
                        Log.d("GameManager", "Placement de tour possible")
                        placeTower(x, y)
                    } else {
                        Log.d("GameManager", "Placement de tour impossible")
                    }
                } else {
                    Log.d("GameManager", "Aucun type de tour sélectionné")
                    trySelectExistingTower(x, y)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            deselectAll()
        }
    }

    fun isGameStarted(): Boolean = isGameStarted

    fun setMuted(muted: Boolean) {
        soundManager.setMuted(muted)
    }

    fun release() {
        soundManager.release()
    }

    private fun handleEnemyHit(enemy: Enemy) {
        if (isGameOver) return

        synchronized(this) {
            try {
                // On ne met plus à jour le score ici
                Log.d("GameManager", "Enemy touché: ${enemy.javaClass.simpleName}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GameManager", "Erreur lors de la mise à jour du score (hit)", e)
            }
        }
    }
}


