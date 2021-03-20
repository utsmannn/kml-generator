package data

import kotlinx.serialization.Serializable

enum class ModeType {
    BIKE, CAR
}

data class CoordinateResult(
    var from: Location = Location(),
    var to: Location = Location()
)

@Serializable
data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bearing: Float? = 0f
)

@Serializable
data class RouteResponse(
    val route: Route?,
    val error: String? = null
)

@Serializable
data class Route(
    val geocode: List<Location>
)

@Serializable
data class PlaceResponses(
    val places: List<Place>?,
    val error: String? = null
)

@Serializable
data class Place(
    val hereId: String,
    val title: String,
    val address: String,
    val district: String,
    val city: String,
    val location: Location,
    val distance: Double,
    val category: String? = null
)