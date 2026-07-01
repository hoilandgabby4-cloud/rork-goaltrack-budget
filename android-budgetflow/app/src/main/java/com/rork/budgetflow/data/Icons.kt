package com.rork.budgetflow.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Train
import androidx.compose.ui.graphics.vector.ImageVector

/** Stable string keys mapped to Material icons so we can serialize icon choices. */
object IconCatalog {
    val entries: List<Pair<String, ImageVector>> = listOf(
        "cart" to Icons.Rounded.LocalGroceryStore,
        "food" to Icons.Rounded.Fastfood,
        "coffee" to Icons.Rounded.LocalCafe,
        "shopping" to Icons.Rounded.ShoppingBag,
        "home" to Icons.Rounded.Home,
        "car" to Icons.Rounded.DirectionsCar,
        "train" to Icons.Rounded.Train,
        "bolt" to Icons.Rounded.Bolt,
        "phone" to Icons.Rounded.Phone,
        "movie" to Icons.Rounded.Movie,
        "game" to Icons.Rounded.SportsEsports,
        "gym" to Icons.Rounded.FitnessCenter,
        "health" to Icons.Rounded.Favorite,
        "spa" to Icons.Rounded.Spa,
        "school" to Icons.Rounded.School,
        "clothes" to Icons.Rounded.Checkroom,
        "pets" to Icons.Rounded.Pets,
        "flight" to Icons.Rounded.Flight,
        "music" to Icons.Rounded.MusicNote,
        "art" to Icons.Rounded.Palette,
        "baby" to Icons.Rounded.ChildCare,
        "child" to Icons.Rounded.ChildCare,
        "gift" to Icons.Rounded.CardGiftcard,
        "subscription" to Icons.Rounded.Subscriptions,
        "travel" to Icons.Rounded.Luggage,
        "laptop" to Icons.Rounded.Laptop,
        "savings" to Icons.Rounded.Savings,
        "star" to Icons.Rounded.Star,
        "wallet" to Icons.Rounded.AccountBalanceWallet,
        "bank" to Icons.Rounded.AccountBalance,
        "card" to Icons.Rounded.CreditCard,
        "pay" to Icons.Rounded.Payments,
        "vehicle" to Icons.Rounded.DirectionsCar,
        "house" to Icons.Rounded.Home,
        "retirement" to Icons.Rounded.Savings,
        "insurance" to Icons.Rounded.HealthAndSafety,
        "healthcare" to Icons.Rounded.HealthAndSafety,
    )

    private val map: Map<String, ImageVector> = entries.toMap()

    fun icon(key: String): ImageVector = map[key] ?: Icons.Rounded.Payments
}
