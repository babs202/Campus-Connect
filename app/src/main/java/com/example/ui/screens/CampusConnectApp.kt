package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusConnectApp(
    viewModel: CampusConnectViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginSignupPortal(viewModel = viewModel)
        return
    }

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUserAccount by viewModel.currentUserAccount.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val activeChatPartnerEmail by viewModel.activeChatPartnerEmail.collectAsStateWithLifecycle()

    var showActiveNotifications by remember { mutableStateOf(false) }
    var showWalletTopUp by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Prominent, styled USTED Logo Badge with Blue & Green Gradient Accent
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "USTED Symbol",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "USTED UNIVERSITY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Campus Connect",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    // Mobile wallet capsule
                    Card(
                        onClick = { showWalletTopUp = true },
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("wallet_capsule_topbar")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GHS ${String.format(Locale.US, "%.2f", currentUserAccount?.cashBalance ?: 0.0)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Theme toggle
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // Notifications bell with badge indicator
                    val systemNotifications by viewModel.simulatedNotifications.collectAsStateWithLifecycle()
                    IconButton(onClick = { showActiveNotifications = true }) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                            if (systemNotifications.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, shape = CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    // Logout Action
                    IconButton(
                        onClick = { viewModel.logoutUser() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout Securely",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Adaptive design check - narrow screens show beautiful bottom developer contact footer
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp
            if (screenWidth < 600) {
                Column {
                    DeveloperContactFooter()
                }
            }
        }
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val isWideScreen = configuration.screenWidthDp >= 600

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Wide Screen Side Navigation Rail - Locked to Active Role info and Logout quick keys
            if (isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display Active Portal Profile Indicator Capsule
                    Text(
                        text = currentRole,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Edit Profile quick toggle
                    IconButton(onClick = { showProfileEditor = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Edit Profile")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Side rails logout button
                    IconButton(onClick = { viewModel.logoutUser() }) {
                        Icon(
                            imageVector = Icons.Default.Logout, 
                            contentDescription = "Log out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Main Contents Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedContent(
                    targetState = currentRole,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "DashboardTransition"
                ) { role ->
                    when (role) {
                        "STUDENT" -> StudentDashboardScreen(viewModel = viewModel, onEditProfile = { showProfileEditor = true })
                        "PROVIDER" -> ProviderDashboardScreen(viewModel = viewModel)
                        "ADMIN" -> AdminDashboardScreen(viewModel = viewModel)
                        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Unknown account state")
                        }
                    }
                }

                // In-App Chat Dialog Overlay (Acts like real-time drawer!)
                if (activeChatPartnerEmail != null) {
                    ChatDrawerOverlay(
                        viewModel = viewModel,
                        onClose = { viewModel.closeChat() }
                    )
                }
            }
        }
    }

    // --- OVERLAY WINDOWS ---

    // 1. Mobile Money Checkout Overlays (OTP verification simulator)
    if (checkoutState != null) {
        MomoCheckoutModal(
            checkoutState = checkoutState!!,
            onConfirmOtp = { otp -> viewModel.confirmMomoPayment(otp) },
            onCancel = { viewModel.closeCheckout() }
        )
    }

    // 2. Simulated Notifications Log Window
    if (showActiveNotifications) {
        NotificationsModal(
            viewModel = viewModel,
            onClose = { showActiveNotifications = false }
        )
    }

    // 3. Simulated Wallet Top-up Modal
    if (showWalletTopUp) {
        WalletTopUpModal(
            viewModel = viewModel,
            onClose = { showWalletTopUp = false }
        )
    }

    // 4. Custom Profile Editor Dialog
    if (showProfileEditor) {
        ProfileEditorModal(
            viewModel = viewModel,
            onClose = { showProfileEditor = false }
        )
    }
}

// ==========================================
// 1. STUDENT DASHBOARD SCREEN IMPLEMENTATION
// ==========================================
@Composable
fun StudentDashboardScreen(
    viewModel: CampusConnectViewModel,
    onEditProfile: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val filterVerifiedOnly by viewModel.filterVerifiedOnly.collectAsStateWithLifecycle()
    val filterHighestRated by viewModel.filterHighestRated.collectAsStateWithLifecycle()

    val studentBookings by viewModel.studentBookings.collectAsStateWithLifecycle()
    var currentTabSelected by remember { mutableStateOf("home") } // "home", "bookings", "messages"

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab switcher
        TabRow(
            selectedTabIndex = if (currentTabSelected == "home") 0 else if (currentTabSelected == "bookings") 1 else 2,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = currentTabSelected == "home",
                onClick = { currentTabSelected = "home" },
                icon = { Icon(Icons.Default.Explore, contentDescription = "Explore Services") },
                text = { Text("Explore", fontSize = 13.sp) }
            )
            Tab(
                selected = currentTabSelected == "bookings",
                onClick = { currentTabSelected = "bookings" },
                icon = { Icon(Icons.Default.EventNote, contentDescription = "Your Bookings") },
                text = { Text("My Bookings", fontSize = 13.sp) }
            )
            Tab(
                selected = currentTabSelected == "messages",
                onClick = { currentTabSelected = "messages" },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Active chats") },
                text = { Text("Chats", fontSize = 13.sp) }
            )
        }

        when (currentTabSelected) {
            "home" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    // USTED Hero banner overlay as a distinctive layout!
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Generated Hero Image
                                Image(
                                    painter = painterResource(id = R.drawable.img_usted_hero),
                                    contentDescription = "USTED Connect Campus Marketplace Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Gradient brush highlight as requested
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF0D47A1).copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )
                                // Content text
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "Akwaba! Welcome to USTED Connect",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Search & book trusted, verified services provided by skills-focused entrepreneurs inside USTED Kumasi campus.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Search and discovery filters - fully styled M3 Text field
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_services_input"),
                                placeholder = { Text("Search tutors, braids, printers, jollof...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear query")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            // Checkbox and ratings pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = filterVerifiedOnly,
                                    onClick = { viewModel.toggleVerifiedFilter() },
                                    label = { Text("Verified Providers", fontSize = 11.sp) },
                                    leadingIcon = {
                                        if (filterVerifiedOnly) Icon(Icons.Default.Check, "Checked", modifier = Modifier.size(14.dp))
                                        else Icon(Icons.Default.Shield, "Verified", modifier = Modifier.size(14.dp))
                                    }
                                )
                                FilterChip(
                                    selected = filterHighestRated,
                                    onClick = { viewModel.toggleRatingFilter() },
                                    label = { Text("★ Highest Rated (4.7+)", fontSize = 11.sp) },
                                    leadingIcon = {
                                        if (filterHighestRated) Icon(Icons.Default.Check, "Checked", modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                        }
                    }

                    // Categories Dynamic Carousel Row
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Quick Services",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    val isSelected = selectedCategory == "all"
                                    val isDark = isSystemInDarkTheme()
                                    val unselectedBg = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
                                    val unselectedText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                    val selectedBg = MaterialTheme.colorScheme.primary
                                    val selectedText = Color.White

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { viewModel.setCategoryFilter("all") }
                                            .testTag("category_filter_all")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(
                                                    color = if (isSelected) selectedBg else unselectedBg,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) selectedBg else Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(16.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = "All Services",
                                                tint = if (isSelected) selectedText else unselectedText,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "All",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                items(categories) { cat ->
                                    val isSelected = selectedCategory == cat.id
                                    val isDark = isSystemInDarkTheme()
                                    val (unselectedBg, unselectedText) = when (cat.id) {
                                        "academic" -> if (isDark) Pair(Color(0xFF1E3A8A), Color(0xFF93C5FD)) else Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                                        "essentials" -> if (isDark) Pair(Color(0xFF064E3B), Color(0xFF6EE7B7)) else Pair(Color(0xFFD1FAE5), Color(0xFF047857))
                                        "personal" -> if (isDark) Pair(Color(0xFF7C2D12), Color(0xFFFDBA74)) else Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
                                        "food" -> if (isDark) Pair(Color(0xFF581C87), Color(0xFFD8B4FE)) else Pair(Color(0xFFF3E8FF), Color(0xFF7E22CE))
                                        "creative" -> if (isDark) Pair(Color(0xFF0C4A6E), Color(0xFF7DD3FC)) else Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
                                        "tech" -> if (isDark) Pair(Color(0xFF831843), Color(0xFFF9A8D4)) else Pair(Color(0xFFFCE7F3), Color(0xFFBE185D))
                                        "events" -> if (isDark) Pair(Color(0xFF78350F), Color(0xFFFCD34D)) else Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                                        else -> if (isDark) Pair(Color(0xFF334155), Color(0xFFCBD5E1)) else Pair(Color(0xFFF1F5F9), Color(0xFF475569))
                                    }
                                    val selectedBg = MaterialTheme.colorScheme.primary
                                    val selectedText = Color.White

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { viewModel.setCategoryFilter(cat.id) }
                                            .testTag("category_filter_${cat.id}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(
                                                    color = if (isSelected) selectedBg else unselectedBg,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) selectedBg else Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(16.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconByName(cat.iconName),
                                                contentDescription = cat.name,
                                                tint = if (isSelected) selectedText else unselectedText,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = cat.name.split(" ")[0],
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Providers (${services.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Empty State feedback as mandated in design skill
                    if (services.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = "Empty list",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No providers match with current filters",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Try clearing search or toggling the verified filters.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Grid-like listing of services
                    items(services) { service ->
                        ServiceListingCard(service = service, viewModel = viewModel)
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }

            "bookings" -> {
                // Bookings view list
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "My Booking Contracts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Track dates, service progress and submit review ratings upon completion",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (studentBookings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Text(
                                    "No Booking Orders Registered",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    "Explore the USTED categories to hire your first service!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(studentBookings) { booking ->
                                StudentBookingItemCard(booking = booking, viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            "messages" -> {
                // Chats listing screen
                val chatContacts by viewModel.chatContacts.collectAsStateWithLifecycle()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Campus Conversations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (chatContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Text(
                                    "No active conversations",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    "Tap 'Chat' icon on any service listing to message providers.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(chatContacts) { contact ->
                                Card(
                                    onClick = { viewModel.startChatWith(contact.email) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.fullName.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = contact.fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                if (contact.isPremium) {
                                                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                                        Text("Verified Pro", fontSize = 9.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                            Text(
                                                text = contact.email,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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
}

// Service Card component with booking triggers
@Composable
fun ServiceListingCard(
    service: ServiceListing,
    viewModel: CampusConnectViewModel
) {
    var showBookingForm by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_listing_card_${service.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category & verification badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = service.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (service.isPremium) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5),
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PREMIUM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFDBA74) else Color(0xFFC2410C),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${service.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "(${service.reviewCount})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = service.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Provider description
            Text(
                text = "by ${service.providerName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description body
            Text(
                text = service.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom section: Location & pricing details with actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = service.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "GHS ${String.format(Locale.US, "%.2f", service.priceGHS)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) Color(0xFF34D399) else Color(0xFF047857) // Emerald-600 / Mint green
                    )
                    Text(
                        text = service.unitText,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    onClick = { viewModel.startChatWith(service.providerEmail) },
                    modifier = Modifier.weight(0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with provider",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Button(
                    onClick = { showBookingForm = true },
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("book_button_${service.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book Instantly", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Interactive booking dynamic form modal
    if (showBookingForm) {
        BookingFormModal(
            service = service,
            onSubmit = { date, time, notes, carrier, phone ->
                showBookingForm = false
                viewModel.createBooking(service, date, time, notes, carrier, phone)
            },
            onClose = { showBookingForm = false }
        )
    }
}

// Student Booking card row
@Composable
fun StudentBookingItemCard(
    booking: Booking,
    viewModel: CampusConnectViewModel
) {
    var showReviewModal by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${booking.paymentTxRef}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Color coded state pill
                Box(
                    modifier = Modifier
                        .background(
                            color = getBookingColor(booking.status).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = getBookingColor(booking.status),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = booking.serviceTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Provider: ${booking.providerName} (${booking.providerEmail})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Timing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = booking.dateText, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = booking.timeText, fontSize = 11.sp)
                }
            }

            if (booking.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${booking.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GHS ${String.format(Locale.US, "%.2f", booking.priceGHS)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Paid securely via ${booking.paymentMethod}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chat option with provider directly
                    IconButton(onClick = { viewModel.startChatWith(booking.providerEmail) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat with provider",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Complete / Leave Review rating action
                    if (booking.status == "COMPLETED" && !booking.notes.contains("[Reviewed]")) {
                        Button(
                            onClick = { showReviewModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("rate_booking_btn_${booking.id}")
                        ) {
                            Icon(Icons.Default.Star, "Rate", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Experience", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Allow canceling if status is pending
                    if (booking.status == "PENDING") {
                        OutlinedButton(
                            onClick = { viewModel.updateBookingStatus(booking.id, "CANCELLED") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showReviewModal) {
        ReviewSubmissionModal(
            booking = booking,
            onSubmit = { rating, comment ->
                showReviewModal = false
                viewModel.submitBookingReview(booking.id, rating, comment)
            },
            onClose = { showReviewModal = false }
        )
    }
}


// ==========================================
// 2. PROVIDER WORKSPACE SCREEN
// ==========================================
@Composable
fun ProviderDashboardScreen(viewModel: CampusConnectViewModel) {
    val currentUserAccount by viewModel.currentUserAccount.collectAsStateWithLifecycle()
    val providerBookings by viewModel.providerBookings.collectAsStateWithLifecycle()
    val allServices by viewModel.services.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val docs by viewModel.providerVerificationDocs.collectAsStateWithLifecycle()
    val myDoc = docs.find { it.providerEmail == (currentUserAccount?.email ?: "") }
    val isApproved = myDoc != null && myDoc.status == "APPROVED"

    var activeTab by remember(isApproved) { mutableStateOf(if (isApproved) 0 else 1) } // 0: Workstation Hub, 1: Venture Registry & Approval

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row to switch
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Workstation",
                            modifier = Modifier.size(16.dp),
                            tint = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Text(
                            "My Workstation",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Registry",
                            modifier = Modifier.size(16.dp),
                            tint = if (activeTab == 1) MaterialTheme.colorScheme.secondary else Color.Gray
                        )
                        Text(
                            "Venture Registry",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.secondary else Color.Gray
                        )
                        // Approval visual chip status
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when (myDoc?.status) {
                                        "APPROVED" -> Color(0xFF2E7D32)
                                        "PENDING" -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            )
        }

        if (activeTab == 0) {
            if (!isApproved) {
                // Feature Locked pending verification / submission clearance
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Clearance Required",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Workspace Clearance Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Your workspace workstation is currently locked pending developer approval and verification clearance. Please head to \"Venture Registry\" to register business details and verification credentials.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { activeTab = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Complete Venture Details", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                ProviderWorkstationTabContent(
                    viewModel = viewModel,
                    currentUserAccount = currentUserAccount,
                    providerBookings = providerBookings,
                    allServices = allServices,
                    categories = categories
                )
            }
        } else {
            VentureRegistrationForm(
                viewModel = viewModel,
                currentUserAccount = currentUserAccount,
                myDoc = myDoc,
                categories = categories
            )
        }
    }
}

@Composable
fun ProviderWorkstationTabContent(
    viewModel: CampusConnectViewModel,
    currentUserAccount: UserAccount?,
    providerBookings: List<Booking>,
    allServices: List<ServiceListing>,
    categories: List<CategoryItem>
) {
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showIdUploadDialog by remember { mutableStateOf(false) }

    val myServices = allServices.filter { it.providerEmail == (currentUserAccount?.email ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Provider Central Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Akwaba, ${currentUserAccount?.fullName ?: "Partner"}. Coordinate your bookings, manage services and view mobile money cash payouts.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Account Status: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (currentUserAccount?.isPremium == true) MaterialTheme.colorScheme.secondary else Color.Gray,
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentUserAccount?.isPremium == true) "VERIFIED PRO" else "LACKS VERIFICATION",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Earnings statistics widget with platform commission calculations as required
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Financial Statements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Calculate earnings
                val completedBookings = providerBookings.filter { it.status == "COMPLETED" }
                val totalGrossVolume = completedBookings.sumOf { it.priceGHS }
                val platformCommissionDeducted = totalGrossVolume * 0.10
                val netPartnerPayouts = totalGrossVolume - platformCommissionDeducted

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Gross Booking Volume", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("GHS ${String.format(Locale.US, "%.2f", totalGrossVolume)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("10% Comm. Deduction", fontSize = 10.sp, color = Color.Red)
                                Text("GHS ${String.format(Locale.US, "%.2f", platformCommissionDeducted)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Red)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Your Wallet Balance (Payouts Ready)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "GHS ${String.format(Locale.US, "%.2f", currentUserAccount?.cashBalance ?: 0.0)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            // Call to Action upgrades
                            if (currentUserAccount?.isPremium == false) {
                                Button(
                                    onClick = { viewModel.upgradeToPremium() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("buy_premium_button")
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Go Premium (GHS 20)", fontSize = 11.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Text("Premium Featured ACTIVE", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Service Listing Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Service Listings (${myServices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Catalog items customers can book online",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddServiceDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_service_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Service List entries
        if (myServices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No listings created yet", fontWeight = FontWeight.Bold)
                        Text("Publish tutorials, styling sessions or laundries details so students can book you.", fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(myServices) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("GHS ${service.priceGHS} - ${service.unitText}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row {
                            IconButton(onClick = { viewModel.deleteService(service.id) }) {
                                Icon(Icons.Default.Delete, "Delete listing", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Active Booking Contracts / Orders
        item {
            Column {
                Text(
                    text = "Customer Bookings Flow",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Incoming hire requests from students",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (providerBookings.isEmpty()) {
            item {
                Text(
                    "No booking requests found in database yet.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        } else {
            items(providerBookings) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Student: ${order.studentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .background(getBookingColor(order.status).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(order.status, color = getBookingColor(order.status), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Schedule: ${order.dateText} at ${order.timeText}", fontSize = 11.sp, color = Color.Gray)
                        Text("Service: ${order.serviceTitle}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Paid: GHS ${order.priceGHS}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Message student
                            IconButton(onClick = { viewModel.startChatWith(order.studentEmail) }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, "Chat student", tint = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            if (order.status == "PENDING") {
                                Button(
                                    onClick = { viewModel.updateBookingStatus(order.id, "ACCEPTED") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("accept_booking_btn_${order.id}")
                                ) {
                                    Text("Accept", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.updateBookingStatus(order.id, "CANCELLED") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red)
                                ) {
                                    Text("Decline", fontSize = 11.sp)
                                }
                            } else if (order.status == "ACCEPTED") {
                                Button(
                                    onClick = { viewModel.updateBookingStatus(order.id, "IN_PROGRESS") },
                                    modifier = Modifier.testTag("start_booking_btn_${order.id}")
                                ) {
                                    Text("Start Job", fontSize = 11.sp)
                                }
                            } else if (order.status == "IN_PROGRESS") {
                                Button(
                                    onClick = { viewModel.updateBookingStatus(order.id, "COMPLETED") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("complete_booking_btn_${order.id}")
                                ) {
                                    Text("Mark Complete", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload verification ID documents block
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, "ID Docs", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Provider Verification Center", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Upload your USTED Student ID or Ghana National Card to earn the Verified checkmark badge.", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showIdUploadDialog = true },
                        modifier = Modifier.testTag("upload_id_btn")
                    ) {
                        Text("Upload Verification Files", fontSize = 11.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    if (showAddServiceDialog) {
        AddServiceModal(
            categories = categories,
            onSubmit = { title, cat, price, unit, desc, loc ->
                showAddServiceDialog = false
                viewModel.createNewService(title, cat, price, unit, desc, loc)
            },
            onClose = { showAddServiceDialog = false }
        )
    }

    if (showIdUploadDialog) {
        IdUploadModal(
            onSubmit = { type, num ->
                showIdUploadDialog = false
                viewModel.submitProviderVerification(type, num)
            },
            onClose = { showIdUploadDialog = false }
        )
    }
}


// ==========================================
// 3. ADMIN DASHBOARD SCREEN
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: CampusConnectViewModel) {
    val documents by viewModel.providerVerificationDocs.collectAsStateWithLifecycle()
    val allBookings by viewModel.allWebBookings.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var showAddCatDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming overview
        item {
            Column {
                Text(
                    text = "Administrator Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Platform oversight, reviews moderation and provider credentials validation",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // General analytic stats with detailed totals
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Platform-Wide Gross Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    val totalGross = allBookings.filter { it.status == "COMPLETED" }.sumOf { it.priceGHS }
                    val totalComms = totalGross * 0.10
                    Text(
                        text = "GHS ${String.format(Locale.US, "%.2f", totalGross)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Accumulated 10% Platform Commissions: GHS ${String.format(Locale.US, "%.2f", totalComms)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("All Bookings", fontSize = 10.sp, color = Color.Gray)
                                Text("${allBookings.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pending Verif.", fontSize = 10.sp, color = Color.Gray)
                                val pendingVerif = documents.filter { it.status == "PENDING" }.size
                                Text("$pendingVerif", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Categories", fontSize = 10.sp, color = Color.Gray)
                                Text("${categories.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // Provider documents validation queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ID Validation Queue (${documents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Moderate registration credentials",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        if (documents.isEmpty()) {
            item {
                Text(
                    "No uploaded provider documents currently.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        } else {
            items(documents) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Provider: ${doc.providerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .background(getBookingColor(doc.status).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(doc.status, color = getBookingColor(doc.status), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Email: ${doc.providerEmail}", fontSize = 11.sp, color = Color.Gray)
                        Text("Credentials Type: ${doc.docType}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("ID Reference Code: ${doc.docNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                        if (doc.status == "PENDING") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.moderateProviderDocument(doc.providerEmail, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f).testTag("approve_id_${doc.providerEmail}")
                                ) {
                                    Text("Approve / Grant Access", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.moderateProviderDocument(doc.providerEmail, false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject ID", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Manage Platform Categories (${categories.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Define the core directory tree",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = { showAddCatDialog = true },
                    modifier = Modifier.testTag("admin_add_cat_btn")
                ) {
                    Text("Create Cat", fontSize = 11.sp)
                }
            }
        }

        items(categories) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getIconByName(cat.iconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(cat.description, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    if (showAddCatDialog) {
        AddCategoryModal(
            onSubmit = { id, name, desc ->
                showAddCatDialog = false
                viewModel.createAdminCategory(id, name, desc, "School")
            },
            onClose = { showAddCatDialog = false }
        )
    }
}


// ==========================================
// 4. SUB-UTILITY OVERLAY SCREENS
// ==========================================

// Realistic Momo Secure Checkout Modal (MTN MoMo, Telecel, AirtelTigo)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomoCheckoutModal(
    checkoutState: CheckoutState,
    onConfirmOtp: (String) -> Unit,
    onCancel: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Yellow, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECURE MOMO CHECKOUT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Divider()

                when (checkoutState.step) {
                    CheckoutStep.OTP_MODAL -> {
                        Text(
                            text = "A push request GHS ${String.format(Locale.US, "%.2f", checkoutState.amount)} was dispatched to your ${checkoutState.momoType} wallet: ${checkoutState.momoNumber}.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Enter the 4-Digit virtual security PIN to validate the transfer:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 4) otpInput = it },
                            placeholder = { Text("xxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("momo_otp_field")
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                                Text("Abort")
                            }
                            Button(
                                onClick = { if (otpInput.length == 4) onConfirmOtp(otpInput) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("submit_momo_otp_btn"),
                                enabled = otpInput.length == 4
                            ) {
                                Text("Authorise")
                            }
                        }
                    }

                    CheckoutStep.COMPLETING -> {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Text(
                            text = "Authorizing secured cellular token with Ghana Interbank Clearing Systems (GhIPSS)...",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }

                    CheckoutStep.SUCCESS -> {
                        Icon(Icons.Default.CheckCircle, "Success", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                        Text("Momo Payment Success!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Your Booking has been created successfully. The service provider Kwami cuts/tutors has been notified instantly.", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Gray)
                        Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("close_success_btn")) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

// Booking submission bottom sheet style dialog
@Composable
fun BookingFormModal(
    service: ServiceListing,
    onSubmit: (String, String, String, String, String) -> Unit,
    onClose: () -> Unit
) {
    var dateInput by remember { mutableStateOf("June 20, 2026") }
    var timeInput by remember { mutableStateOf("11:00 AM") }
    var notesInput by remember { mutableStateOf("") }
    var momoCarrier by remember { mutableStateOf("MTN MoMo") }
    var momoNumber by remember { mutableStateOf("+233 596500361") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Book: ${service.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Hourly charge: GHS ${service.priceGHS}. All transfers are escrowed safely.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Divider()

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Reservation Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Reservation Time") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Instructions / Hostel address") },
                    placeholder = { Text("Ex: Main computer lab desk, South Campus Block room 4") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Text("Enter MoMo account to deduct:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                // Momo carrier switcher row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MTN MoMo", "Telecel Cash", "AirtelTigo").forEach { carrier ->
                        val selected = momoCarrier == carrier
                        Card(
                            onClick = { momoCarrier = carrier },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                carrier,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = momoNumber,
                    onValueChange = { momoNumber = it },
                    label = { Text("Mobile Money Wallet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_momo_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1.0f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(dateInput, timeInput, notesInput, momoCarrier, momoNumber) },
                        modifier = Modifier
                            .weight(1.0f)
                            .testTag("submit_booking_form_btn")
                    ) {
                        Text("Proceed")
                    }
                }
            }
        }
    }
}

// Real-time Chat drawer overlay simulator
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDrawerOverlay(
    viewModel: CampusConnectViewModel,
    onClose: () -> Unit
) {
    val messages by viewModel.currentChatHistory.collectAsStateWithLifecycle()
    val partnerEmail by viewModel.activeChatPartnerEmail.collectAsStateWithLifecycle()
    var textMsgVal by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Live Conversation",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                partnerEmail ?: "provider@",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close chat", tint = Color.White)
                        }
                    }
                }

                // Messages list area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderEmail == viewModel.currentUserEmail.value
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Input line bar with direct trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = textMsgVal,
                        onValueChange = { textMsgVal = it },
                        placeholder = { Text("Write message here...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(textMsgVal)
                            textMsgVal = ""
                        },
                        modifier = Modifier.testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Notifications trigger Overlay list
@Composable
fun NotificationsModal(
    viewModel: CampusConnectViewModel,
    onClose: () -> Unit
) {
    val items by viewModel.simulatedNotifications.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp)
                    .height(350.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Alert Center Logs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { alarm ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(alarm.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(alarm.text, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// Profile Editor Modal
@Composable
fun ProfileEditorModal(
    viewModel: CampusConnectViewModel,
    onClose: () -> Unit
) {
    val user by viewModel.currentUserAccount.collectAsStateWithLifecycle()
    var nameEx by remember { mutableStateOf(user?.fullName ?: "Amina Osei-Kofi") }
    var phoneEx by remember { mutableStateOf(user?.phone ?: "+233 241112222") }
    var campusEx by remember { mutableStateOf(user?.campus ?: "USTED Main Campus") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit USTED Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = nameEx,
                    onValueChange = { nameEx = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneEx,
                    onValueChange = { phoneEx = it },
                    label = { Text("WhatsApp / Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = campusEx,
                    onValueChange = { campusEx = it },
                    label = { Text("University Campus") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onClose) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateUserProfile(nameEx, phoneEx, campusEx, user?.subPlan ?: "Free")
                            onClose()
                        }
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

// Wallet topup modal
@Composable
fun WalletTopUpModal(
    viewModel: CampusConnectViewModel,
    onClose: () -> Unit
) {
    var amountInput by remember { mutableStateOf("100.00") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Text("Momo Wallet Top-Up", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Add mock proceeds to try out and book services", fontSize = 11.sp, color = Color.Gray)

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    placeholder = { Text("Ex: 100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 10.0
                            viewModel.addFunds(amt)
                            onClose()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Funds")
                    }
                }
            }
        }
    }
}

// Add custom listing drawer
@Composable
fun AddServiceModal(
    categories: List<CategoryItem>,
    onSubmit: (String, String, Double, String, String, String) -> Unit,
    onClose: () -> Unit
) {
    var heading by remember { mutableStateOf("Typesetting thesis papers") }
    var selectedCatId by remember { mutableStateOf("academic") }
    var priceStr by remember { mutableStateOf("1.50") }
    var unitStr by remember { mutableStateOf("per printed page") }
    var textBody by remember { mutableStateOf("Will typestate and binder major project assignments inside USTED Kumasi campus.") }
    var campusLocation by remember { mutableStateOf("USTED Main Block Study Stall") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Create Service Listing", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = heading,
                    onValueChange = { heading = it },
                    label = { Text("Service Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pick Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val active = selectedCatId == cat.id
                        Card(
                            onClick = { selectedCatId = cat.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                cat.name,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (GHS)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitStr,
                        onValueChange = { unitStr = it },
                        label = { Text("Pricing unit") },
                        placeholder = { Text("per hour / wash bag") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = textBody,
                    onValueChange = { textBody = it },
                    label = { Text("Full description of service") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = campusLocation,
                    onValueChange = { campusLocation = it },
                    label = { Text("Stall / Dorm Location Info") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onClose) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pr = priceStr.toDoubleOrNull() ?: 10.0
                            onSubmit(heading, selectedCatId, pr, unitStr, textBody, campusLocation)
                        },
                        modifier = Modifier.testTag("submit_new_service_btn")
                    ) {
                        Text("Publish")
                    }
                }
            }
        }
    }
}

// Upload ID verification file Modal
@Composable
fun IdUploadModal(
    onSubmit: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var idTypeSelected by remember { mutableStateOf("Student ID") }
    var refNo by remember { mutableStateOf("USTED-2024-ENT-002") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Submit Verification ID", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Let administrators evaluate credentials to approve verified provider listings.", fontSize = 11.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Student ID", "National ID").forEach { option ->
                        val selected = idTypeSelected == option
                        Card(
                            onClick = { idTypeSelected = option },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                option,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("Document Reference Identifier") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onClose) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(idTypeSelected, refNo) },
                        modifier = Modifier.testTag("submit_id_modal_btn")
                    ) {
                        Text("Submit Files")
                    }
                }
            }
        }
    }
}

// Admin add category
@Composable
fun AddCategoryModal(
    onSubmit: (String, String, String) -> Unit,
    onClose: () -> Unit
) {
    var catId by remember { mutableStateOf("security") }
    var catName by remember { mutableStateOf("Campus Security Run") }
    var catDesc by remember { mutableStateOf("Safeguarding student walks late night") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add New Marketplace Category", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                OutlinedTextField(
                    value = catId,
                    onValueChange = { catId = it },
                    label = { Text("Id slug (lowercase)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = catName,
                    onValueChange = { catName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = catDesc,
                    onValueChange = { catDesc = it },
                    label = { Text("Category Details") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onClose) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(catId, catName, catDesc) },
                        modifier = Modifier.testTag("submit_add_cat_modal")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}


// Review submission Modal
@Composable
fun ReviewSubmissionModal(
    booking: Booking,
    onSubmit: (Int, String) -> Unit,
    onClose: () -> Unit
) {
    var scoreValue by remember { mutableIntStateOf(5) }
    var commentField by remember { mutableStateOf("Perfect! Kwame Cuts is unmatched inside USTED. Finished with line-up and complete sanitizer.") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate service experience!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = booking.serviceTitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Render star selectors
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { scoreValue = i }) {
                            Icon(
                                imageVector = if (i <= scoreValue) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $i",
                                tint = if (i <= scoreValue) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = commentField,
                    onValueChange = { commentField = it },
                    label = { Text("Written feedback review") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(scoreValue, commentField) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_rate_btn")
                    ) {
                        Text("Post Review")
                    }
                }
            }
        }
    }
}

// Developer dynamic contact details footer as required
@Composable
fun DeveloperContactFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Contact support",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WhatsApp Support 📞 : +233 596500361",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "dev@campusconnect.edu.gh",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// --- UTILITY ENUM CONVERTER FUNCTIONS ---
private fun getIconByName(iconName: String): ImageVector {
    return when (iconName) {
        "School" -> Icons.Default.School
        "ContentCut" -> Icons.Default.ContentCut
        "LocalLaundryService" -> Icons.Default.LocalLaundryService
        "Restaurant" -> Icons.Default.Restaurant
        "PhotoCamera" -> Icons.Default.PhotoCamera
        "Computer" -> Icons.Default.Computer
        "Speaker" -> Icons.Default.Speaker
        else -> Icons.Default.Category
    }
}

private fun getBookingColor(status: String): Color {
    return when (status) {
        "PENDING" -> Color(0xFFFF9800) // Amber
        "ACCEPTED" -> Color(0xFF1E88E5) // Light Blue
        "IN_PROGRESS" -> Color(0xFF8E24AA) // Purple
        "COMPLETED" -> Color(0xFF2E7D32) // Emerald Green
        "CANCELLED", "REJECTED" -> Color(0xFFD32F2F) // Ruby Red
        "APPROVED" -> Color(0xFF4CAF50)
        else -> Color.DarkGray
    }
}

@Composable
fun LoginSignupPortal(viewModel: CampusConnectViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedSignupRole by remember { mutableStateOf("STUDENT") } // "STUDENT", "PROVIDER"
    var isSignupMode by remember { mutableStateOf(false) }
    var isForgotMode by remember { mutableStateOf(false) }

    // State flows
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val signupError by viewModel.signupError.collectAsStateWithLifecycle()
    val resetError by viewModel.resetError.collectAsStateWithLifecycle()

    val signupState by viewModel.signupState.collectAsStateWithLifecycle()
    val forgotStep by viewModel.forgotPasswordState.collectAsStateWithLifecycle()

    val signupOtp by viewModel.signupOtp.collectAsStateWithLifecycle()
    val resetOtp by viewModel.resetOtp.collectAsStateWithLifecycle()
    val resetEmailOrPhone by viewModel.resetEmailOrPhone.collectAsStateWithLifecycle()

    var showGoogleDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand Logo icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "CAMPUS CONNECT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "University Services & Venture Network",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isForgotMode) {
                    // --- FORGOT PASSWORD FLOW ---
                    Text("Recover Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    when (forgotStep) {
                        CampusConnectViewModel.ForgotPasswordStep.REQUEST_FORM -> {
                            var forgotInput by remember { mutableStateOf("") }
                            Text(
                                "Enter your email or phone number to receive a verification security code.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = forgotInput,
                                onValueChange = { forgotInput = it },
                                label = { Text("Email or Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("forgot_input")
                            )

                            if (resetError != null) {
                                Text(resetError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = { viewModel.initiateForgotPassword(forgotInput) },
                                modifier = Modifier.fillMaxWidth().testTag("forgot_send_code_button")
                            ) {
                                Text("Send Verification Code")
                            }

                            TextButton(onClick = {
                                isForgotMode = false
                                viewModel.resetForgotPasswordState()
                            }) {
                                Text("Back to Login", fontWeight = FontWeight.Bold)
                            }
                        }
                        CampusConnectViewModel.ForgotPasswordStep.VERIFY_RESET_OTP -> {
                            var enteredResetOtp by remember { mutableStateOf("") }
                            
                            // Demo friendly indicator
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔑 Simulated Recovery Code Sent!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Reference Code: $resetOtp", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    Text("(Tap to autofill code)", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            OutlinedTextField(
                                value = enteredResetOtp,
                                onValueChange = { enteredResetOtp = it },
                                label = { Text("6-Digit Verification Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("forgot_otp_input")
                            )

                            if (resetError != null) {
                                Text(resetError!!, color = Color.Red, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.verifyForgotPasswordCode(enteredResetOtp) },
                                modifier = Modifier.fillMaxWidth().testTag("forgot_verify_code_button")
                            ) {
                                Text("Verify Account")
                            }

                            TextButton(onClick = { viewModel.resetForgotPasswordState() }) {
                                Text("Resend Code")
                            }
                        }
                        CampusConnectViewModel.ForgotPasswordStep.NEW_PASSWORD_FORM -> {
                            var newPass by remember { mutableStateOf("") }
                            Text("Set a secure access password for your account.", fontSize = 11.sp, textAlign = TextAlign.Center)

                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("New Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("forgot_new_pass_input")
                            )

                            if (resetError != null) {
                                Text(resetError!!, color = Color.Red, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.resetPasswordWithNew(newPass) },
                                modifier = Modifier.fillMaxWidth().testTag("forgot_save_pass_button")
                            ) {
                                Text("Update Password & Login")
                            }
                        }
                        CampusConnectViewModel.ForgotPasswordStep.SUCCESS -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                            Text("Password Restored Successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text("Your credentials have been certified with the secure authentication storage.", fontSize = 11.sp, textAlign = TextAlign.Center)

                            Button(
                                onClick = {
                                    isForgotMode = false
                                    viewModel.resetForgotPasswordState()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Proceed to Login")
                            }
                        }
                    }
                } else if (!isSignupMode) {
                    // --- LOGIN MODE ---
                    Text("Sign In to Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_password")
                    )

                    if (loginError != null) {
                        Text(loginError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            isForgotMode = true
                            viewModel.clearErrors()
                        }) {
                            Text("Forgot password?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.loginUser(email, password) },
                        modifier = Modifier.fillMaxWidth().testTag("signin_submit_button")
                    ) {
                        Text("Sign In")
                    }

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f))
                        Text("or", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                        Divider(modifier = Modifier.weight(1f))
                    }

                    // Google login button
                    OutlinedButton(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("google_auth_btn"),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Google Logo", tint = Color(0xFF1D4ED8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account?", fontSize = 11.sp)
                        TextButton(onClick = {
                            isSignupMode = true
                            viewModel.clearErrors()
                        }) {
                            Text("Register Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // QUICK DEMO SHORTCUT PANEL
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("⚡ Quick Demo Sessions", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Card(
                            onClick = {
                                email = "student@usted.edu.gh"
                                password = "password"
                                viewModel.loginUser("student@usted.edu.gh", "password")
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Amina\n(Student)", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
                        }
                        Card(
                            onClick = {
                                email = "kwame.asante@usted.edu.gh"
                                password = "password"
                                viewModel.loginUser("kwame.asante@usted.edu.gh", "password")
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kwame\n(Provider)", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
                        }
                        Card(
                            onClick = {
                                email = "admin@campusconnect.edu.gh"
                                password = "password"
                                viewModel.loginUser("admin@campusconnect.edu.gh", "password")
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dev\n(Admin)", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    // --- SIGNUP MODE ---
                    when (signupState) {
                        CampusConnectViewModel.SignupStep.FORM -> {
                            Text("Register Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_name")
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Campus Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_email")
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Ghana MoMo Phone") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_phone")
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Access Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_password")
                            )

                            // Role Selection Toggle Capsules
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Select Account Role Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("STUDENT" to "Student (Customer)", "PROVIDER" to "Venture / Provider").forEach { (type, labelText) ->
                                        val isSel = selectedSignupRole == type
                                        Card(
                                            onClick = { selectedSignupRole = type },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            ),
                                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                labelText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                                                textAlign = TextAlign.Center,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (signupError != null) {
                                Text(signupError!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            }

                            Button(
                                onClick = { viewModel.initiateSignup(email, name, phone, selectedSignupRole, password) },
                                modifier = Modifier.fillMaxWidth().testTag("signup_get_started_btn")
                            ) {
                                Text("Generate Code & Sign Up")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Already registered?", fontSize = 11.sp)
                                TextButton(onClick = {
                                    isSignupMode = false
                                    viewModel.clearErrors()
                                }) {
                                    Text("Sign In Pool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        CampusConnectViewModel.SignupStep.VERIFICATION_OTP -> {
                            var pinInput by remember { mutableStateOf("") }
                            
                            // Visual OTP badge helper
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✉️ Verification OTP Sent Successfully!", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Please enter verification pin $signupOtp below", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("(Tap to autofill verification code)", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                                }
                            }

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("6-Digit OTP Verification Pin") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_otp_input")
                            )

                            if (signupError != null) {
                                Text(signupError!!, color = Color.Red, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }

                            Button(
                                onClick = { viewModel.completeSignup(pinInput) },
                                modifier = Modifier.fillMaxWidth().testTag("signup_submit_otp_btn")
                            ) {
                                Text("Verify & Login Account")
                            }

                            OutlinedButton(
                                onClick = { viewModel.cancelSignupOtp() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Sign Up")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoogleDialog) {
        Dialog(onDismissRequest = { showGoogleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sign in with Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Select a simulated Google account to securely link with Campus Connect Services.", fontSize = 11.sp, textAlign = TextAlign.Center)

                    listOf(
                        "Issahaku Baba" to "issahaku@gmail.com",
                        "Emmanuella Boateng" to "emmanuella.b@gmail.com"
                    ).forEach { (gName, gMail) ->
                        Card(
                            onClick = {
                                showGoogleDialog = false
                                viewModel.loginWithGoogle(gMail, gName)
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(gName.take(1), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(gName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(gMail, fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    TextButton(onClick = { showGoogleDialog = false }) {
                        Text("Cancel", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun VentureRegistrationForm(
    viewModel: CampusConnectViewModel,
    currentUserAccount: UserAccount?,
    myDoc: ProviderDocument?,
    categories: List<CategoryItem>
) {
    var ventureName by remember { mutableStateOf(myDoc?.providerName ?: "") }
    var description by remember { mutableStateOf("") }
    var basePriceText by remember { mutableStateOf("") }
    var unitText by remember { mutableStateOf("per page") }
    var selectedCategory by remember { mutableStateOf("academic") }
    var location by remember { mutableStateOf("") }
    var idTypeSelected by remember { mutableStateOf(myDoc?.docType ?: "Student ID") }
    var docNumber by remember { mutableStateOf(myDoc?.docNumber ?: "") }

    // If a service listing already exists, we can pre-populate description, prices, location, etc.
    val allServices by viewModel.services.collectAsStateWithLifecycle()
    val existingListing = allServices.find { it.providerEmail == (currentUserAccount?.email ?: "") }

    LaunchedEffect(existingListing) {
        existingListing?.let {
            if (ventureName.isEmpty()) ventureName = it.providerName
            if (description.isEmpty()) description = it.description
            if (basePriceText.isEmpty()) basePriceText = it.priceGHS.toString()
            if (unitText.isEmpty()) unitText = it.unitText
            if (selectedCategory == "academic") selectedCategory = it.category
            if (location.isEmpty()) location = it.location
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Venture Workspace Certification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Register your business venture details and verification ID card to build your specialized storefront.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Status banner
        if (myDoc != null) {
            item {
                val status = myDoc.status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status == "PENDING") Color(0xFFFFF3E0) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (status == "PENDING") Icons.Default.VerifiedUser else Icons.Default.Cancel,
                            contentDescription = status,
                            tint = if (status == "PENDING") Color(0xFFE65100) else Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (status == "PENDING") "🛡️ Account Status: PENDING CLEARANCE" else "❌ Account Status: DOCUMENT DISCREPANCY REJECTED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (status == "PENDING") Color(0xFFE65100) else Color(0xFFC62828)
                            )
                            Text(
                                text = if (status == "PENDING") {
                                    "Akwaba, your venture \"${myDoc.providerName}\" details have been uploaded successfully. Developers are evaluating your credentials. You can edit detail fields and submit updates below seamlessly."
                                } else {
                                    "Our systems rejected the verification link. Please check your Student ID / National ID code and venture categorizations, update details, and submit again."
                                },
                                fontSize = 11.sp,
                                color = if (status == "PENDING") Color(0xFFE65100).copy(alpha = 0.85f) else Color(0xFFC62828).copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Registration detail form fields
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Venture & Business Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    OutlinedTextField(
                        value = ventureName,
                        onValueChange = { ventureName = it },
                        label = { Text("Venture Name (e.g. SassyBraids / Kwame Barber)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("venture_name_input")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Venture Base Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Render category options
                            listOf("academic" to "Academic", "personal" to "Personal", "essentials" to "Essentials", "creative" to "Creative").forEach { (id, name) ->
                                val selected = selectedCategory == id
                                Card(
                                    onClick = { selectedCategory = id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Base Specialties & Venture Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = basePriceText,
                            onValueChange = { basePriceText = it },
                            label = { Text("Base Price (GHS)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitText,
                            onValueChange = { unitText = it },
                            label = { Text("Price Unit Text (e.g., per page)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Campus Location (e.g., Bani Hostel / DST)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(modifier = Modifier.padding(vertical = 6.dp))

                    Text(
                        text = "Venture Credentials ID Proof",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Student ID", "National ID").forEach { name ->
                            val selected = idTypeSelected == name
                            Card(
                                onClick = { idTypeSelected = name },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.secondary else Color.LightGray),
                                modifier = Modifier.weight(1f)
                             ) {
                                Text(
                                    name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = { Text("ID Reference Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    var validationMessage by remember { mutableStateOf("") }

                    if (validationMessage.isNotEmpty()) {
                        Text(validationMessage, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pr = basePriceText.toDoubleOrNull()
                            if (ventureName.isBlank() || description.isBlank() || pr == null || location.isBlank() || docNumber.isBlank()) {
                                validationMessage = "Please complete all fields with valid details before registration!"
                            } else {
                                validationMessage = ""
                                viewModel.registerProviderVenture(
                                    ventureName = ventureName,
                                    category = selectedCategory,
                                    description = description,
                                    price = pr,
                                    unitText = unitText,
                                    location = location,
                                    idType = idTypeSelected,
                                    idNumber = docNumber
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_venture_btn")
                    ) {
                        Text(if (myDoc == null) "Submit Venture Details" else "Update Venture Details & Submit")
                    }
                }
            }
        }
    }
}
