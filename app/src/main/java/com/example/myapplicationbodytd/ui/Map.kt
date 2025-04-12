package com.example.myapplicationbodytd.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PathMeasure
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import com.example.myapplicationbodytd.managers.GameManager
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import android.util.Log

class Map(private val gameManager: GameManager) {
    private val path = Path()
    private val wayPoints = mutableListOf<PointF>()
    private val towerPlacements = mutableSetOf<PointF>()
    
    private val pathWidth = 30f
    private val minTowerDistance = 80f
    private val gridSize = 50f
    private var animationProgress = 0f
    private val pathGlowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = pathWidth + 20f
        color = Color.argb(50, 255, 255, 255)
        maskFilter = android.graphics.BlurMaskFilter(15f, android.graphics.BlurMaskFilter.Blur.OUTER)
    }

    init {
        initializePath()
    }

    private fun initializePath() {
        val screenWidth = gameManager.getScreenWidth()
        val screenHeight = gameManager.getScreenHeight()
        
        // Points de contrôle pour un chemin qui ressemble à un intestin
        wayPoints.clear()
        wayPoints.add(PointF(0f, screenHeight / 2f))
        
        // Premier segment - entrée de l'intestin
        wayPoints.add(PointF(screenWidth * 0.1f, screenHeight * 0.4f))
        wayPoints.add(PointF(screenWidth * 0.2f, screenHeight * 0.6f))
        
        // Deuxième segment - première boucle
        wayPoints.add(PointF(screenWidth * 0.3f, screenHeight * 0.3f))
        wayPoints.add(PointF(screenWidth * 0.4f, screenHeight * 0.7f))
        wayPoints.add(PointF(screenWidth * 0.5f, screenHeight * 0.4f))
        
        // Troisième segment - deuxième boucle
        wayPoints.add(PointF(screenWidth * 0.6f, screenHeight * 0.6f))
        wayPoints.add(PointF(screenWidth * 0.7f, screenHeight * 0.3f))
        wayPoints.add(PointF(screenWidth * 0.8f, screenHeight * 0.5f))
        
        // Dernier segment - sortie de l'intestin
        wayPoints.add(PointF(screenWidth * 0.9f, screenHeight * 0.4f))
        wayPoints.add(PointF(screenWidth.toFloat(), screenHeight / 2f))

        // Création du chemin avec des courbes de Bézier plus organiques
        path.reset()
        path.moveTo(wayPoints[0].x, wayPoints[0].y)
        
        for (i in 0 until wayPoints.size - 1) {
            val current = wayPoints[i]
            val next = wayPoints[i + 1]
            
            // Calcul des points de contrôle pour des courbes plus organiques
            val controlX1 = current.x + (next.x - current.x) * 0.3f
            val controlY1 = current.y + (next.y - current.y) * 0.3f
            val controlX2 = current.x + (next.x - current.x) * 0.7f
            val controlY2 = current.y + (next.y - current.y) * 0.7f
            
            path.cubicTo(controlX1, controlY1, controlX2, controlY2, next.x, next.y)
        }

        // Ajout d'emplacements de tours stratégiques le long du chemin
        towerPlacements.clear()
        
        // Zone 1: Défense initiale
        towerPlacements.add(PointF(screenWidth * 0.1f, screenHeight * 0.35f))
        towerPlacements.add(PointF(screenWidth * 0.1f, screenHeight * 0.65f))
        
        // Zone 2: Première boucle
        towerPlacements.add(PointF(screenWidth * 0.3f, screenHeight * 0.25f))
        towerPlacements.add(PointF(screenWidth * 0.3f, screenHeight * 0.75f))
        
        // Zone 3: Deuxième boucle
        towerPlacements.add(PointF(screenWidth * 0.5f, screenHeight * 0.35f))
        towerPlacements.add(PointF(screenWidth * 0.5f, screenHeight * 0.65f))
        
        // Zone 4: Troisième boucle
        towerPlacements.add(PointF(screenWidth * 0.7f, screenHeight * 0.25f))
        towerPlacements.add(PointF(screenWidth * 0.7f, screenHeight * 0.75f))
        
        // Zone 5: Défense finale
        towerPlacements.add(PointF(screenWidth * 0.9f, screenHeight * 0.35f))
        towerPlacements.add(PointF(screenWidth * 0.9f, screenHeight * 0.65f))
    }

    fun updatePath() {
        initializePath()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        val screenWidth = canvas.width
        val screenHeight = canvas.height

        // Mise à jour de l'animation
        animationProgress = (animationProgress + 0.01f) % 1f

        // Dessiner la grille avec effet de profondeur
        drawGrid(canvas, paint, screenWidth, screenHeight)

        // Dessiner le chemin avec effet de brillance
        drawPath(canvas, paint)

        // Dessiner les points de contrôle avec animation
        drawWaypoints(canvas, paint)

        // Dessiner les emplacements de tours valides avec effet de pulsation
        drawTowerPlacements(canvas, paint)
    }

    private fun drawGrid(canvas: Canvas, paint: Paint, screenWidth: Int, screenHeight: Int) {
        // Grille principale
        paint.color = Color.argb(30, 255, 255, 255)
        paint.strokeWidth = 1f
        
        for (x in 0..screenWidth step gridSize.toInt()) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), screenHeight.toFloat(), paint)
        }
        for (y in 0..screenHeight step gridSize.toInt()) {
            canvas.drawLine(0f, y.toFloat(), screenWidth.toFloat(), y.toFloat(), paint)
        }

        // Grille secondaire (plus fine)
        paint.color = Color.argb(15, 255, 255, 255)
        paint.strokeWidth = 0.5f
        
        for (x in 0..screenWidth step (gridSize/2).toInt()) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), screenHeight.toFloat(), paint)
        }
        for (y in 0..screenHeight step (gridSize/2).toInt()) {
            canvas.drawLine(0f, y.toFloat(), screenWidth.toFloat(), y.toFloat(), paint)
        }
    }

    private fun drawPath(canvas: Canvas, paint: Paint) {
        // Effet de brillance du chemin
        canvas.drawPath(path, pathGlowPaint)

        // Chemin principal
        paint.color = Color.argb(200, 100, 100, 100)
        paint.strokeWidth = pathWidth
        paint.style = Paint.Style.STROKE
        
        // Effet de texture sur le chemin
        val pathMeasure = PathMeasure(path, false)
        val length = pathMeasure.length
        val numPoints = 100
        
        for (i in 0 until numPoints) {
            val pos = FloatArray(2)
            val tan = FloatArray(2)
            val distance = length * i / numPoints
            pathMeasure.getPosTan(distance, pos, tan)
            
            // Effet de brillance qui se déplace le long du chemin
            val alpha = (sin(animationProgress * 2 * PI.toFloat() + i * 0.1f) * 50 + 50).toInt()
            paint.color = Color.argb(alpha, 150, 150, 150)
            canvas.drawCircle(pos[0], pos[1], pathWidth/2, paint)
        }
        
        // Dessiner le chemin principal
        paint.color = Color.argb(200, 100, 100, 100)
        canvas.drawPath(path, paint)
    }

    private fun drawWaypoints(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        
        wayPoints.forEachIndexed { index, point ->
            // Effet de pulsation pour les points de contrôle
            val pulseScale = 1f + 0.2f * sin(animationProgress * 2 * PI.toFloat() + index * 0.5f)
            
            // Cercle extérieur
            paint.color = Color.argb(100, 255, 255, 0)
            canvas.drawCircle(point.x, point.y, 15f * pulseScale, paint)
            
            // Point central
            paint.color = Color.YELLOW
            canvas.drawCircle(point.x, point.y, 10f * pulseScale, paint)
            
            // Numéro du point
            paint.color = Color.BLACK
            paint.textSize = 20f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText((index + 1).toString(), point.x, point.y + 7f, paint)
        }
    }

    private fun drawTowerPlacements(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        
        towerPlacements.forEach { point ->
            // Effet de pulsation pour les emplacements de tours
            val pulseScale = 1f + 0.1f * sin(animationProgress * 3 * PI.toFloat())
            
            // Cercle extérieur
            paint.color = Color.argb(80, 0, 255, 0)
            canvas.drawCircle(point.x, point.y, minTowerDistance/2 * pulseScale, paint)
            
            // Cercle intérieur
            paint.color = Color.argb(120, 0, 255, 0)
            canvas.drawCircle(point.x, point.y, minTowerDistance/3 * pulseScale, paint)
        }
    }

    fun isValidTowerLocation(x: Float, y: Float): Boolean {
        val point = PointF(x, y)
        val screenWidth = gameManager.getScreenWidth()
        val screenHeight = gameManager.getScreenHeight()
        
        // Vérifier si le point est trop proche du chemin
        val nearPath = isPointNearPath(point)
        if (nearPath) {
            Log.d("Map", "Point trop proche du chemin: ($x,$y)")
            return false
        }

        // Vérifier si le point est trop proche d'une autre tour
        val nearTower = isPointNearTower(point)
        if (nearTower) {
            Log.d("Map", "Point trop proche d'une autre tour: ($x,$y)")
            return false
        }

        // Vérifier si le point est dans les limites de l'écran
        val inBounds = x >= minTowerDistance && x <= screenWidth - minTowerDistance &&
                      y >= minTowerDistance && y <= screenHeight - minTowerDistance
        if (!inBounds) {
            Log.d("Map", "Point hors limites: ($x,$y)")
            return false
        }

        Log.d("Map", "Emplacement valide pour une tour: ($x,$y)")
        return true
    }

    private fun isPointNearPath(point: PointF): Boolean {
        val pathMeasure = PathMeasure(path, false)
        var minDistance = Float.MAX_VALUE
        
        for (i in 0..100) {
            val pathPoint = FloatArray(2)
            pathMeasure.getPosTan(pathMeasure.length * i / 100f, pathPoint, null)
            val dist = calculateDistance(point.x, point.y, pathPoint[0], pathPoint[1])
            minDistance = minOf(minDistance, dist)
        }
        
        val isNear = minDistance < pathWidth
        if (isNear) {
            Log.d("Map", "Distance minimale au chemin: $minDistance (limite: $pathWidth)")
        }
        return isNear
    }

    private fun isPointNearTower(point: PointF): Boolean {
        return towerPlacements.any { towerPoint ->
            calculateDistance(point.x, point.y, towerPoint.x, towerPoint.y) < minTowerDistance
        }
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }

    fun addTowerPlacement(point: PointF) {
        towerPlacements.add(point)
    }

    fun getWayPoints(): List<PointF> = wayPoints.toList()

    fun getPath(): Path = path
}
