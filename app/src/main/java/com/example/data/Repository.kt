package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CampusConnectRepository(private val dao: CampusConnectDao) {

    // --- USERS ---
    fun getUserFlow(email: String): Flow<UserAccount?> = dao.getUserFlow(email)
    fun getAllUsersFlow(): Flow<List<UserAccount>> = dao.getAllUsersFlow()
    suspend fun getUserSync(email: String): UserAccount? = dao.getUserSync(email)
    suspend fun insertUser(user: UserAccount) = dao.insertUser(user)
    suspend fun updateUser(user: UserAccount) = dao.updateUser(user)

    // --- CATEGORIES ---
    fun getAllCategories(): Flow<List<CategoryItem>> = dao.getAllCategoriesFlow()
    suspend fun insertCategory(category: CategoryItem) = dao.insertCategory(category)
    suspend fun deleteCategory(category: CategoryItem) = dao.deleteCategory(category)

    // --- SERVICES ---
    fun getAllServices(): Flow<List<ServiceListing>> = dao.getAllServicesFlow()
    fun getServicesByCategory(category: String): Flow<List<ServiceListing>> = dao.getServicesByCategoryFlow(category)
    suspend fun getServiceById(id: Int): ServiceListing? = dao.getServiceById(id)
    suspend fun insertService(service: ServiceListing) = dao.insertService(service)
    suspend fun deleteServiceById(id: Int) = dao.deleteServiceById(id)

    // --- BOOKINGS ---
    fun getAllBookings(): Flow<List<Booking>> = dao.getAllBookingsFlow()
    fun getBookingsForStudent(email: String): Flow<List<Booking>> = dao.getBookingsForStudentFlow(email)
    fun getBookingsForProvider(email: String): Flow<List<Booking>> = dao.getBookingsForProviderFlow(email)
    suspend fun getBookingById(id: Int): Booking? = dao.getBookingById(id)
    suspend fun insertBooking(booking: Booking): Long = dao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = dao.updateBooking(booking)

    // --- MESSAGES ---
    fun getChatHistory(userA: String, userB: String): Flow<List<ChatMessage>> = dao.getChatHistoryFlow(userA, userB)
    fun getAllMessagesFlow(): Flow<List<ChatMessage>> = dao.getAllMessagesFlow()
    suspend fun insertMessage(msg: ChatMessage) = dao.insertMessage(msg)

    // --- DOCUMENTS (VERIFICATION) ---
    fun getAllDocuments(): Flow<List<ProviderDocument>> = dao.getAllDocumentsFlow()
    suspend fun getDocumentForProvider(email: String): ProviderDocument? = dao.getDocumentForProvider(email)
    suspend fun insertDocument(doc: ProviderDocument) = dao.insertDocument(doc)
    suspend fun updateDocument(doc: ProviderDocument) = dao.updateDocument(doc)

    // --- REVIEWS ---
    fun getReviewsForProvider(email: String): Flow<List<ServiceReview>> = dao.getReviewsForProviderFlow(email)
    suspend fun insertReview(review: ServiceReview) = dao.insertReview(review)

    // --- PREPOPULATE DATABASE METHOD ---
    suspend fun prepopulateIfEmpty() {
        if (dao.getCategoryCount() == 0) {
            val devCategories = listOf(
                CategoryItem("academic", "Academic Services", "School", "Tutors, typing services, homework, and proofreading support"),
                CategoryItem("personal", "Personal Services", "ContentCut", "Hairdressers, barbers, private campus fitness coaches"),
                CategoryItem("essentials", "Campus Essentials", "LocalLaundryService", "Laundry workers, photocopying, prints delivery"),
                CategoryItem("food", "Food & Delivery", "Restaurant", "Jollof rice vendors, fresh grocery and late night snack runs"),
                CategoryItem("creative", "Creative Services", "PhotoCamera", "Event photographers, videographers, posters & logo design"),
                CategoryItem("tech", "Technology Services", "Computer", "Phone/PC technician, device repair, software support"),
                CategoryItem("events", "Events Support", "Speaker", "Event planners, sound rental owners, student MC services")
            )
            for (cat in devCategories) {
                dao.insertCategory(cat)
            }
        }

        if (dao.getServiceCount() == 0) {
            val devServices = listOf(
                ServiceListing(
                    providerEmail = "kwame.asante@usted.edu.gh",
                    providerName = "Kwame Asante Cuts",
                    providerPhone = "+233 241234567",
                    category = "personal",
                    title = "Classic Clipper Haircuts",
                    priceGHS = 25.0,
                    unitText = "per trim",
                    description = "Professional barbershop cuts at USTED. Located at South Campus Hostel Room 12. Complete sanitization, sharp line-ups, and skin fades.",
                    rating = 4.9,
                    reviewCount = 42,
                    location = "South Campus Hostel, Room 12",
                    isVerified = true,
                    isPremium = true
                ),
                ServiceListing(
                    providerEmail = "abena.serwaa@usted.edu.gh",
                    providerName = "SassyBraids Salon & Makeup",
                    providerPhone = "+233 247890123",
                    category = "personal",
                    title = "Knotless Braids & Beauty",
                    priceGHS = 130.0,
                    unitText = "per style",
                    description = "Expert braids, makeup, and eyelashes. I travel directly to your hostel/dormitory. Bring your details or let me buy matching attachments.",
                    rating = 4.8,
                    reviewCount = 58,
                    location = "DST Hostel / Mobile Services",
                    isVerified = true,
                    isPremium = false
                ),
                ServiceListing(
                    providerEmail = "kenneth.mensah@usted.edu.gh",
                    providerName = "Ken's Academic Tutors",
                    providerPhone = "+233 209998887",
                    category = "academic",
                    title = "Algebra & Statistics Prep",
                    priceGHS = 45.0,
                    unitText = "per hour",
                    description = "Struggling with Business Maths or ICT tests? I break things down logically. Excellent grades guaranteed, complete revision with past questions.",
                    rating = 4.7,
                    reviewCount = 19,
                    location = "USTED Library / Block C Study Room",
                    isVerified = true,
                    isPremium = true
                ),
                ServiceListing(
                    providerEmail = "elikplim.sosu@usted.edu.gh",
                    providerName = "SparkleWash USTED",
                    providerPhone = "+233 249000111",
                    category = "essentials",
                    title = "Laundry Pickup & Wash Fold",
                    priceGHS = 35.0,
                    unitText = "per large bag",
                    description = "We wash, tumble dry, and meticulously fold. Mobile pickup from your hostel and drop-off in under 24 hours. Affordable pricing built for students.",
                    rating = 5.0,
                    reviewCount = 82,
                    location = "Block B hostel block, Kumasi Campus",
                    isVerified = true,
                    isPremium = true
                ),
                ServiceListing(
                    providerEmail = "aba.mansa@usted.edu.gh",
                    providerName = "Aba's Jollof & Waakye Hub",
                    providerPhone = "+233 551112223",
                    category = "food",
                    title = "Vibrant Jollof & Gari Fort",
                    priceGHS = 20.0,
                    unitText = "per standard pack",
                    description = "Delicious Ghanaian Jollof rice with fried fish/chicken, pepper sauce, and egg. Rich Waakye with shito, we deliver anywhere within the USTED campus gates.",
                    rating = 4.8,
                    reviewCount = 110,
                    location = "Main Gate Food Row / Delivery",
                    isVerified = true,
                    isPremium = false
                ),
                ServiceListing(
                    providerEmail = "derrick.osei@usted.edu.gh",
                    providerName = "Derrick Speedy Prints",
                    providerPhone = "+233 596111122",
                    category = "essentials",
                    title = "Thesis Typing & Neat Binding",
                    priceGHS = 1.5,
                    unitText = "per page",
                    description = "Fast, high-accuracy typesetting, research proofreading, and assignment typing. We also print and spine-bind major project deliverables on campus.",
                    rating = 4.6,
                    reviewCount = 12,
                    location = "Opposite Main Hall Block",
                    isVerified = true,
                    isPremium = false
                ),
                ServiceListing(
                    providerEmail = "dr.tech@gmail.com",
                    providerName = "Dr. Tech repairs",
                    providerPhone = "+233 503456789",
                    category = "tech",
                    title = "Cracked Screen & RAM Upgrades",
                    priceGHS = 90.0,
                    unitText = "per diagnostic",
                    description = "Hardware repair of iPhones, Androids, MacBooks, and Windows PCs. Operating system installs, data recovery, malware removal at budget rates.",
                    rating = 4.9,
                    reviewCount = 49,
                    location = "USTED Tech Square Stall A",
                    isVerified = true,
                    isPremium = true
                ),
                ServiceListing(
                    providerEmail = "michael.ofori@usted.edu.gh",
                    providerName = "USTED Prime Capture",
                    providerPhone = "+233 245678901",
                    category = "creative",
                    title = "Professional Birthday & Group Shoots",
                    priceGHS = 250.0,
                    unitText = "per outdoor session",
                    description = "Includes 15 fully-edited digital shots, custom lighting, styling recommendations. Highly popular service for campus graduates and student influencers.",
                    rating = 4.7,
                    reviewCount = 31,
                    location = "USTED Botanical Gardens / Campus",
                    isVerified = true,
                    isPremium = false
                )
            )
            for (srv in devServices) {
                dao.insertService(srv)
            }
        }

        // Initialize sample users so all 3 roles work instantly on first load
        initSampleUser("student@usted.edu.gh", "Amina Osei-Kofi", "+233 241112222", "STUDENT", 60.0)
        initSampleUser("kwame.asante@usted.edu.gh", "Kwame Asante cuts", "+233 241234567", "PROVIDER", 280.0)
        initSampleUser("elikplim.sosu@usted.edu.gh", "Elikplim Sosu (SparkleWash)", "+233 249000111", "PROVIDER", 450.0)
        initSampleUser("admin@campusconnect.edu.gh", "USTED Coordinator", "+233 596500361", "ADMIN", 1200.0)

        // Preload standard verification documents for Kwame and Abena, so that admin looks full
        if (dao.getAllDocumentsFlow().first().isEmpty()) {
            dao.insertDocument(ProviderDocument("kwame.asante@usted.edu.gh", "Kwame Asante Cuts", "Main Campus", "Personal Services", "Student ID", "USTED-2024-COMP-192", "APPROVED"))
            dao.insertDocument(ProviderDocument("abena.serwaa@usted.edu.gh", "SassyBraids Salon", "Main Campus", "Personal Services", "National ID", "GH-882200334-9", "PENDING"))
            dao.insertDocument(ProviderDocument("elikplim.sosu@usted.edu.gh", "SparkleWash USTED", "Main Campus", "Campus Essentials", "Student ID", "USTED-2023-MGT-092", "PENDING"))
        }

        // Add some preloaded messages to build an interactive initial chat history
        if (dao.getAllMessagesFlow().first().isEmpty()) {
            dao.insertMessage(ChatMessage(senderEmail = "student@usted.edu.gh", recipientEmail = "kwame.asante@usted.edu.gh", text = "Hi Kwame, do you have space around 2 PM today for a haircut?", timestamp = System.currentTimeMillis() - 3600000))
            dao.insertMessage(ChatMessage(senderEmail = "kwame.asante@usted.edu.gh", recipientEmail = "student@usted.edu.gh", text = "Yes Amina! Main hostel room 12. See you then.", timestamp = System.currentTimeMillis() - 1800000))
        }
    }

    private suspend fun initSampleUser(email: String, name: String, phone: String, role: String, cash: Double) {
        if (dao.getUserSync(email) == null) {
            dao.insertUser(UserAccount(
                email = email,
                fullName = name,
                phone = phone,
                roleType = role,
                university = "USTED",
                campus = "Main Campus",
                cashBalance = cash,
                isPremium = role == "PROVIDER", // Set providers as premium by default for robust demo layout
                subPlan = if (role == "PROVIDER") "Premium" else "Free"
            ))
        }
    }
}
