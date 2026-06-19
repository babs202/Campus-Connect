package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusConnectDao {

    // --- USERS ---
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserSync(email: String): UserAccount?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserFlow(email: String): Flow<UserAccount?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)


    // --- CATEGORIES ---
    @Query("SELECT * FROM categories")
    fun getAllCategoriesFlow(): Flow<List<CategoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryItem)

    @Delete
    suspend fun deleteCategory(category: CategoryItem)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int


    // --- SERVICES ---
    @Query("SELECT * FROM services ORDER BY isPremium DESC, rating DESC")
    fun getAllServicesFlow(): Flow<List<ServiceListing>>

    @Query("SELECT * FROM services WHERE category = :category ORDER BY isPremium DESC, rating DESC")
    fun getServicesByCategoryFlow(category: String): Flow<List<ServiceListing>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: Int): ServiceListing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceListing)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteServiceById(id: Int)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int


    // --- BOOKINGS ---
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookingsFlow(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE studentEmail = :email ORDER BY timestamp DESC")
    fun getBookingsForStudentFlow(email: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE providerEmail = :email ORDER BY timestamp DESC")
    fun getBookingsForProviderFlow(email: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Int): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)


    // --- CHAT MESSAGES ---
    @Query("SELECT * FROM messages WHERE (senderEmail = :userA AND recipientEmail = :userB) OR (senderEmail = :userB AND recipientEmail = :userA) ORDER BY timestamp ASC")
    fun getChatHistoryFlow(userA: String, userB: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessage)


    // --- DOCUMENTS (VERIFICATION) ---
    @Query("SELECT * FROM provider_documents ORDER BY submittedAt DESC")
    fun getAllDocumentsFlow(): Flow<List<ProviderDocument>>

    @Query("SELECT * FROM provider_documents WHERE providerEmail = :email")
    suspend fun getDocumentForProvider(email: String): ProviderDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: ProviderDocument)

    @Update
    suspend fun updateDocument(doc: ProviderDocument)


    // --- REVIEWS ---
    @Query("SELECT * FROM reviews WHERE providerEmail = :email ORDER BY timestamp DESC")
    fun getReviewsForProviderFlow(email: String): Flow<List<ServiceReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ServiceReview)
}

@Database(
    entities = [
        UserAccount::class,
        CategoryItem::class,
        ServiceListing::class,
        Booking::class,
        ChatMessage::class,
        ProviderDocument::class,
        ServiceReview::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CampusConnectDatabase : RoomDatabase() {
    abstract val dao: CampusConnectDao
}
