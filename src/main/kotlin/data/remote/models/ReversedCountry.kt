package data.remote.models

data class ReversedCountry(
    val address: Address,
    val addresstype: String,
    val boundingbox: List<String>,
    val `class`: String,
    val display_name: String,
    val importance: Double,
    val lat: String,
    val licence: String,
    val lon: String,
    val name: String,
    val osm_id: Long,
    val osm_type: String,
    val place_id: Long,
    val place_rank: Int,
    val type: String
)
