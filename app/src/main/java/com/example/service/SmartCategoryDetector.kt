package com.example.service

import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType

object SmartCategoryDetector {

    private val keywordCategoryMap = mapOf(
        TransactionCategory.FOOD to listOf(
            "swiggy", "zomato", "dinner", "lunch", "breakfast", "cafe", "restaurant",
            "starbucks", "food", "groceries", "burger", "pizza", "coffee", "tea",
            "snack", "subway", "mcdonald", "kfc", "domino", "biryani", "supermarket", "baking"
        ),
        TransactionCategory.TRANSPORT to listOf(
            "uber", "ola", "metro", "bus", "train", "flight", "petrol", "diesel",
            "fuel", "taxi", "rapido", "cab", "parking", "toll", "airline", "commute", "auto"
        ),
        TransactionCategory.SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "clothes", "shoes", "headphones", "gadget",
            "shopping", "mall", "electronics", "zara", "h&m", "ikea", "purchase", "dress", "watch"
        ),
        TransactionCategory.BILLS to listOf(
            "electricity", "bill", "water", "wifi", "broadband", "gas", "recharge",
            "airtel", "jio", "rent", "maintenance", "utility", "insurance", "emi", "loan"
        ),
        TransactionCategory.ENTERTAINMENT to listOf(
            "netflix", "spotify", "prime", "movie", "cinema", "game", "gaming",
            "steam", "concert", "theatre", "disney", "hotstar", "party", "club", "playstation"
        ),
        TransactionCategory.HEALTH to listOf(
            "doctor", "medicine", "pharmacy", "gym", "hospital", "dentist",
            "health", "clinic", "lab", "apollo", "fitness", "supplements", "therapy"
        ),
        TransactionCategory.EDUCATION to listOf(
            "course", "udemy", "tuition", "book", "school", "college",
            "exam", "education", "coursera", "class", "training", "books", "library"
        ),
        TransactionCategory.TRAVEL to listOf(
            "hotel", "resort", "airbnb", "trip", "vacation", "flight", "tour",
            "travel", "booking.com", "makemytrip", "holiday", "luggage", "visa"
        ),
        TransactionCategory.SALARY to listOf(
            "salary", "paycheck", "wages", "bonus", "payroll", "stipend"
        ),
        TransactionCategory.FREELANCE to listOf(
            "freelance", "client", "upwork", "fiverr", "consulting", "contract", "gig", "project fee"
        ),
        TransactionCategory.INVESTMENT to listOf(
            "dividend", "stocks", "mutual fund", "crypto", "interest", "investment", "zerodha", "groww", "etf", "bitcoin"
        )
    )

    /**
     * Given user's description text, returns the best matched category, or null if uncertain.
     */
    fun detectCategory(description: String): Pair<TransactionCategory, TransactionType>? {
        if (description.isBlank()) return null
        val lower = description.lowercase()

        for ((category, keywords) in keywordCategoryMap) {
            for (keyword in keywords) {
                if (lower.contains(keyword)) {
                    val defaultType = if (category.isIncomeOnly) TransactionType.INCOME else TransactionType.EXPENSE
                    return Pair(category, defaultType)
                }
            }
        }
        return null
    }
}
