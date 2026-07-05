package com.example.data

import kotlinx.coroutines.flow.Flow

class MoodBoardRepository(private val dao: MoodBoardDao) {
    val allBoards: Flow<List<MoodBoard>> = dao.getAllBoards()
    val allSavedPins: Flow<List<SavedPin>> = dao.getAllSavedPins()
    val allMarketplaceAssets: Flow<List<MarketplaceAsset>> = dao.getAllMarketplaceAssets()

    suspend fun getBoardById(boardId: Long): MoodBoard? = dao.getBoardById(boardId)

    fun getPinsForBoard(boardId: Long): Flow<List<SavedPin>> = dao.getPinsForBoard(boardId)

    suspend fun insertBoard(board: MoodBoard): Long = dao.insertBoard(board)

    suspend fun deleteBoardById(boardId: Long) {
        dao.deleteBoardById(boardId)
    }

    suspend fun insertSavedPin(pin: SavedPin): Long = dao.insertSavedPin(pin)

    suspend fun deleteSavedPinById(pinId: Long) = dao.deleteSavedPinById(pinId)

    suspend fun insertMarketplaceAssets(assets: List<MarketplaceAsset>) {
        dao.insertMarketplaceAssets(assets)
    }

    suspend fun purchaseAsset(id: String) {
        dao.updateAssetPurchaseStatus(id, true)
    }
}
