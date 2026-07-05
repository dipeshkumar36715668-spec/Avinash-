package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_boards")
data class MoodBoard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val isCollaborative: Boolean = false,
    val collaborators: String = "", // Comma-separated names
    val coverImageUrl: String = ""
)

@Entity(tableName = "saved_pins")
data class SavedPin(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boardId: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val category: String,
    val price: Double = 0.0,
    val isFromMarketplace: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_assets")
data class MarketplaceAsset(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val price: Double, // In credits/points
    val rating: Float = 4.8f,
    val downloads: Int = 0,
    val isPurchased: Boolean = false,
    val category: String // "Brush Set", "Vector Illustration", "Color Palette", "Web Template"
)
