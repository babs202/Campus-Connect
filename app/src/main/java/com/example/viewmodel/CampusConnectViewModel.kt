package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CampusConnectViewModel(private val repository: CampusConnectRepository) : ViewModel() {

    // --- SESSION STATE & AUTHENTICATION ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentRole = MutableStateFlow("STUDENT") // STUDENT, PROVIDER, ADMIN
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Errors & Steps
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signupError = MutableStateFlow<String?>(null)
    val signupError: StateFlow<String?> = _signupError.asStateFlow()

    private val _resetError = MutableStateFlow<String?>(null)
    val resetError: StateFlow<String?> = _resetError.asStateFlow()

    private val _signupState = MutableStateFlow(SignupStep.FORM)
    val signupState: StateFlow<SignupStep> = _signupState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordStep.REQUEST_FORM)
    val forgotPasswordState: StateFlow<ForgotPasswordStep> = _forgotPasswordState.asStateFlow()

    private val _signupOtp = MutableStateFlow("")
    val signupOtp: StateFlow<String> = _signupOtp.asStateFlow()

    private val _resetOtp = MutableStateFlow("")
    val resetOtp: StateFlow<String> = _resetOtp.asStateFlow()

    private val _resetEmailOrPhone = MutableStateFlow("")
    val resetEmailOrPhone: StateFlow<String> = _resetEmailOrPhone.asStateFlow()

    private var tempSignupUser: UserAccount? = null

    enum class SignupStep {
        FORM, VERIFICATION_OTP
    }

    enum class ForgotPasswordStep {
        REQUEST_FORM, VERIFY_RESET_OTP, NEW_PASSWORD_FORM, SUCCESS
    }

    // Active User details
    val currentUserAccount: StateFlow<UserAccount?> = _currentUserEmail
        .flatMapLatest { email -> repository.getUserFlow(email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- SEARCH / DISCOVERY STATE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _filterVerifiedOnly = MutableStateFlow(false)
    val filterVerifiedOnly: StateFlow<Boolean> = _filterVerifiedOnly.asStateFlow()

    private val _filterHighestRated = MutableStateFlow(false)
    val filterHighestRated: StateFlow<Boolean> = _filterHighestRated.asStateFlow()

    // --- THEME STATE ---
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- AUTOMATED AUTH CONTROLLERS ---
    fun clearErrors() {
        _loginError.value = null
        _signupError.value = null
        _resetError.value = null
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserSync(email)
            if (user == null) {
                _loginError.value = "Account matching this email was not found. Please sign up!"
            } else if (user.password != password) {
                _loginError.value = "Incorrect password. Please try again!"
            } else {
                _currentUserEmail.value = email
                _currentRole.value = user.roleType
                _isLoggedIn.value = true
                _loginError.value = null
                addNotification("Signed In", "Welcome back, ${user.fullName}!")
            }
        }
    }

    fun logoutUser() {
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
        _currentRole.value = "STUDENT"
        _signupState.value = SignupStep.FORM
        _forgotPasswordState.value = ForgotPasswordStep.REQUEST_FORM
        clearErrors()
        addNotification("Signed Out", "You have logged out of your session securely.")
    }

    fun initiateSignup(email: String, name: String, phone: String, role: String, pass: String) {
        viewModelScope.launch {
            _signupError.value = null
            val existing = repository.getUserSync(email)
            if (existing != null) {
                _signupError.value = "This email is already registered. Please login!"
                return@launch
            }
            if (email.isBlank() || name.isBlank() || phone.isBlank() || pass.isBlank()) {
                _signupError.value = "Please fill in all details to sign up!"
                return@launch
            }

            val pin = (100000..999999).random().toString()
            _signupOtp.value = pin
            tempSignupUser = UserAccount(
                email = email,
                fullName = name,
                phone = phone,
                roleType = role,
                password = pass,
                cashBalance = if (role == "PROVIDER") 0.0 else 250.0,
                isPremium = false,
                subPlan = "Free"
            )
            _signupState.value = SignupStep.VERIFICATION_OTP
            addNotification("Email Verification Code", "Sign-up code for **$email** is **$pin**.")
        }
    }

    fun completeSignup(code: String) {
        viewModelScope.launch {
            if (code == _signupOtp.value) {
                val user = tempSignupUser
                if (user != null) {
                    repository.insertUser(user)
                    _currentUserEmail.value = user.email
                    _currentRole.value = user.roleType
                    _isLoggedIn.value = true
                    _signupState.value = SignupStep.FORM
                    _signupError.value = null
                    tempSignupUser = null
                    addNotification("Signup Completed", "Welcome to Campus Connect, ${user.fullName}!")
                }
            } else {
                _signupError.value = "Invalid 6-digit verification code. Please try again."
            }
        }
    }

    fun cancelSignupOtp() {
        _signupState.value = SignupStep.FORM
        tempSignupUser = null
        clearErrors()
    }

    fun initiateForgotPassword(emailOrPhone: String) {
        viewModelScope.launch {
            _resetError.value = null
            if (emailOrPhone.isBlank()) {
                _resetError.value = "Please enter an email or phone number first."
                return@launch
            }
            // Search all users
            val allUsers = repository.getAllUsersFlow().first()
            val matched = allUsers.find { it.email.equals(emailOrPhone, ignoreCase = true) || it.phone == emailOrPhone }
            if (matched == null) {
                _resetError.value = "No registered account found with that email or phone."
                return@launch
            }

            val p = (100000..999999).random().toString()
            _resetOtp.value = p
            _resetEmailOrPhone.value = matched.email
            _forgotPasswordState.value = ForgotPasswordStep.VERIFY_RESET_OTP
            addNotification("Recovery Code", "Your verification recovery code is **$p**.")
        }
    }

    fun verifyForgotPasswordCode(code: String) {
        if (code == _resetOtp.value) {
            _resetError.value = null
            _forgotPasswordState.value = ForgotPasswordStep.NEW_PASSWORD_FORM
        } else {
            _resetError.value = "Invalid verification OTP code. Please enter the correct code."
        }
    }

    fun resetPasswordWithNew(newPass: String) {
        viewModelScope.launch {
            val mail = _resetEmailOrPhone.value
            val user = repository.getUserSync(mail)
            if (user != null && newPass.isNotBlank()) {
                repository.updateUser(user.copy(password = newPass))
                _forgotPasswordState.value = ForgotPasswordStep.SUCCESS
                addNotification("Password Restored", "Password for $mail updated successfully!")
            } else {
                _resetError.value = "Please enter a valid password."
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordStep.REQUEST_FORM
        _resetEmailOrPhone.value = ""
        clearErrors()
    }

    fun loginWithGoogle(gmail: String, name: String) {
        viewModelScope.launch {
            val existing = repository.getUserSync(gmail)
            if (existing != null) {
                _currentUserEmail.value = gmail
                _currentRole.value = existing.roleType
                _isLoggedIn.value = true
                _loginError.value = null
                addNotification("Signed In via Google", "Secure Google authorization as $name.")
            } else {
                val newUser = UserAccount(
                    email = gmail,
                    fullName = name,
                    phone = "+233 Google Auth",
                    roleType = "STUDENT",
                    password = "google_authenticated_secure",
                    cashBalance = 250.0
                )
                repository.insertUser(newUser)
                _currentUserEmail.value = gmail
                _currentRole.value = "STUDENT"
                _isLoggedIn.value = true
                addNotification("Google Registered", "A standard Student account successfully created under Google profile.")
            }
        }
    }

    // --- DATA STREAMS ---
    val categories: StateFlow<List<CategoryItem>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceListing>> = combine(
        repository.getAllServices(),
        repository.getAllDocuments(),
        _searchQuery,
        _selectedCategory,
        _filterVerifiedOnly,
        _filterHighestRated
    ) { Array ->
        val allServices = Array[0] as List<ServiceListing>
        val docs = Array[1] as List<ProviderDocument>
        val query = Array[2] as String
        val cat = Array[3] as String
        val verified = Array[4] as Boolean
        val highest = Array[5] as Boolean

        val approvedProviders = docs.filter { it.status == "APPROVED" }.map { it.providerEmail }.toSet()
        allServices.filter { srv ->
            // Filter marketplace: only show approved ventures to students!
            // BUT let the provider who registered the venture view their own pending listing.
            val isApproved = approvedProviders.contains(srv.providerEmail) || srv.providerEmail == _currentUserEmail.value
            if (!isApproved) return@filter false

            val matchQuery = srv.title.contains(query, ignoreCase = true) ||
                    srv.providerName.contains(query, ignoreCase = true) ||
                    srv.description.contains(query, ignoreCase = true)
            val matchCategory = cat == "all" || srv.category == cat
            val matchVerified = !verified || srv.isVerified
            val matchRating = !highest || srv.rating >= 4.7
            matchQuery && matchCategory && matchVerified && matchRating
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookings for Student
    val studentBookings: StateFlow<List<Booking>> = _currentUserEmail
        .flatMapLatest { email -> repository.getBookingsForStudent(email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookings for Provider
    val providerBookings: StateFlow<List<Booking>> = _currentUserEmail
        .flatMapLatest { email -> repository.getBookingsForProvider(email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All bookings for Admin
    val allWebBookings: StateFlow<List<Booking>> = repository.getAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Verification documents
    val providerVerificationDocs: StateFlow<List<ProviderDocument>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CHAT ACTIVE THREAD ---
    private val _activeChatPartnerEmail = MutableStateFlow<String?>(null)
    val activeChatPartnerEmail: StateFlow<String?> = _activeChatPartnerEmail.asStateFlow()

    val currentChatHistory: StateFlow<List<ChatMessage>> = _activeChatPartnerEmail
        .flatMapLatest { partnerEmail ->
            if (partnerEmail != null) {
                repository.getChatHistory(_currentUserEmail.value, partnerEmail)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Contacts flow (people who have messaged or been messaged)
    val chatContacts: StateFlow<List<UserAccount>> = repository.getAllMessagesFlow()
        .map { messages ->
            val myEmail = _currentUserEmail.value
            val partnerEmails = messages.mapNotNull { msg ->
                when {
                    msg.senderEmail == myEmail -> msg.recipientEmail
                    msg.recipientEmail == myEmail -> msg.senderEmail
                    else -> null
                }
            }.distinct()

            partnerEmails.mapNotNull { email ->
                repository.getUserSync(email)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SYSTEM NOTIFICATIONS LOG (In-App simulation) ---
    private val _simulatedNotifications = MutableStateFlow<List<SimulatedNotification>>(
        listOf(
            SimulatedNotification("Welcome to USTED Connect!", "Discover verified students offering tutoring, barbering, printing & jollof. Stay offline-accessible!", System.currentTimeMillis())
        )
    )
    val simulatedNotifications: StateFlow<List<SimulatedNotification>> = _simulatedNotifications.asStateFlow()

    fun addNotification(title: String, body: String) {
        val newList = listOf(SimulatedNotification(title, body, System.currentTimeMillis())) + _simulatedNotifications.value
        _simulatedNotifications.value = newList
    }

    // --- MOBILE MONEY CHECKOUT CONTROLLER ---
    private val _checkoutState = MutableStateFlow<CheckoutState?>(null)
    val checkoutState: StateFlow<CheckoutState?> = _checkoutState.asStateFlow()

    // --- SEEDING & SETUP ---
    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // --- CORE HANDLERS ---

    fun changeActiveRole(role: String) {
        _currentRole.value = role
        when (role) {
            "STUDENT" -> {
                _currentUserEmail.value = "student@usted.edu.gh"
            }
            "PROVIDER" -> {
                _currentUserEmail.value = "kwame.asante@usted.edu.gh"
            }
            "ADMIN" -> {
                _currentUserEmail.value = "admin@campusconnect.edu.gh"
            }
        }
    }

    // Student selects a custom partner to open Chat Screen
    fun startChatWith(partnerEmail: String) {
        _activeChatPartnerEmail.value = partnerEmail
    }

    fun closeChat() {
        _activeChatPartnerEmail.value = null
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleVerifiedFilter() {
        _filterVerifiedOnly.value = !_filterVerifiedOnly.value
    }

    fun toggleRatingFilter() {
        _filterHighestRated.value = !_filterHighestRated.value
    }

    // --- USER PROFILE OPERATIONS ---
    fun updateUserProfile(name: String, phone: String, campus: String, subPlan: String) {
        viewModelScope.launch {
            val current = currentUserAccount.value ?: return@launch
            val updated = current.copy(
                fullName = name,
                phone = phone,
                campus = campus,
                subPlan = subPlan,
                isPremium = subPlan != "Free"
            )
            repository.updateUser(updated)
            addNotification("Profile Updated", "Your Campus Connect profile has been synchronized successfully.")
        }
    }

    fun addFunds(amount: Double) {
        viewModelScope.launch {
            val current = currentUserAccount.value ?: return@launch
            val updated = current.copy(cashBalance = current.cashBalance + amount)
            repository.updateUser(updated)
            addNotification("Deposited GHS $amount", "Your virtual mobile money wallet has pocketed GHS $amount successfully.")
        }
    }

    // --- BOOKING OPERATIONS ---
    fun createBooking(
        service: ServiceListing,
        date: String,
        time: String,
        notes: String,
        momoType: String,
        momoNumber: String
    ) {
        viewModelScope.launch {
            val student = currentUserAccount.value ?: return@launch
            
            // Check funds first
            if (student.cashBalance < service.priceGHS) {
                addNotification("Booking Failed", "Insufficent cash balance to book ${service.title}. Top up your wallet.")
                return@launch
            }

            // Begin simulated secure Mobile Money payment flow
            _checkoutState.value = CheckoutState(
                service = service,
                date = date,
                time = time,
                notes = notes,
                momoType = momoType,
                momoNumber = momoNumber,
                amount = service.priceGHS,
                otpEntered = "",
                step = CheckoutStep.OTP_MODAL
            )
        }
    }

    fun confirmMomoPayment(otp: String) {
        viewModelScope.launch {
            val checkout = _checkoutState.value ?: return@launch
            _checkoutState.value = checkout.copy(step = CheckoutStep.COMPLETING)
            delay(1500) // Realistic MoMo secure authorization processing delay

            // Create active Booking in database
            val student = currentUserAccount.value!!
            val newBooking = Booking(
                studentEmail = student.email,
                studentName = student.fullName,
                studentPhone = student.phone,
                serviceId = checkout.service.id,
                serviceTitle = checkout.service.title,
                providerEmail = checkout.service.providerEmail,
                providerName = checkout.service.providerName,
                category = checkout.service.category,
                priceGHS = checkout.amount,
                dateText = checkout.date,
                timeText = checkout.time,
                notes = checkout.notes,
                status = "PENDING",
                paymentMethod = checkout.momoType,
                paymentNumber = checkout.momoNumber,
                paymentTxRef = "TX-${(100000..999999).random()}"
            )

            repository.insertBooking(newBooking)

            // Deduct from student's virtual balance
            val updatedStudent = student.copy(cashBalance = student.cashBalance - checkout.amount)
            repository.updateUser(updatedStudent)

            _checkoutState.value = checkout.copy(step = CheckoutStep.SUCCESS)
            addNotification(
                "Payment Confirmed - GHS ${checkout.amount}",
                "Booking pending approval. Authorized via ${checkout.momoType} (${checkout.momoNumber})"
            )
            
            // Send instant simulated chat notification message from System to Provider
            repository.insertMessage(
                ChatMessage(
                    senderEmail = "system@cc.edu.gh",
                    recipientEmail = checkout.service.providerEmail,
                    text = "🔔 New Booking Alert: student ${student.fullName} has paid GHS ${checkout.amount} for your service: '${checkout.service.title}' on ${checkout.date} at ${checkout.time}."
                )
            )
        }
    }

    fun closeCheckout() {
        _checkoutState.value = null
    }

    // --- PROVIDER SERVICE ACTIONS ---
    fun updateBookingStatus(bookingId: Int, newStatus: String) {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId) ?: return@launch
            val updated = booking.copy(status = newStatus)
            repository.updateBooking(updated)

            // Calculate commission details & transfer earnings if marked completed
            if (newStatus == "COMPLETED") {
                val commission = booking.priceGHS * 0.10 // 10% standard platform commission
                val netEarnings = booking.priceGHS - commission

                // Distribute proceeds to provider
                val provider = repository.getUserSync(booking.providerEmail)
                if (provider != null) {
                    val updatedProvider = provider.copy(cashBalance = provider.cashBalance + netEarnings)
                    repository.updateUser(updatedProvider)
                }

                addNotification(
                    "Job Completed!",
                    "You completed service '${booking.serviceTitle}'. GHS $netEarnings added to earnings after 10% commission deductions."
                )
            } else {
                addNotification(
                    "Booking Status: $newStatus",
                    "Service order '${booking.serviceTitle}' has been updated to status: $newStatus."
                )
            }

            // Sync alert via automated notification chat message
            repository.insertMessage(
                ChatMessage(
                    senderEmail = "system@cc.edu.gh",
                    recipientEmail = booking.studentEmail,
                    text = "📋 Your Booking '${booking.serviceTitle}' status updated to $newStatus by ${booking.providerName}!"
                )
            )
        }
    }

    // Create a new Service Listing
    fun createNewService(
        title: String,
        category: String,
        priceGHS: Double,
        unitText: String,
        description: String,
        location: String
    ) {
        viewModelScope.launch {
            val provider = currentUserAccount.value ?: return@launch
            val newSrv = ServiceListing(
                providerEmail = provider.email,
                providerName = provider.fullName,
                providerPhone = provider.phone,
                category = category,
                title = title,
                priceGHS = priceGHS,
                unitText = unitText,
                description = description,
                location = location,
                isVerified = provider.isPremium, // Premium or active status grants verification
                isPremium = provider.isPremium
            )
            repository.insertService(newSrv)
            addNotification("Service Published", "Your new USTED service listing '$title' is now live on the marketplace!")
        }
    }

    fun deleteService(serviceId: Int) {
        viewModelScope.launch {
            repository.deleteServiceById(serviceId)
            addNotification("Service Listing Removed", "Your service listing was deleted from the catalog.")
        }
    }

    // --- INSTANT MESSAGING ACTIONS ---
    fun sendMessage(textVal: String) {
        val partner = _activeChatPartnerEmail.value ?: return
        val me = _currentUserEmail.value
        if (textVal.isBlank()) return

        viewModelScope.launch {
            val newMsg = ChatMessage(
                senderEmail = me,
                recipientEmail = partner,
                text = textVal
            )
            repository.insertMessage(newMsg)

            // Trigger natural automated provider responses under student role to simulate rich interactions
            if (_currentRole.value == "STUDENT") {
                delay(2000)
                val responseMsg = ChatMessage(
                    senderEmail = partner,
                    recipientEmail = me,
                    text = "Akwaba! Thanks for messaging. I will respond carefully and coordinate details. We can meet up on the USTED campus."
                )
                repository.insertMessage(responseMsg)
            }
        }
    }

    // --- PROVIDER SUBSCRIPTION MANAGEMENT ---
    fun upgradeToPremium() {
        viewModelScope.launch {
            val user = currentUserAccount.value ?: return@launch
            if (user.cashBalance < 20.0) {
                addNotification("Upgrade Failed", "Insufficient funds to buy Premium Plan (GHS 20.0). Top up wallet.")
                return@launch
            }
            // Mark user premium, deduct GHS 20.0
            val updatedUser = user.copy(
                subPlan = "Premium",
                isPremium = true,
                cashBalance = user.cashBalance - 20.0
            )
            repository.updateUser(updatedUser)
            addNotification("Upgraded to Premium!", "Your Listings now hold VIP featured ranking and premium promotional badges on the dashboard.")
        }
    }

    // --- ADMIN CONTROLS ---
    fun createAdminCategory(id: String, name: String, desc: String, icon: String) {
        viewModelScope.launch {
            val newCat = CategoryItem(id, name, icon, desc)
            repository.insertCategory(newCat)
            addNotification("Category Added", "Admin created new service category: $name")
        }
    }

    fun registerProviderVenture(
        ventureName: String,
        category: String,
        description: String,
        price: Double,
        unitText: String,
        location: String,
        idType: String,
        idNumber: String
    ) {
        viewModelScope.launch {
            val me = currentUserAccount.value ?: return@launch
            
            // Insert document representing approval state
            val doc = ProviderDocument(
                providerEmail = me.email,
                providerName = ventureName, // Name of venture
                campus = me.campus,
                category = category,
                docType = idType,
                docNumber = idNumber,
                status = "PENDING"
            )
            repository.insertDocument(doc)

            // Look up if they already have a ServiceListing so we replace/overwrite or update it, preventing double entries!
            val allListings = repository.getAllServices().first()
            val existing = allListings.find { it.providerEmail == me.email }
            
            val listing = ServiceListing(
                id = existing?.id ?: 0, // replaces existing or auto generates 0 if new
                providerEmail = me.email,
                providerName = ventureName,
                providerPhone = me.phone,
                category = category.lowercase(),
                title = ventureName,
                priceGHS = price,
                unitText = unitText,
                description = description,
                location = location,
                isVerified = false, // True once Admin approves!
                rating = existing?.rating ?: 4.8,
                reviewCount = existing?.reviewCount ?: 8
            )
            repository.insertService(listing)

            addNotification(
                "Venture Details Submitted", 
                "Venture $ventureName registered for review! Your business profile won't be visible to students until Developer/Admin approves."
            )
        }
    }

    fun submitProviderVerification(idType: String, idNumber: String) {
        viewModelScope.launch {
            val me = currentUserAccount.value ?: return@launch
            val doc = ProviderDocument(
                providerEmail = me.email,
                providerName = me.fullName,
                campus = me.campus,
                category = "General",
                docType = idType,
                docNumber = idNumber,
                status = "PENDING"
            )
            repository.insertDocument(doc)
            addNotification("Documents Uploaded", "Your Student/National ID verification files registered for Admin moderation successfully.")
        }
    }

    fun moderateProviderDocument(providerEmail: String, isApproved: Boolean) {
        viewModelScope.launch {
            val doc = repository.getDocumentForProvider(providerEmail) ?: return@launch
            val newStatus = if (isApproved) "APPROVED" else "REJECTED"
            val updatedDoc = doc.copy(status = newStatus)
            repository.updateDocument(updatedDoc)

            // If approved, update provider user details and mark existing services as verified
            val user = repository.getUserSync(providerEmail)
            if (user != null) {
                val updatedUser = user.copy(isPremium = isApproved) // Admin authorization triggers Premium privilege
                repository.updateUser(updatedUser)
            }

            // Verify listings as well
            val allListings = repository.getAllServices().first()
            allListings.filter { srv -> srv.providerEmail == providerEmail }.forEach { srv ->
                repository.insertService(srv.copy(isVerified = isApproved))
            }

            addNotification(
                "Document Moderated",
                "Successfully marked provider document for $providerEmail as $newStatus."
            )

            repository.insertMessage(
                ChatMessage(
                    senderEmail = "admin@campusconnect.edu.gh",
                    recipientEmail = providerEmail,
                    text = "🛡️ Admin Account Verification: Your uploaded documentation has been evaluated and marked as **$newStatus**!"
                )
            )
        }
    }

    // --- LEAVE SERVICE REVIEWS ---
    fun submitBookingReview(bookingId: Int, score: Int, comments: String) {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId) ?: return@launch
            // Add review record
            val review = ServiceReview(
                serviceId = booking.serviceId,
                providerEmail = booking.providerEmail,
                studentName = booking.studentName,
                rating = score,
                reviewText = comments
            )
            repository.insertReview(review)

            // Change Booking to completed or reviewed status
            val updatedBooking = booking.copy(notes = "${booking.notes} [Reviewed]")
            repository.updateBooking(updatedBooking)

            addNotification("Review Submitted", "Thank you! Gave $score Stars for service: ${booking.serviceTitle}.")
        }
    }
}

// --- SECURE MOMO TRANSACTION STATE WRAPPERS ---
data class CheckoutState(
    val service: ServiceListing,
    val date: String,
    val time: String,
    val notes: String,
    val momoType: String, // MTN MoMo, Telecel Cash, AirtelTigo Money
    val momoNumber: String,
    val amount: Double,
    val otpEntered: String,
    val step: CheckoutStep
)

enum class CheckoutStep {
    OTP_MODAL, COMPLETING, SUCCESS
}

// --- NOTIFICATION UTILS ---
data class SimulatedNotification(
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
