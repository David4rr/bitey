package com.bitey.app.core.database.model

val DEFAULT_ENTRY_TAGS = listOf(
    // Taste
    TagEntity(tagName = "Spicy", category = "Taste"),
    TagEntity(tagName = "Savory", category = "Taste"),
    TagEntity(tagName = "Sweet", category = "Taste"),
    TagEntity(tagName = "Umami", category = "Taste"),
    TagEntity(tagName = "Crispy", category = "Taste"),
    TagEntity(tagName = "Smoky", category = "Taste"),
    TagEntity(tagName = "Rich", category = "Taste"),
    // Ambience
    TagEntity(tagName = "Street Food", category = "Ambience"),
    TagEntity(tagName = "Cozy Cafe", category = "Ambience"),
    TagEntity(tagName = "Casual Eatery", category = "Ambience"),
    TagEntity(tagName = "Fine Dining", category = "Ambience"),
    TagEntity(tagName = "Hidden Gem", category = "Ambience"),
    TagEntity(tagName = "Night Market", category = "Ambience"),
    // Diet
    TagEntity(tagName = "Halal", category = "Diet"),
    TagEntity(tagName = "Vegetarian", category = "Diet"),
    TagEntity(tagName = "High Protein", category = "Diet"),
    TagEntity(tagName = "Dessert", category = "Diet")
)
