package com.example.myapplicationbodytd.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.myapplicationbodytd.towers.TowerType

class TowerButton(
    private val type: TowerType,
    private val bounds: RectF
) {
    fun draw(canvas: Canvas, paint: Paint) {
        // Dessiner le fond du bouton
        paint.color = type.color
        canvas.drawRect(bounds, paint)
        
        // Dessiner le texte du coût
        paint.color = android.graphics.Color.WHITE
        paint.textSize = bounds.height() * 0.4f
        val costText = "${type.cost}$"
        val textWidth = paint.measureText(costText)
        val textX = bounds.centerX() - textWidth / 2
        val textY = bounds.centerY() + paint.textSize / 3
        canvas.drawText(costText, textX, textY, paint)
    }
    
    fun contains(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }
    
    fun getType(): TowerType = type
    fun getBounds(): RectF = bounds
} 