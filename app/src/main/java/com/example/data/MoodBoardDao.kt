package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodBoardDao {
    // Mood Boards
    @Query("SELECT * FROM mood_boards ORDER BY name ASC")
    fun getAllBoards(): Flow<List<MoodBoard>>

    @Query("SELECT * FROM mood_boards WHERE id = :boardId LIMIT 1")
    suspend fun getBoardById(boardId: Long): MoodBoard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: MoodBoard): Long

    @Delete
    suspend fun deleteBoard(board: MoodBoard)

    @Query("DELETE FROM mood_boards WHERE id = :boardId")
    suspend fun deleteBoardById(boardId: Long)

    // Saved Pins
    @Query("SELECT * FROM saved_pins WHERE boardId = :boardId ORDER BY savedAt DESC")
    fun getPinsForBoard(boardId: Long): Flow<List<SavedPin>>

    @Query("SELECT * FROM saved_pins ORDER BY savedAt DESC")
    fun getAllSavedPins(): Flow<List<SavedPin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPin(pin: SavedPin): Long

    @Query("DELETE FROM saved_pins WHERE id = :pinId")
    suspend fun deleteSavedPinById(pinId: Long)

    // Marketplace Assets
    @Query("SELECT * FROM marketplace_assets ORDER BY title ASC")
    fun getAllMarketplaceAssets(): Flow<List<MarketplaceAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceAssets(assets: List<MarketplaceAsset>)

    @Query("UPDATE marketplace_assets SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updateAssetPurchaseStatus(id: String, isPurchased: Boolean)
}
