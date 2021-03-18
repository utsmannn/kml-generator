import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PlaceRepositoryImpl(private val httpClient: HttpClient) : PlaceRepository {

    private fun getSearchUrl(query: String, atLocation: Location, apiKey: String): String {
        val latitude = atLocation.latitude
        val longitude = atLocation.longitude
        return "https://route-here-api.herokuapp.com/search?query=$query&location=$latitude,$longitude&hereApiKey=$apiKey"
    }

    private fun getReverseSearchUrl(apiKey: String): String {
        return "https://revgeocode.search.hereapi.com/v1/revgeocode?apikey=$apiKey"
    }

    private fun getRoutesUrl(start: Location, destination: Location, apiKey: String, result: String): String {
        val startLoc = "${start.latitude},${start.longitude}"
        val destinationLoc = "${destination.latitude},${destination.longitude}"

        return "https://route-here-api.herokuapp.com/route?from=$startLoc&to=$destinationLoc&hereApiKey=$apiKey&result=$result"
    }

    override suspend fun searchPlace(query: String, atLocation: Location, hereApiKey: String): SearchPlaceState {
        val response: PlaceResponses = try {
            val stringResponse: String = httpClient.get {
                url(getSearchUrl(query, atLocation, hereApiKey))
                contentType(ContentType.Application.Json)
            }

            val format = Json {
                ignoreUnknownKeys = true
            }
            format.decodeFromString(stringResponse)
        } catch (e: ClientRequestException) {
            e.printStackTrace()
            val errorStatus = e.response.status
            PlaceResponses(places = null, error = "Here API ${errorStatus.description}")
        }

        val isError = response.error != null && response.places == null
        return if (isError) {
            SearchPlaceState.Failed(response.error ?: "Failed")
        } else {
            val data = response.places
            if (!data.isNullOrEmpty()) {
                SearchPlaceState.Success(data)
            } else {
                SearchPlaceState.Failed("Place not found")
            }
        }
    }

    override suspend fun getRoutes(
        start: Location,
        destination: Location,
        hereApiKey: String
    ): RoutePlaceState {
        console.log("try get route....")
        val response: RouteResponse = try {
            val stringResponse: String = httpClient.get {
                url(getRoutesUrl(start, destination, hereApiKey, "coordinate"))
                contentType(ContentType.Application.Json)
            }

            val format = Json {
                ignoreUnknownKeys = true
            }
            format.decodeFromString(stringResponse)
        } catch (e: ClientRequestException) {
            e.printStackTrace()
            val errorStatus = e.response.status
            RouteResponse(error = "Here API ${errorStatus.description}", route = null)
        }

        val isError = response.error != null && response.route == null
        return if (isError) {
            RoutePlaceState.Failed(response.error ?: "Failed")
        } else {
            val data = response.route
            if (data != null) {
                RoutePlaceState.Success(data = response.route)
            } else {
                RoutePlaceState.Failed("Route not found")
            }
        }
    }

    /*override suspend fun reverseSearchPlace(hereApiKey: String): SearchPlaceState {
        val response =  try {
            httpClient.get {
                url(getReverseSearchUrl(hereApiKey))
                contentType(ContentType.Application.Json)
            }
        } catch (e: ClientRequestException) {
            val errorStatus = e.response.status
            HerePlaceResult(errorDescription = "Here API ${errorStatus.description}", items = null)
        }

        console.log("response is --> $response")

        val isError = response.errorDescription != null && response.items == null
        return if (isError) {
            SearchPlaceState.Failed(response.errorDescription ?: "Failed")
        } else {
            val data = response.items?.map { it.mapToPlaceResponse() }
            if (!data.isNullOrEmpty()) {
                SearchPlaceState.Success(data)
            } else {
                SearchPlaceState.Failed("Place not found")
            }
        }
    }*/
}