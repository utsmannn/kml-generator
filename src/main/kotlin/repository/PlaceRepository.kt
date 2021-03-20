package repository

import data.Location
import data.RoutePlaceState
import data.SearchPlaceState

interface PlaceRepository {
    suspend fun searchPlace(query: String, atLocation: Location, hereApiKey: String): SearchPlaceState
    suspend fun getRoutes(start: Location, destination: Location, hereApiKey: String): RoutePlaceState

    //suspend fun reverseSearchPlace(atLocation: data.Location, hereApiKey: String): data.SearchPlaceState
    /*suspend fun getRoutes(start: data.Location, destination: data.Location, modeType: data.ModeType, hereApiKey: String): data.RoutePlaceState*/

    companion object {
        const val HERE_API_PARAM = "Here-Api-Key"
    }
}