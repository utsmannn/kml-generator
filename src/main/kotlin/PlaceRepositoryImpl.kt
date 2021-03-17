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
        return "https://discover.search.hereapi.com/v1/discover?at=$latitude,$longitude&q=$query&apikey=$apiKey"
    }

    private fun getReverseSearchUrl(apiKey: String): String {
        return "https://revgeocode.search.hereapi.com/v1/revgeocode?apikey=$apiKey"
    }

    private fun getRoutesUrl(start: Location, destination: Location, modeType: ModeType, apiKey: String, returnResult: String): String {
        val startLoc = "${start.latitude},${start.longitude}"
        val destinationLoc = "${destination.latitude},${destination.longitude}"
        val mode = when (modeType) {
            ModeType.CAR -> "car"
            ModeType.BIKE -> "scooter"
        }
        return "https://router.hereapi.com/v8/routes?" +
                "transportMode=$mode&origin=$startLoc&destination=$destinationLoc&return=$returnResult&apikey=$apiKey"
    }

    override suspend fun searchPlace(query: String, atLocation: Location, hereApiKey: String): SearchPlaceState {
        val response: HerePlaceResult =  try {
            val stringResponse: String = httpClient.get {
                url(getSearchUrl(query, atLocation, hereApiKey))
                contentType(ContentType.Application.Json)
            }

            val format = Json {
                ignoreUnknownKeys = true
            }
            val data = format.decodeFromString<HerePlaceResult>(stringResponse)
            data
        } catch (e: ClientRequestException) {
            console.log("anjir --> \n${e.message}")
            e.printStackTrace()
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

    /*override suspend fun getRoutes(
        start: Location,
        destination: Location,
        modeType: ModeType,
        hereApiKey: String
    ): RoutePlaceState {
        val polyResponseAsync: HereRouteResult = try {
            httpClient.get {
                url(getRoutesUrl(start, destination, modeType, hereApiKey, "polyline"))
                contentType(ContentType.Application.Json)
            }
        } catch (e: ClientRequestException) {
            val errorStatus = e.response.status
            HereRouteResult(errorDescription = "Here API ${errorStatus.description}", routes = null)
        }

        val isError = polyResponseAsync.errorDescription != null && polyResponseAsync.routes == null

        return if (isError) {
            RoutePlaceState.Failed(polyResponseAsync.errorDescription ?: "Failed")
        } else {
            val polyline = polyResponseAsync.getPolyline()

            val rawLatLng = PolylineEncoderDecoder.decode(polyline).map {
                LatLng(latitude = it.lat, longitude = it.lng)
            }

            val googlePoly = PolyUtil.encode(rawLatLng)
            val length = lengthResponseAsync.getLength()

            if (googlePoly != null || length != null) {
                val price = calculatePrice(length, modeType)
                val data = RouteResponse(googlePoly, length, price)
                RoutePlaceState.Success(data)
            } else {
                RoutePlaceState.Failed("Route not found")
            }
        }
    }*/

}