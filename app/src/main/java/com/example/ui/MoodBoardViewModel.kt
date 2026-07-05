package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.network.GeminiRequest
import com.example.ui.network.GenerationConfig
import com.example.ui.network.Content
import com.example.ui.network.Part
import com.example.ui.network.RetrofitClient
import com.example.ui.network.StyleRecommendation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DiscoverPin(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val category: String,
    val initialLikes: Int,
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BoardActivity(
    val id: String,
    val boardId: Long,
    val actor: String,
    val action: String,
    val relativeTime: String
)

sealed interface AiRecommendationState {
    object Idle : AiRecommendationState
    object Loading : AiRecommendationState
    data class Success(val recommendation: StyleRecommendation) : AiRecommendationState
    data class Error(val message: String) : AiRecommendationState
}

class MoodBoardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MoodBoardDatabase.getDatabase(application)
    private val repository = MoodBoardRepository(db.moodBoardDao())

    // Live state from Database
    val boards: StateFlow<List<MoodBoard>> = repository.allBoards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPins: StateFlow<List<SavedPin>> = repository.allSavedPins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketplaceAssets: StateFlow<List<MarketplaceAsset>> = repository.allMarketplaceAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Balance for Marketplace (simulated credits)
    private val _userCredits = MutableStateFlow(450)
    val userCredits: StateFlow<Int> = _userCredits.asStateFlow()

    // AI recommendation state
    private val _aiState = MutableStateFlow<AiRecommendationState>(AiRecommendationState.Idle)
    val aiState: StateFlow<AiRecommendationState> = _aiState.asStateFlow()

    // Active screen navigation
    private val _currentTab = MutableStateFlow("discover")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Detail pin overlay
    private val _selectedPin = MutableStateFlow<DiscoverPin?>(null)
    val selectedPin: StateFlow<DiscoverPin?> = _selectedPin.asStateFlow()

    // Current active board view
    private val _activeBoard = MutableStateFlow<MoodBoard?>(null)
    val activeBoard: StateFlow<MoodBoard?> = _activeBoard.asStateFlow()

    // List of interactive mock comments added during the session
    private val _customPinComments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val customPinComments: StateFlow<Map<String, List<Comment>>> = _customPinComments.asStateFlow()

    // Collaboration activities
    private val _collaborationLogs = MutableStateFlow<List<BoardActivity>>(emptyList())
    val collaborationLogs: StateFlow<List<BoardActivity>> = _collaborationLogs.asStateFlow()

    // Curated Discover Feed
    val discoverPins = listOf(
        DiscoverPin(
            id = "pin_1",
            title = "Japandi Oak Serenity",
            description = "Earthy minimalism combining Japanese rustic wabi-sabi aesthetics with functional Nordic elements.",
            imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600&auto=format&fit=crop",
            author = "Sora Takahashi",
            category = "Interior",
            initialLikes = 345,
            comments = listOf(
                Comment("Lars K.", "Love the balance of raw oak with linen textures."),
                Comment("Yuki T.", "Simplicity at its absolute finest.")
            )
        ),
        DiscoverPin(
            id = "pin_2",
            title = "Brutalist Concrete Oasis",
            description = "Raw exposed geometric concrete structures juxtaposed with emerald-tinted infinity pools.",
            imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600&auto=format&fit=crop",
            author = "Elena Rostova",
            category = "Architecture",
            initialLikes = 521,
            comments = listOf(
                Comment("BrutalistFan", "Incredible scale. The green pool softens it perfectly.")
            )
        ),
        DiscoverPin(
            id = "pin_3",
            title = "Warm Terracotta Warmth",
            description = "Earthy natural ochres, warm terracotta pots, and sandy linen draperies under soft afternoon sun.",
            imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=600&auto=format&fit=crop",
            author = "Mateo Silva",
            category = "Interior",
            initialLikes = 289
        ),
        DiscoverPin(
            id = "pin_4",
            title = "Neo-Retro Acid Editorial",
            description = "High-contrast editorial layout utilizing bold grotesque fonts, acid-green highlights, and chrome elements.",
            imageUrl = "https://images.unsplash.com/photo-1561070791-26c113006238?w=600&auto=format&fit=crop",
            author = "Zoe Vance",
            category = "Graphic Design",
            initialLikes = 412
        ),
        DiscoverPin(
            id = "pin_5",
            title = "Linen Summer Styling",
            description = "Effortless casual styling featuring off-white linen shirts, neutral trousers, and classic leather mules.",
            imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop",
            author = "Chara V.",
            category = "Fashion",
            initialLikes = 198
        ),
        DiscoverPin(
            id = "pin_6",
            title = "Warm Pastel Dining",
            description = "Playful mid-century dining space with custom pastel chairs, wavy wall shelves, and retro lighting.",
            imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=600&auto=format&fit=crop",
            author = "Clara Dupont",
            category = "Interior",
            initialLikes = 310
        ),
        DiscoverPin(
            id = "pin_7",
            title = "Emerald Forest Cabin",
            description = "A beautifully structured geometric wood cabin reflecting dark alpine forest foliage in massive glass facades.",
            imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&auto=format&fit=crop",
            author = "Olaf Thon",
            category = "Architecture",
            initialLikes = 604
        ),
        DiscoverPin(
            id = "pin_8",
            title = "Liquid Chromatic Wave",
            description = "Futuristic liquid 3D render art swirling in pastel gradients of lilac, powder blue, and soft silver reflections.",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop",
            author = "Aris Thorne",
            category = "Illustration",
            initialLikes = 478
        )
    )

    init {
        // Pre-populate some boards and marketplace assets if database is empty
        viewModelScope.launch {
            boards.first() // wait for flow initialization
            if (boards.value.isEmpty()) {
                repository.insertBoard(
                    MoodBoard(
                        name = "Japandi Serenity",
                        description = "Calming organic textures, warm woods, and serene negative spaces.",
                        isCollaborative = true,
                        collaborators = "Yuki, Clara, Lars",
                        coverImageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600&auto=format&fit=crop"
                    )
                )
                repository.insertBoard(
                    MoodBoard(
                        name = "Summer Linen Style",
                        description = "Loose silhouettes, earth shades, and breezy summer vibes.",
                        isCollaborative = false,
                        coverImageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop"
                    )
                )
                repository.insertBoard(
                    MoodBoard(
                        name = "Acid Grotesque Design",
                        description = "Bold neon designs, heavy grid spacing, and digital textures.",
                        isCollaborative = true,
                        collaborators = "Dave, Zoe",
                        coverImageUrl = "https://images.unsplash.com/photo-1561070791-26c113006238?w=600&auto=format&fit=crop"
                    )
                )
            }

            marketplaceAssets.first()
            if (marketplaceAssets.value.isEmpty()) {
                repository.insertMarketplaceAssets(
                    listOf(
                        MarketplaceAsset(
                            id = "asset_1",
                            title = "Wabi-Sabi Brush Bundle",
                            description = "30+ high-quality organic charcoal and wash brushes for Procreate and Photoshop.",
                            imageUrl = "https://images.unsplash.com/photo-1626785774573-4b799315345d?w=600&auto=format&fit=crop",
                            author = "Sora Takahashi",
                            price = 120.0,
                            downloads = 1420,
                            category = "Brush Set"
                        ),
                        MarketplaceAsset(
                            id = "asset_2",
                            title = "Warm Mid-Century Palette",
                            description = "Seamless HEX/ASE color swatches and palettes curated from retro danish dining rooms.",
                            imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=600&auto=format&fit=crop",
                            author = "Clara Dupont",
                            price = 50.0,
                            downloads = 850,
                            category = "Color Palette"
                        ),
                        MarketplaceAsset(
                            id = "asset_3",
                            title = "Grotesque Poster Template",
                            description = "Layered editable vector files designed in brutalist styles. Dynamic grids included.",
                            imageUrl = "https://images.unsplash.com/photo-1561070791-26c113006238?w=600&auto=format&fit=crop",
                            author = "Zoe Vance",
                            price = 180.0,
                            downloads = 390,
                            category = "Vector Layout"
                        ),
                        MarketplaceAsset(
                            id = "asset_4",
                            title = "Liquid Pastel Textures",
                            description = "15 beautiful ultra-HD holographic and liquid metallic backgrounds for mockups.",
                            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop",
                            author = "Aris Thorne",
                            price = 90.0,
                            downloads = 2100,
                            category = "Background Textures"
                        )
                    )
                )
            }

            // Populate some mock logs for collaborative boards
            _collaborationLogs.value = listOf(
                BoardActivity("act_1", 1L, "Yuki", "pinned 'Japandi Oak Serenity'", "2 min ago"),
                BoardActivity("act_2", 1L, "Clara", "added a comment on board chat", "10 min ago"),
                BoardActivity("act_3", 3L, "Dave", "pinned 'Neo-Retro Acid Editorial'", "1 hour ago"),
                BoardActivity("act_4", 3L, "Zoe", "joined the board", "3 hours ago"),
                BoardActivity("act_5", 1L, "Lars", "voted on color concept", "5 hours ago")
            )
        }
    }

    // Set Active Navigation Tab
    fun setTab(tab: String) {
        _currentTab.value = tab
        _activeBoard.value = null // reset board detail overlay
    }

    // Pin Selection
    fun selectPin(pin: DiscoverPin?) {
        _selectedPin.value = pin
    }

    // Board Selection Detail
    fun selectBoard(board: MoodBoard?) {
        _activeBoard.value = board
    }

    // Board CRUD
    fun createBoard(name: String, desc: String, isCollaborative: Boolean, collaborators: String) {
        viewModelScope.launch {
            val board = MoodBoard(
                name = name,
                description = desc,
                isCollaborative = isCollaborative,
                collaborators = if (isCollaborative) collaborators else "",
                coverImageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop" // decorative default
            )
            repository.insertBoard(board)
        }
    }

    fun deleteBoard(boardId: Long) {
        viewModelScope.launch {
            repository.deleteBoardById(boardId)
            if (_activeBoard.value?.id == boardId) {
                _activeBoard.value = null
            }
        }
    }

    // Saving Pins to Boards
    fun savePinToBoard(boardId: Long, title: String, desc: String, url: String, author: String, category: String, price: Double = 0.0, isFromMarketplace: Boolean = false) {
        viewModelScope.launch {
            val savedPin = SavedPin(
                boardId = boardId,
                title = title,
                description = desc,
                imageUrl = url,
                author = author,
                category = category,
                price = price,
                isFromMarketplace = isFromMarketplace
            )
            repository.insertSavedPin(savedPin)

            // Auto log collaboration activity if collaborative
            val targetBoard = boards.value.find { it.id == boardId }
            if (targetBoard != null && targetBoard.isCollaborative) {
                val newLog = BoardActivity(
                    id = "act_" + System.currentTimeMillis(),
                    boardId = boardId,
                    actor = "You",
                    action = "pinned '$title'",
                    relativeTime = "Just now"
                )
                _collaborationLogs.value = listOf(newLog) + _collaborationLogs.value
            }
        }
    }

    fun removeSavedPin(pinId: Long) {
        viewModelScope.launch {
            repository.deleteSavedPinById(pinId)
        }
    }

    // Interactive comments management (per-session state)
    fun addCommentToPin(pinId: String, text: String) {
        if (text.isBlank()) return
        val currentComments = _customPinComments.value[pinId] ?: emptyList()
        val updatedList = currentComments + Comment("You", text)
        _customPinComments.value = _customPinComments.value + (pinId to updatedList)

        // Update selected pin details with comments if it is currently viewed
        val currentSelected = _selectedPin.value
        if (currentSelected?.id == pinId) {
            _selectedPin.value = currentSelected.copy()
        }
    }

    // Purchase asset in marketplace
    fun buyMarketplaceAsset(asset: MarketplaceAsset, targetBoardId: Long?) {
        viewModelScope.launch {
            if (_userCredits.value >= asset.price.toInt()) {
                // Deduct credits
                _userCredits.value -= asset.price.toInt()

                // Mark as purchased in database
                repository.purchaseAsset(asset.id)

                // If user selected a board, automatically save it as a pin on that board!
                if (targetBoardId != null) {
                    savePinToBoard(
                        boardId = targetBoardId,
                        title = asset.title,
                        desc = asset.description,
                        url = asset.imageUrl,
                        author = asset.author,
                        category = asset.category,
                        price = asset.price,
                        isFromMarketplace = true
                    )
                }
            }
        }
    }

    // Get live comments combined with presets
    fun getCommentsForPin(pin: DiscoverPin): List<Comment> {
        val custom = _customPinComments.value[pin.id] ?: emptyList()
        return pin.comments + custom
    }

    // Collaborative boards: Add interactive collab chat comment
    fun addCollabComment(boardId: Long, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            val newLog = BoardActivity(
                id = "act_" + System.currentTimeMillis(),
                boardId = boardId,
                actor = "You",
                action = "commented: \"$commentText\"",
                relativeTime = "Just now"
            )
            _collaborationLogs.value = listOf(newLog) + _collaborationLogs.value
        }
    }

    // AI Style Recommendation using Gemini API
    fun generateAiStyle(boardDescription: String, boardName: String) {
        if (boardDescription.isBlank() && boardName.isBlank()) {
            _aiState.value = AiRecommendationState.Error("Please enter a style concept or board details.")
            return
        }

        _aiState.value = AiRecommendationState.Loading

        viewModelScope.launch {
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // Key is missing/placeholder. Provide a brilliant simulated recommendation!
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(2000) // simulated loading delay
                    val mockRec = generateSimulatedRecommendation(boardName, boardDescription)
                    _aiState.value = AiRecommendationState.Success(mockRec)
                }
                return@launch
            }

            try {
                val prompt = """
                    You are a professional design stylist assistant. Your job is to analyze a mood board concept and return a comprehensive, highly inspiring style recommendation in JSON format.
                    
                    Board Name: "$boardName"
                    Board Description: "$boardDescription"
                    
                    Return ONLY a JSON object that strictly adheres to this structure (do NOT include any other text or wrappers except the raw JSON itself):
                    {
                      "styleTheme": "Short descriptive name of style theme",
                      "themeDescription": "A paragraph outlining why this style fits and how to design with it.",
                      "recommendedColors": ["HEX_CODE_1", "HEX_CODE_2", "HEX_CODE_3", "HEX_CODE_4"],
                      "colorNames": ["Color Name 1", "Color Name 2", "Color Name 3", "Color Name 4"],
                      "typographyHeading": "Serif or display typography suggestion, e.g. Syne",
                      "typographyBody": "Clean body font suggestion, e.g. Space Grotesk",
                      "visualVibe": "Comma separated visual descriptors representing the aesthetic atmosphere",
                      "searchSuggestions": ["Suggestion 1", "Suggestion 2", "Suggestion 3"]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.7f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawText != null) {
                    val cleanedJson = cleanJson(rawText)
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(StyleRecommendation::class.java)
                    val rec = withContext(Dispatchers.Default) {
                        adapter.fromJson(cleanedJson)
                    }
                    if (rec != null) {
                        _aiState.value = AiRecommendationState.Success(rec)
                    } else {
                        _aiState.value = AiRecommendationState.Error("Could not parse style recommendation.")
                    }
                } else {
                    _aiState.value = AiRecommendationState.Error("No recommendations returned from Gemini.")
                }

            } catch (e: Exception) {
                // Log and fall back to custom preset recommendations
                val fallback = generateSimulatedRecommendation(boardName, boardDescription)
                _aiState.value = AiRecommendationState.Success(fallback)
            }
        }
    }

    private fun cleanJson(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    // High quality offline fallback generator for premium user experience
    private fun generateSimulatedRecommendation(name: String, desc: String): StyleRecommendation {
        val input = (name + " " + desc).lowercase()
        return when {
            input.contains("cyber") || input.contains("neon") || input.contains("punk") || input.contains("dark") -> {
                StyleRecommendation(
                    styleTheme = "Cyber-Industrial Neon Minimalist",
                    themeDescription = "A futuristic design approach combining stark high-contrast layouts, heavy gridded guidelines, digital scanlines, and glowing neon indicators against deep coal backdrops.",
                    recommendedColors = listOf("#0F0F11", "#00FF66", "#FF0055", "#1F31FF"),
                    colorNames = listOf("Deep Obsidian", "Neon Acid Green", "Electric Magenta", "Laser Beam Blue"),
                    typographyHeading = "Syne (Geometric Bold)",
                    typographyBody = "JetBrains Mono (Monospace)",
                    visualVibe = "raw grid lines, glow highlights, carbon surfaces, high-contrast typography, digital telemetry, high-tech asymmetry",
                    searchSuggestions = listOf("Industrial brutalist graphics", "Cyberpunk digital templates", "Retro-future neon interface")
                )
            }
            input.contains("japandi") || input.contains("wabi") || input.contains("organic") || input.contains("nature") || input.contains("seren") -> {
                StyleRecommendation(
                    styleTheme = "Japandi Organic Harmony",
                    themeDescription = "An peaceful, restorative blend of Japanese rustic wabi-sabi simplicity and Nordic clean functionality. Centered around warm, tactile wooden grains, soft natural shadows, and organic cotton textures.",
                    recommendedColors = listOf("#F4EFEA", "#D8C7B6", "#8F7D6D", "#3E3A37"),
                    colorNames = listOf("Raw Linen White", "Toasted Oatmeal", "Earth Clay", "Sumi Ink Gray"),
                    typographyHeading = "Playfair Display (Serif)",
                    typographyBody = "Inter (Sans-Serif)",
                    visualVibe = "tactile woven textures, ceramic pottery silhouette, soft diffused ambient light, warm neutral shadows, asymmetric spatial harmony",
                    searchSuggestions = listOf("Wabi-sabi minimalist spaces", "Organic interior textures", "Japanese beige color palette")
                )
            }
            input.contains("acid") || input.contains("retro") || input.contains("graphic") || input.contains("poster") -> {
                StyleRecommendation(
                    styleTheme = "Neo-Retro Acid Editorial",
                    themeDescription = "A bold, graphic-heavy style drawing inspiration from 1990s desktop publishing, underground music zines, and vivid chrome textures. Celebrates heavy display headings, deliberate overlapping margins, and high-impact acid green elements.",
                    recommendedColors = listOf("#0E0E0E", "#D8F343", "#FFFFFF", "#B800FF"),
                    colorNames = listOf("Absolute Void", "Acid Chartreuse", "Pristine Snow", "Psychedelic Violet"),
                    typographyHeading = "Clash Display (Grotesque)",
                    typographyBody = "Space Grotesk (Neo-Grotesque)",
                    visualVibe = "overlapping borders, fluid metallic chrome 3D, zine photocopier grain, extreme letter-spacing, asymmetric card layout",
                    searchSuggestions = listOf("Swiss neo-brutalist layouts", "Chrome 3D abstract fluid art", "1990s poster design templates")
                )
            }
            else -> {
                // Default elegant design recommend
                StyleRecommendation(
                    styleTheme = "Modern Editorial Sophistication",
                    themeDescription = "A balanced aesthetic focusing on refined luxury, clean negative space, high contrast, and classic typography pairing. Perfect for high-end fashion, architecture portfolios, and digital asset marketplaces.",
                    recommendedColors = listOf("#FAF8F5", "#E31E24", "#262626", "#B99E74"),
                    colorNames = listOf("Champagne Ivory", "Editorial Crimson", "Rich Charcoal", "Muted Brass Gold"),
                    typographyHeading = "Cormorant Garamond (Serif)",
                    typographyBody = "Cabinet Grotesk (Sans)",
                    visualVibe = "expansive white spaces, crisp editorial borders, rich crimson focus points, fine line accents, geometric column balance",
                    searchSuggestions = listOf("High-end editorial layout", "Modernist interior detail", "French chic color design")
                )
            }
        }
    }
}
