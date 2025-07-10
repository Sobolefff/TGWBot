package data.remote.models

data class Address(
    val country: String,
    val country_code: String,
    val postcode: String,
    val road: String,
    val state: String,
    val state_district: String,
    val village: String
)