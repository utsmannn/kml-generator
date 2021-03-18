
interface PlaceRepository {
    suspend fun searchPlace(query: String, atLocation: Location, hereApiKey: String): SearchPlaceState
    suspend fun getRoutes(start: Location, destination: Location, hereApiKey: String): RoutePlaceState

    //suspend fun reverseSearchPlace(atLocation: Location, hereApiKey: String): SearchPlaceState
    /*suspend fun getRoutes(start: Location, destination: Location, modeType: ModeType, hereApiKey: String): RoutePlaceState*/

    companion object {
        const val HERE_API_PARAM = "Here-Api-Key"
    }
}