package com.bitey.app.core.location

data class GeocodedAddress(
    val displayName: String,
    val placeName: String? = null,
    val street: String? = null,
    val subLocality: String? = null,
    val city: String? = null,
    val country: String? = null
)
