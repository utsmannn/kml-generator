

sealed class SearchPlaceState {
    data class Success(val data: List<PlaceResponse>) : SearchPlaceState()
    data class Failed(val reason: String) : SearchPlaceState()
}

sealed class RoutePlaceState {
    data class Success(val data: RouteResponse?) : RoutePlaceState()
    data class Failed(val reason: String) : RoutePlaceState()
}