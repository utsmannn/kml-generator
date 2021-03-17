import kotlinx.serialization.Serializable

enum class ModeType {
    BIKE, CAR
}

@Serializable
data class Location(
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val bearing: Float? = 0f
)

@Serializable
data class RouteResponse(
    val geocode: List<Location>
)

@Serializable
data class PlaceResponse(
    val hereId: String,
    val title: String,
    val address: String,
    val district: String,
    val city: String,
    val location: Location,
    val distance: Double,
    val category: String? = null
)

@Serializable
data class HerePlaceResult(
    val items: List<Item>?,
    val errorDescription: String? = null
) {

    @Serializable
    data class Item(
        val id: String? = null,
        val title: String? = null,
        val address: Address? = null,
        val position: Position? = null,
        val distance: Double? = null,
        val categories: List<Category>? = emptyList()
    ) {
        fun mapToPlaceResponse(): PlaceResponse {
            val newAddress = address?.label?.removePrefix("$title, ")
            val newTitle = title?.removePrefix("$title, ")
            return PlaceResponse(
                hereId = id ?: "",
                title = newTitle ?: "Unknown place",
                address = newAddress ?: "Unknown address",
                district = address?.district ?: "Unknown district",
                city = address?.city ?: "Unknown city",
                location = position?.toLocation() ?: Location(),
                distance = distance ?: 0.0,
                category = categories?.firstOrNull()?.name
            )
        }
    }

    @Serializable
    data class Address(
        val label: String? = null,
        val street: String? = null,
        val district: String? = null,
        val subdistrict: String? = null,
        val city: String? = null,
        val country: String? = null
    )

    @Serializable
    data class Position(
        val lat: Double = 0.0,
        val lng: Double = 0.0
    ) {
        fun toLocation(): Location {
            return Location(
                latitude = lat,
                longitude = lng
            )
        }
    }

    @Serializable
    data class Category(
        val name: String
    )
}

@Serializable
data class HereRouteResult(
    val routes: List<Route>?,
    val errorDescription: String?
) {

    @Serializable
    data class Section(
        val id: String,
        val polyline: String,
        val summary: Summary,
        val transport: Transport
    )

    @Serializable
    data class Summary(
        val length: Long
    )

    @Serializable
    data class Route(
        val sections: List<Section>
    )

    @Serializable
    data class Transport(
        val mode: String
    )

    fun getPolyline(): String? {
        return routes?.firstOrNull()?.sections?.firstOrNull()?.polyline
    }

    fun getLength(): Long? {
        return routes?.firstOrNull()?.sections?.firstOrNull()?.summary?.length
    }
}