package com.example.myapplicationbodytd

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.myapplicationbodytd.enemies.Enemy
import com.example.myapplicationbodytd.managers.GameManager
import com.example.myapplicationbodytd.player.Player
import com.example.myapplicationbodytd.towers.Tower
import com.example.myapplicationbodytd.towers.TowerType
import com.example.myapplicationbodytd.ui.Map
import com.example.myapplicationbodytd.ui.TowerButton

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var running = false
    private val surfaceHolder: SurfaceHolder = holder
    private var lastUpdateTime: Long = System.nanoTime()
    private var selectedTowerType: TowerType? = null
    private val targetFrameTime = 16_666_666L // ~60 FPS
    private val paint = Paint()
    private val gameManager: GameManager
    private val player: Player
    private var isGameStarted = false
    private val startButtonBounds = android.graphics.RectF()
    private val replayButtonBounds = android.graphics.RectF()

    init {
        holder.addCallback(this)
        isFocusable = true
        setZOrderOnTop(true)
        gameManager = GameManager.getInstance(context)
        player = Player()
        Log.d("GameView", "GameView initialized")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        println("GameView onSizeChanged: $w x $h")
        if (w > 0 && h > 0) {
            println("Calling setScreenDimensions on GameManager: $gameManager")
            gameManager.setScreenDimensions(w, h)
            
            // Définir les limites du bouton de démarrage
            val buttonWidth = w * 0.6f
            val buttonHeight = h * 0.1f
            val buttonX = (w - buttonWidth) / 2
            val buttonY = h * 0.4f
            startButtonBounds.set(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight)
            
            // Définir les limites du bouton rejouer
            replayButtonBounds.set(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("GameView", "Surface created")
        running = true
        lastUpdateTime = System.nanoTime()
        thread = Thread(this).apply { 
            name = "GameThread"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("GameView", "Surface changed: width=$width, height=$height")
        if (width > 0 && height > 0) {
            println("Calling setScreenDimensions on GameManager: $gameManager")
            gameManager.setScreenDimensions(width, height)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("GameView", "Surface destroyed")
        running = false
        thread?.join()
    }

    override fun run() {
        var lastTime = System.nanoTime()
        val targetTime = 1000000000 / 60 // 60 FPS

        while (running) {
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastTime) / 1000000000.0f
            lastTime = currentTime

            update(deltaTime)
            draw()

            val frameTime = System.nanoTime() - currentTime
            if (frameTime < targetTime) {
                Thread.sleep((targetTime - frameTime) / 1000000)
            }
        }
    }

    private fun update(deltaTime: Float) {
        if (isGameStarted) {
            try {
                gameManager.update(deltaTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun draw() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas()
            canvas?.let {
                // Effacer l'écran avec une couleur de fond
                it.drawColor(Color.BLACK)
                
                if (!isGameStarted) {
                    drawStartMenu(it)
                } else if (gameManager.isGameOver()) {
                    drawGameOver(it)
                } else {
                    drawGame(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
        }
    }

    private fun drawGame(canvas: Canvas) {
        // Dessiner la zone de jeu
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), gameManager.getGameAreaBottom())
        gameManager.draw(canvas, paint)
        canvas.restore()

        // Dessiner le menu des tours
        drawTowerMenu(canvas)
    }

    private fun drawStartMenu(canvas: Canvas) {
        // Dessiner le titre
        paint.color = Color.WHITE
        paint.textSize = 80f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Body TD", width / 2f, height * 0.3f, paint)

        // Dessiner le bouton de démarrage
        paint.color = Color.GREEN
        canvas.drawRoundRect(startButtonBounds, 20f, 20f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("Commencer", startButtonBounds.centerX(), startButtonBounds.centerY() + paint.textSize / 3, paint)
    }

    private fun drawTowerMenu(canvas: Canvas) {
        val buttonHeight = gameManager.getTowerMenuHeight() / 3
        val buttonY = gameManager.getGameAreaBottom()
        
        // Dessiner les boutons de tours
        paint.color = TowerType.BASIC.color
        canvas.drawRect(0f, buttonY, width.toFloat(), buttonY + buttonHeight, paint)
        
        paint.color = TowerType.SNIPER.color
        canvas.drawRect(0f, buttonY + buttonHeight, width.toFloat(), buttonY + buttonHeight * 2, paint)
        
        paint.color = TowerType.RAPID.color
        canvas.drawRect(0f, buttonY + buttonHeight * 2, width.toFloat(), buttonY + buttonHeight * 3, paint)
        
        // Dessiner les prix des tours
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        
        canvas.drawText("${TowerType.BASIC.cost}$", width / 2f, buttonY + buttonHeight / 2 + paint.textSize / 3, paint)
        canvas.drawText("${TowerType.SNIPER.cost}$", width / 2f, buttonY + buttonHeight * 1.5f + paint.textSize / 3, paint)
        canvas.drawText("${TowerType.RAPID.cost}$", width / 2f, buttonY + buttonHeight * 2.5f + paint.textSize / 3, paint)
    }

    private fun drawGameOver(canvas: Canvas) {
        // Dessiner le message de fin de jeu
        paint.color = Color.WHITE
        paint.textSize = 80f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Game Over", width / 2f, height * 0.3f, paint)
        
        // Dessiner le score final
        paint.textSize = 40f
        canvas.drawText("Score: ${gameManager.getScore()}", width / 2f, height * 0.4f, paint)
        
        // Dessiner le bouton rejouer
        paint.color = Color.GREEN
        canvas.drawRoundRect(replayButtonBounds, 20f, 20f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("Rejouer", replayButtonBounds.centerX(), replayButtonBounds.centerY() + paint.textSize / 3, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                
                Log.d("GameView", "Touch DOWN - x: $x, y: $y")
                
                if (!isGameStarted) {
                    if (startButtonBounds.contains(x, y)) {
                        startGame()
                        return true
                    }
                    return false
                } else if (gameManager.isGameOver()) {
                    if (replayButtonBounds.contains(x, y)) {
                        restartGame()
                        return true
                    }
                    return false
                }
                
                // Vérifier si le tap est dans le menu des tours
                if (y >= gameManager.getGameAreaBottom()) {
                    Log.d("GameView", "Tap dans le menu des tours")
                    handleTowerMenuTap(x, y)
                    return true
                }
                
                // Vérifier si le tap est dans la zone de jeu
                if (y < gameManager.getGameAreaBottom()) {
                    Log.d("GameView", "Tap dans la zone de jeu")
                    gameManager.handleGameAreaTap(x, y)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startGame() {
        isGameStarted = true
        gameManager.startGame()
    }

    private fun restartGame() {
        isGameStarted = true
        gameManager.startGame()
    }

    private fun handleTowerMenuTap(x: Float, y: Float) {
        val buttonHeight = gameManager.getTowerMenuHeight() / 3
        val buttonY = gameManager.getGameAreaBottom()
        
        Log.d("GameView", "Menu des tours - x: $x, y: $y, buttonY: $buttonY, buttonHeight: $buttonHeight")
        
        when {
            y < buttonY + buttonHeight -> {
                Log.d("GameView", "Tour de base sélectionnée")
                gameManager.selectTowerType(TowerType.BASIC)
            }
            y < buttonY + buttonHeight * 2 -> {
                Log.d("GameView", "Tour sniper sélectionnée")
                gameManager.selectTowerType(TowerType.SNIPER)
            }
            else -> {
                Log.d("GameView", "Tour rapide sélectionnée")
                gameManager.selectTowerType(TowerType.RAPID)
            }
        }
    }

    fun resume() {
        if (!running) {
            running = true
            thread = Thread(this).apply { start() }
        }
    }

    fun pause() {
        running = false
        thread?.join()
    }

    fun cleanup() {
        // Arrêter la boucle de jeu
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // Libérer les ressources du canvas
        holder.surface.release()
    }
}
