

sealed class SearchPlaceState {
    data class Success(val data: List<Place>) : SearchPlaceState()
    data class Failed(val reason: String) : SearchPlaceState()
}

sealed class RoutePlaceState {
    data class Success(val data: Route) : RoutePlaceState()
    data class Failed(val reason: String) : RoutePlaceState()
}