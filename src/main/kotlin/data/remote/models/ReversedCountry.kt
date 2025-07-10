package data.remote.models

data class ReversedCountry(
    val address: Address,
    val addresstype: String,
    val boundingbox: List<String>,
    val category: String,
    val display_name: String,
    val importance: String,
    val lat: String,
    val licence: String,
    val lon: String,
    val name: String,
    val osm_id: String,
    val osm_type: String,
    val place_id: String,
    val place_rank: String,
    val type: String
)