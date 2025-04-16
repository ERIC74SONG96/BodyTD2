package com.example.myapplicationbodytd.towers

enum class TowerType(
    val cost: Int,
    val color: Int
) {
    BASIC(100, android.graphics.Color.BLUE),
    SNIPER(200, android.graphics.Color.RED),
    RAPID(150, android.graphics.Color.GREEN)
} 