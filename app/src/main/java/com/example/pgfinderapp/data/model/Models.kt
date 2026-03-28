package com.example.pgfinderapp.data.model

enum class Role { GUEST, OWNER }
enum class FoodType(val displayName: String) {
    VEG("Veg"),
    NON_VEG("Non-Veg"),
    BOTH("Veg & Non-Veg"),
    NONE("None")
}
enum class ACType(val displayName: String) {
    AC("AC"),
    NON_AC("NON-AC"),
    BOTH("AC&NON-AC")
}

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val role: Role,
    val email: String = "",
    val password: String = ""
)

data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String
)

data class PG(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val alternatePhone: String?,
    val name: String,
    val address: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val capacity: Int,
    val availableBeds: Int,
    val costPerMonth: Int,
    val foodType: FoodType,
    val acType: ACType,
    val bedsInRoom: Int,
    val rating: Double,
    val images: List<String>,
    val reviews: List<Review>,
    val isVerified: Boolean,
    val mapLink: String? = null,
    val rules: String? = null
)

val initialPGs = listOf(
    PG(
        id = "1", ownerId = "owner1", ownerName = "Rahul Sharma", ownerPhone = "9876543210", ownerEmail = "rahul@example.com", alternatePhone = null, name = "Sunrise PG for Men",
        address = "123 Main St, Koramangala", location = "Koramangala, Bangalore",
        latitude = 12.9352, longitude = 77.6245,
        capacity = 50, availableBeds = 10, costPerMonth = 8000, foodType = FoodType.BOTH, acType = ACType.BOTH, bedsInRoom = 2,
        rating = 4.5, images = listOf(), reviews = listOf(
            Review("r1", "u1", "Rahul", 4, "Good food and clean rooms.")
        ), isVerified = true, mapLink = "https://goo.gl/maps/example1",
        rules = "1. No smoking\n2. Entry till 11 PM\n3. Guests not allowed overnight"
    ),
    PG(
        id = "2", ownerId = "owner1", ownerName = "Rahul Sharma", ownerPhone = "9876543210", ownerEmail = "rahul@example.com", alternatePhone = null, name = "Comfort Stay Women PG",
        address = "456 Cross Rd, HSR Layout", location = "HSR Layout, Bangalore",
        latitude = 12.9116, longitude = 77.6389,
        capacity = 30, availableBeds = 5, costPerMonth = 10000, foodType = FoodType.VEG, acType = ACType.AC, bedsInRoom = 1,
        rating = 4.8, images = listOf(), reviews = listOf(), isVerified = true, mapLink = "https://goo.gl/maps/example2",
        rules = "1. Maintain cleanliness\n2. No loud music after 10 PM"
    ),
    PG(
        id = "3", ownerId = "guest1", ownerName = "Amit Kumar", ownerPhone = "9123456780", ownerEmail = "amit@example.com", alternatePhone = "9988776655", name = "Budget PG",
        address = "789 2nd Main, BTM Layout", location = "BTM Layout, Bangalore",
        latitude = 12.9166, longitude = 77.6101,
        capacity = 100, availableBeds = 20, costPerMonth = 5000, foodType = FoodType.NONE, acType = ACType.NON_AC, bedsInRoom = 4,
        rating = 3.2, images = listOf(), reviews = listOf(), isVerified = false, mapLink = null, rules = null
    )
)

val initialUsers = listOf(
    User("guest1", "Guest User", 22, Role.GUEST, "guest@test.com", "password123"),
    User("owner1", "Owner User", 35, Role.OWNER, "owner@test.com", "password123")
)
