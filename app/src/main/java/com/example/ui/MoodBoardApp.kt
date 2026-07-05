package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.MarketplaceAsset
import com.example.data.MoodBoard
import com.example.data.SavedPin
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodBoardApp(
    viewModel: MoodBoardViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userCredits by viewModel.userCredits.collectAsStateWithLifecycle()
    val boards by viewModel.boards.collectAsStateWithLifecycle()
    val savedPins by viewModel.savedPins.collectAsStateWithLifecycle()
    val selectedPin by viewModel.selectedPin.collectAsStateWithLifecycle()
    val activeBoard by viewModel.activeBoard.collectAsStateWithLifecycle()

    var showCreateBoardDialog by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf<MarketplaceAsset?>(null) }
    var showCreditInfoDialog by remember { mutableStateOf(false) }

    // Active category filter for Discover Feed
    var activeCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "MoodPin AI",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Credit balance indicator
                    Card(
                        onClick = { showCreditInfoDialog = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MarketplaceGold.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("credits_badge"),
                        border = BorderStroke(1.dp, MarketplaceGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Savings,
                                contentDescription = "Credits icon",
                                tint = MarketplaceGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$userCredits cr",
                                fontWeight = FontWeight.Bold,
                                color = MarketplaceGold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Profile placeholder
                    IconButton(
                        onClick = { showCreditInfoDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PinterestRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                fontWeight = FontWeight.Bold,
                                color = PinterestRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == "discover" && activeBoard == null,
                    onClick = { viewModel.setTab("discover") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "discover") Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Discover Tab"
                        )
                    },
                    label = { Text("Discover") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PinterestRed,
                        selectedTextColor = PinterestRed,
                        indicatorColor = PinterestRed.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_discover")
                )

                NavigationBarItem(
                    selected = currentTab == "boards" || activeBoard != null,
                    onClick = { viewModel.setTab("boards") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "boards") Icons.Filled.FolderCopy else Icons.Outlined.FolderCopy,
                            contentDescription = "Boards Tab"
                        )
                    },
                    label = { Text("Mood Boards") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PinterestRed,
                        selectedTextColor = PinterestRed,
                        indicatorColor = PinterestRed.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_boards")
                )

                NavigationBarItem(
                    selected = currentTab == "stylist",
                    onClick = { viewModel.setTab("stylist") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "stylist") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Stylist Tab"
                        )
                    },
                    label = { Text("AI Stylist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PinterestRed,
                        selectedTextColor = PinterestRed,
                        indicatorColor = PinterestRed.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_stylist")
                )

                NavigationBarItem(
                    selected = currentTab == "marketplace",
                    onClick = { viewModel.setTab("marketplace") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "marketplace") Icons.Filled.LocalMall else Icons.Outlined.LocalMall,
                            contentDescription = "Marketplace Tab"
                        )
                    },
                    label = { Text("Marketplace") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PinterestRed,
                        selectedTextColor = PinterestRed,
                        indicatorColor = PinterestRed.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_marketplace")
                )
            }
        },
        floatingActionButton = {
            // Show FAB to create a board when looking at boards list
            if (currentTab == "boards" && activeBoard == null) {
                FloatingActionButton(
                    onClick = { showCreateBoardDialog = true },
                    containerColor = PinterestRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("fab_create_board")
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create Board")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Board", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cross-tab switching with proper overlays
            when {
                activeBoard != null -> {
                    BoardDetailView(
                        board = activeBoard!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectBoard(null) }
                    )
                }
                currentTab == "discover" -> {
                    DiscoverFeedView(
                        viewModel = viewModel,
                        activeCategory = activeCategory,
                        onCategorySelect = { activeCategory = it },
                        onPinClick = { viewModel.selectPin(it) }
                    )
                }
                currentTab == "boards" -> {
                    BoardsListView(
                        boards = boards,
                        savedPins = savedPins,
                        onBoardClick = { viewModel.selectBoard(it) }
                    )
                }
                currentTab == "stylist" -> {
                    AiStylistView(
                        viewModel = viewModel,
                        boards = boards,
                        onNavigateToDiscoverWithQuery = { query ->
                            activeCategory = "All"
                            viewModel.setTab("discover")
                            Toast.makeText(context, "Searching Discover: $query", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                currentTab == "marketplace" -> {
                    AssetMarketplaceView(
                        viewModel = viewModel,
                        onUnlockClick = { asset -> showPurchaseDialog = asset }
                    )
                }
            }

            // Pin Detail Overlay Dialog
            if (selectedPin != null) {
                PinDetailDialog(
                    pin = selectedPin!!,
                    boards = boards,
                    viewModel = viewModel,
                    onDismiss = { viewModel.selectPin(null) }
                )
            }

            // Create Mood Board Dialog
            if (showCreateBoardDialog) {
                CreateBoardDialog(
                    onDismiss = { showCreateBoardDialog = false },
                    onConfirm = { name, desc, isCollab, collabs ->
                        viewModel.createBoard(name, desc, isCollab, collabs)
                        showCreateBoardDialog = false
                        Toast.makeText(context, "Board created successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Purchase confirmation dialog
            if (showPurchaseDialog != null) {
                PurchaseAssetDialog(
                    asset = showPurchaseDialog!!,
                    boards = boards,
                    userCredits = userCredits,
                    onDismiss = { showPurchaseDialog = null },
                    onConfirm = { targetBoardId ->
                        viewModel.buyMarketplaceAsset(showPurchaseDialog!!, targetBoardId)
                        Toast.makeText(context, "Asset unlocked and saved to board!", Toast.LENGTH_LONG).show()
                        showPurchaseDialog = null
                    }
                )
            }

            // Credit Balance Info Dialog
            if (showCreditInfoDialog) {
                CreditInfoDialog(
                    currentBalance = userCredits,
                    onDismiss = { showCreditInfoDialog = false }
                )
            }
        }
    }
}

// ==========================================
// 1. DISCOVER FEED (PINTEREST CLONE CORNER)
// ==========================================
@Composable
fun DiscoverFeedView(
    viewModel: MoodBoardViewModel,
    activeCategory: String,
    onCategorySelect: (String) -> Unit,
    onPinClick: (DiscoverPin) -> Unit
) {
    val categories = listOf("All", "Interior", "Fashion", "Graphic Design", "Architecture", "Illustration")

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal scrollable category filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == activeCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PinterestRed,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) PinterestRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Waterfall / Staggered Grid of Pins
        val filteredPins = remember(activeCategory) {
            if (activeCategory == "All") {
                viewModel.discoverPins
            } else {
                viewModel.discoverPins.filter { it.category == activeCategory }
            }
        }

        if (filteredPins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Empty list",
                        tint = GrayMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No pins available in this style category yet.",
                        color = GrayMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp
            ) {
                items(filteredPins) { pin ->
                    PinGridCard(
                        pin = pin,
                        onClick = { onPinClick(pin) }
                    )
                }
            }
        }
    }
}

@Composable
fun PinGridCard(
    pin: DiscoverPin,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("pin_card_${pin.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Elegant asymmetric ratio image display
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = pin.imageUrl,
                    contentDescription = pin.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.FillWidth
                )

                // Category overlay chip
                Text(
                    text = pin.category,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // Title and author footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = pin.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(PinterestRed.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pin.author.firstOrNull()?.toString() ?: "?",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinterestRed
                        )
                    }
                    Text(
                        text = pin.author,
                        fontSize = 11.sp,
                        color = GrayMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. PIN DETAIL AND COLLABORATIVE COMMENTING
// ==========================================
@Composable
fun PinDetailDialog(
    pin: DiscoverPin,
    boards: List<MoodBoard>,
    viewModel: MoodBoardViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentComments = viewModel.getCommentsForPin(pin)
    var commentText by remember { mutableStateOf("") }
    var expandedBoardDropdown by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(pin.initialLikes) }
    var hasLiked by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("pin_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable container for detail + comments
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Close button overlay on top image
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = pin.imageUrl,
                            contentDescription = pin.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Title and Save Board row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pin.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinterestRed,
                                letterSpacing = 1.sp
                            )

                            // Dropdown/Button to Save Pin to Board
                            Box {
                                Button(
                                    onClick = { expandedBoardDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("save_to_board_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Pin Icon",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedBoardDropdown,
                                    onDismissRequest = { expandedBoardDropdown = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    if (boards.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No boards found. Create one first!") },
                                            onClick = { expandedBoardDropdown = false }
                                        )
                                    } else {
                                        boards.forEach { board ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (board.isCollaborative) Icons.Filled.Group else Icons.Filled.Folder,
                                                            contentDescription = "Board icon",
                                                            tint = if (board.isCollaborative) CollaborativeBlue else GrayMuted,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(board.name)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.savePinToBoard(
                                                        boardId = board.id,
                                                        title = pin.title,
                                                        desc = pin.description,
                                                        url = pin.imageUrl,
                                                        author = pin.author,
                                                        category = pin.category
                                                    )
                                                    expandedBoardDropdown = false
                                                    Toast.makeText(
                                                        context,
                                                        "Saved to board '${board.name}'!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pin.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Creator line
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(PinterestRed.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pin.author.first().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = PinterestRed,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "By ${pin.author}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = pin.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive likes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (hasLiked) {
                                        likesCount--
                                    } else {
                                        likesCount++
                                    }
                                    hasLiked = !hasLiked
                                }
                            ) {
                                Icon(
                                    imageVector = if (hasLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like pin",
                                    tint = if (hasLiked) PinterestRed else GrayMuted
                                )
                            }
                            Text(
                                text = "$likesCount likes",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Comment Log header
                        Text(
                            text = "Comments (${currentComments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (currentComments.isEmpty()) {
                            Text(
                                text = "Be the first to share feedback or ask a designer!",
                                fontSize = 12.sp,
                                color = GrayMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentComments) { comment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(PinterestRed.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = comment.author.first().toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PinterestRed
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = comment.author,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = comment.text,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Comment input footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write comment...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.addCommentToPin(pin.id, commentText)
                                commentText = ""
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.addCommentToPin(pin.id, commentText)
                            commentText = ""
                        },
                        modifier = Modifier.testTag("send_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Post Comment",
                            tint = PinterestRed
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MOOD BOARDS GALLERY
// ==========================================
@Composable
fun BoardsListView(
    boards: List<MoodBoard>,
    savedPins: List<SavedPin>,
    onBoardClick: (MoodBoard) -> Unit
) {
    if (boards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.FolderCopy,
                    contentDescription = "No boards",
                    tint = GrayMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "You haven't created any boards yet.\nTap 'Create Board' to set up a mood board!",
                    color = GrayMuted,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("mood_boards_grid"),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(boards) { board ->
                val boardPinsCount = savedPins.count { it.boardId == board.id }
                BoardCard(
                    board = board,
                    pinCount = boardPinsCount,
                    onClick = { onBoardClick(board) }
                )
            }
        }
    }
}

@Composable
fun BoardCard(
    board: MoodBoard,
    pinCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("board_card_${board.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Elegant layered cover visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color.LightGray)
            ) {
                if (board.coverImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = board.coverImageUrl,
                        contentDescription = board.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(PinterestRed.copy(alpha = 0.4f), CollaborativeBlue.copy(alpha = 0.4f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Grid4x4,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Board badge tag overlay
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomStart)
                ) {
                    if (board.isCollaborative) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(CollaborativeBlue, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = "Collab",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collab", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Lock",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Private", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Board Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = board.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$pinCount items saved",
                    fontSize = 12.sp,
                    color = GrayMuted
                )

                if (board.isCollaborative && board.collaborators.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "With: ${board.collaborators}",
                        fontSize = 10.sp,
                        color = CollaborativeBlue,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. ACTIVE BOARD DETAIL (COLLABORATION VIEW)
// ==========================================
@Composable
fun BoardDetailView(
    board: MoodBoard,
    viewModel: MoodBoardViewModel,
    onBack: () -> Unit
) {
    val savedPins by viewModel.savedPins.collectAsStateWithLifecycle()
    val collaborationLogs by viewModel.collaborationLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val boardPins = remember(savedPins) {
        savedPins.filter { it.boardId == board.id }
    }

    val activeLogs = remember(collaborationLogs) {
        collaborationLogs.filter { it.boardId == board.id }
    }

    var collabMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero top card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AsyncImage(
                model = board.coverImageUrl.ifEmpty { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop" },
                contentDescription = board.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Blur gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .align(Alignment.TopStart)
                    .testTag("board_back_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Title bottom details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = board.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (board.isCollaborative) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = "Collab",
                            tint = CollaborativeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = board.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Section: Grid list of items saved on this board
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Mood Assets (${boardPins.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Delete Board Action
                TextButton(
                    onClick = {
                        viewModel.deleteBoard(board.id)
                        Toast.makeText(context, "Board deleted.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PinterestRed)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Board")
                }
            }

            if (boardPins.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.PushPin, contentDescription = null, tint = GrayMuted, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This board is currently empty. Pin designs from the discover feed or marketplace!",
                            color = GrayMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Flow-like grid of saved pins
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(boardPins) { pin ->
                        SavedPinCard(
                            pin = pin,
                            onRemove = { viewModel.removeSavedPin(pin.id) }
                        )
                    }
                }
            }

            // ==========================================
            // COLLABORATIVE ACTIVITY LOGS & DISCUSSION
            // ==========================================
            if (board.isCollaborative) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Forum,
                        contentDescription = "Collab feed",
                        tint = CollaborativeBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collaborator Board Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CollaborativeBlue
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time logging of design votes, edits, and pin recommendations with: ${board.collaborators}",
                    fontSize = 11.sp,
                    color = GrayMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Activity Logs List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        activeLogs.take(6).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(CollaborativeBlue.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = log.actor.first().toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CollaborativeBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${log.actor} ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = log.action,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = log.relativeTime,
                                    fontSize = 10.sp,
                                    color = GrayMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive collaborative message posting
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = collabMessageText,
                                onValueChange = { collabMessageText = it },
                                placeholder = { Text("Comment on the board...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("collab_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    viewModel.addCollabComment(board.id, collabMessageText)
                                    collabMessageText = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CollaborativeBlue)
                            ) {
                                Text("Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPinCard(
    pin: SavedPin,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = pin.imageUrl,
                contentDescription = pin.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Removal button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Remove Pin",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom title card banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = pin.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (pin.isFromMarketplace) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MarketplaceGold, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Asset Unlock", color = MarketplaceGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. AI STYLE RECOMMENDATIONS (GEMINI SYSTEM)
// ==========================================
@Composable
fun AiStylistView(
    viewModel: MoodBoardViewModel,
    boards: List<MoodBoard>,
    onNavigateToDiscoverWithQuery: (String) -> Unit
) {
    var boardSelectionName by remember { mutableStateOf("") }
    var boardSelectionDesc by remember { mutableStateOf("") }
    var customConceptText by remember { mutableStateOf("") }
    var expandedBoardsMenu by remember { mutableStateOf(false) }

    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // AI Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PinterestRed.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, PinterestRed.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PinterestRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "AI Design Stylist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PinterestRed
                    )
                    Text(
                        "Analyze your mood boards with Gemini 3.5 Flash to generate custom style recommendations, copy design guidelines, swatches, and prompts.",
                        fontSize = 11.sp,
                        color = GrayMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Select Active Mood Board
        Text(
            "1. Choose a board or design context",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedBoardsMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("select_board_dropdown"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (boardSelectionName.isEmpty()) "Select a mood board..." else boardSelectionName,
                        color = if (boardSelectionName.isEmpty()) GrayMuted else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown indicator",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            DropdownMenu(
                expanded = expandedBoardsMenu,
                onDismissRequest = { expandedBoardsMenu = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("[Custom Prompt Only]") },
                    onClick = {
                        boardSelectionName = "Custom Concept Space"
                        boardSelectionDesc = ""
                        expandedBoardsMenu = false
                    }
                )
                boards.forEach { board ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Folder, contentDescription = null, tint = GrayMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(board.name)
                            }
                        },
                        onClick = {
                            boardSelectionName = board.name
                            boardSelectionDesc = board.description
                            expandedBoardsMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Custom style notes / descriptors
        Text(
            "2. Optional: Add specific style prompt or vibes",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = customConceptText,
            onValueChange = { customConceptText = it },
            placeholder = { Text("E.g., High-contrast editorial style with neon acid elements or mid-century retro vibes", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_prompt_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinterestRed,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Generate Action Button
        Button(
            onClick = {
                val finalDesc = if (boardSelectionDesc.isEmpty()) customConceptText else "$boardSelectionDesc. $customConceptText"
                viewModel.generateAiStyle(finalDesc, boardSelectionName)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_recommendations_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Text("Generate AI Stylist Dossier", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result States (Loading/Success/Error)
        AnimatedContent(targetState = aiState) { state ->
            when (state) {
                is AiRecommendationState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = GrayMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select details above to generate an AI design portfolio.", color = GrayMuted, fontSize = 12.sp)
                        }
                    }
                }
                is AiRecommendationState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = PinterestRed, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Gemini is curating color palettes...", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Assembling typography recommendations and design guidelines.", color = GrayMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                is AiRecommendationState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp
                        )
                    }
                }
                is AiRecommendationState.Success -> {
                    val rec = state.recommendation
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "AI DESIGN STYLE DOSSIER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinterestRed,
                            letterSpacing = 1.5.sp
                        )

                        // Main Style Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = rec.styleTheme,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = rec.themeDescription,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Color swatches row
                        Text(
                            text = "Interactive Swatches (Tap to copy HEX)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GrayMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rec.recommendedColors.forEachIndexed { index, hex ->
                                val colorName = rec.colorNames.getOrNull(index) ?: "Color"
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(hex))
                                            Toast.makeText(context, "$hex Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = hex,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = colorName,
                                        fontSize = 9.sp,
                                        color = GrayMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Visual atmosphere tags
                        Text(
                            text = "Visual Atmosphere Markers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GrayMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rec.visualVibe.split(",").forEach { tag ->
                                Text(
                                    text = tag.trim(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Typography pairings
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Typography Pairing recommendation:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrayMuted)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("HEADING FONT", fontSize = 9.sp, color = GrayMuted)
                                        Text(rec.typographyHeading, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("BODY COPY FONT", fontSize = 9.sp, color = GrayMuted)
                                        Text(rec.typographyBody, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Action search prompts
                        Text(
                            text = "Search Prompts (Tap to query Discover board)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GrayMuted
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            rec.searchSuggestions.forEach { search ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDiscoverWithQuery(search) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Search, contentDescription = null, tint = PinterestRed, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(search, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = GrayMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. ASSET MARKETPLACE
// ==========================================
@Composable
fun AssetMarketplaceView(
    viewModel: MoodBoardViewModel,
    onUnlockClick: (MarketplaceAsset) -> Unit
) {
    val assets by viewModel.marketplaceAssets.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Gold banner for digital assets
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MarketplaceGold.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, MarketplaceGold.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MarketplaceGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Design Asset Marketplace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MarketplaceGold
                    )
                    Text(
                        "Unlock premium templates, brush sets, palettes, or design guides with credits and immediately pin them to your mood boards.",
                        fontSize = 11.sp,
                        color = GrayMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Premium Digital Assets",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(assets) { asset ->
                AssetGridCard(
                    asset = asset,
                    onActionClick = {
                        if (asset.isPurchased) {
                            // Already bought. Trigger simulated download.
                            Toast.makeText(context, "Downloaded ${asset.title} to storage!", Toast.LENGTH_SHORT).show()
                        } else {
                            onUnlockClick(asset)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AssetGridCard(
    asset: MarketplaceAsset,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .testTag("asset_card_${asset.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                AsyncImage(
                    model = asset.imageUrl,
                    contentDescription = asset.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Category overlay chip
                Text(
                    text = asset.category.uppercase(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .align(Alignment.TopStart)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = asset.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "By ${asset.author}",
                    fontSize = 10.sp,
                    color = GrayMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MarketplaceGold, modifier = Modifier.size(10.dp))
                    Text(text = "${asset.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "(${asset.downloads} dl)", fontSize = 9.sp, color = GrayMuted)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (asset.isPurchased) {
                    OutlinedButton(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, PinterestRed)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = PinterestRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PinterestRed)
                        }
                    }
                } else {
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("unlock_asset_button_${asset.id}"),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarketplaceGold)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${asset.price.toInt()} cr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. DIALOGS & OVERLAYS
// ==========================================
@Composable
fun CreateBoardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, isCollab: Boolean, collaborators: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var isCollaborative by remember { mutableStateOf(false) }
    var collaborators by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("create_board_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create New Mood Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Board Name") },
                    modifier = Modifier.fillMaxWidth().testTag("create_board_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Collaborative Board", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Allow other designers to save items & discuss.", fontSize = 11.sp, color = GrayMuted)
                    }
                    Switch(
                        checked = isCollaborative,
                        onCheckedChange = { isCollaborative = it },
                        modifier = Modifier.testTag("collaborative_switch")
                    )
                }

                if (isCollaborative) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = collaborators,
                        onValueChange = { collaborators = it },
                        label = { Text("Collaborators") },
                        placeholder = { Text("E.g., Yuki, Dave, Zoe") },
                        modifier = Modifier.fillMaxWidth().testTag("collaborators_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, desc, isCollaborative, collaborators) },
                        colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                        modifier = Modifier.testTag("confirm_create_board_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseAssetDialog(
    asset: MarketplaceAsset,
    boards: List<MoodBoard>,
    userCredits: Int,
    onDismiss: () -> Unit,
    onConfirm: (boardId: Long?) -> Unit
) {
    var expandedBoardsMenu by remember { mutableStateOf(false) }
    var selectedBoardId by remember { mutableStateOf<Long?>(null) }
    var selectedBoardName by remember { mutableStateOf("Do not save to board") }

    val hasSufficientBalance = userCredits >= asset.price.toInt()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("purchase_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Unlock Digital Asset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = asset.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(asset.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Author: ${asset.author}", fontSize = 11.sp, color = GrayMuted)
                        Text("Cost: ${asset.price.toInt()} credits", fontWeight = FontWeight.Bold, color = MarketplaceGold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (hasSufficientBalance) {
                    Text(
                        text = "Choose a board to automatically pin this unlocked asset onto:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedBoardsMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedBoardName, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedBoardsMenu,
                            onDismissRequest = { expandedBoardsMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f).background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Do not save to board") },
                                onClick = {
                                    selectedBoardId = null
                                    selectedBoardName = "Do not save to board"
                                    expandedBoardsMenu = false
                                }
                            )
                            boards.forEach { board ->
                                DropdownMenuItem(
                                    text = { Text(board.name) },
                                    onClick = {
                                        selectedBoardId = board.id
                                        selectedBoardName = board.name
                                        expandedBoardsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onConfirm(selectedBoardId) },
                            colors = ButtonDefaults.buttonColors(containerColor = MarketplaceGold),
                            modifier = Modifier.testTag("confirm_purchase_button")
                        ) {
                            Text("Confirm Unlock", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Text(
                        text = "Insufficient Credits! You need ${asset.price.toInt() - userCredits} more credits to unlock this asset. Try saving more items to your mood boards or look at other assets.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GrayMuted)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CreditInfoDialog(
    currentBalance: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Simulated Credits Portfolio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MarketplaceGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You currently have $currentBalance credits in your wallet! Credits allow you to unlock professional digital assets in the Integrated Asset Marketplace.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "💡 How to earn credits:\n- Create mood boards to earn points\n- Analyze boards with the AI design stylist\n- Engage in board discussions with collaborators",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = GrayMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MarketplaceGold)
                ) {
                    Text("Awesome!", color = Color.White)
                }
            }
        }
    }
}
