package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val fullName: String,
    val phone: String,
    val roleType: String, // "STUDENT", "PROVIDER", "ADMIN"
    val university: String = "USTED",
    val campus: String = "Main Campus",
    val profilePhoto: String = "",
    val cashBalance: Double = 500.0, // Default virtual balance for demo
    val isPremium: Boolean = false,
    val subPlan: String = "Free", // "Free", "Premium"
    val password: String = "password"
)

@Entity(tableName = "categories")
data class CategoryItem(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val description: String
)

@Entity(tableName = "services")
data class ServiceListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerEmail: String,
    val providerName: String,
    val providerPhone: String,
    val category: String,
    val title: String,
    val priceGHS: Double,
    val unitText: String, // e.g. "per page", "per run", "per session"
    val description: String,
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val location: String,
    val isVerified: Boolean = true,
    val isPremium: Boolean = false
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentEmail: String,
    val studentName: String,
    val studentPhone: String,
    val serviceId: Int,
    val serviceTitle: String,
    val providerEmail: String,
    val providerName: String,
    val category: String,
    val priceGHS: Double,
    val dateText: String,
    val timeText: String,
    val notes: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    val paymentMethod: String = "",
    val paymentNumber: String = "",
    val paymentTxRef: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderEmail: String,
    val recipientEmail: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val imageWebUrl: String = "" // Simulation support for image sending
)

@Entity(tableName = "provider_documents")
data class ProviderDocument(
    @PrimaryKey val providerEmail: String,
    val providerName: String,
    val campus: String,
    val category: String,
    val docType: String, // "Student ID", "National ID", "Business Cert"
    val docNumber: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ServiceReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceId: Int,
    val providerEmail: String,
    val studentName: String,
    val rating: Int,
    val reviewText: String,
    val replyText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
