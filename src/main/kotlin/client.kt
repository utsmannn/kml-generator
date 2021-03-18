import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import kotlin.js.Date

const val hereApiKey = "rujdNo1Z-UlY47ipLzcvIkgXpiNSvdYdTxpiDRV-Z6I"

var isInputStartReady = false
var isInputEndReady = false

var coordinateResult = CoordinateResult()
var inputIdFocus = ""

val httpClient = HttpClient(Js) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}

val placeRepository: PlaceRepository = PlaceRepositoryImpl(httpClient)

fun main() {
    window.onload = {
        document.body?.sayHello()
    }
}

fun Node.sayHello() {
    append {
        div {
            id = "form"
            div(classes = "row") {
                div(classes = "card col") {
                    id = "input-container"
                    div(classes = "card-content") {
                        div(classes = "input-field s6") {
                            input(type = InputType.text) {
                                placeholder = "Start location"
                                id = "inputStart"

                                val searchJob = debounceJob<String> {
                                    loaderShow()
                                    GlobalScope.launch {
                                        console.log("search for --> $it")
                                        searchApi(it)
                                    }
                                }

                                onInputFunction = {
                                    val newVal = document.getElementById("inputStart") as? HTMLInputElement
                                    val newString = newVal?.value
                                    loaderClear()
                                    searchJob.job?.cancel()
                                    if (newString != null && newString.count() > 2) {
                                        searchJob.param(newString)
                                    } else {
                                        removeList()
                                    }

                                    inputIdFocus = "inputStart"
                                }
                            }
                        }
                        div(classes = "input-field s6") {
                            input(type = InputType.text) {
                                placeholder = "End location"
                                id = "inputEnd"

                                val searchJob = debounceJob<String> {
                                    loaderShow()
                                    GlobalScope.launch {
                                        console.log("search for --> $it")
                                        searchApi(it)
                                    }
                                }

                                onInputFunction = {
                                    val newVal = document.getElementById("inputEnd") as? HTMLInputElement
                                    val newString = newVal?.value
                                    loaderClear()
                                    searchJob.job?.cancel()
                                    if (newString != null && newString.count() > 2) {
                                        searchJob.param(newString)
                                    } else {
                                        removeList()
                                    }

                                    inputIdFocus = "inputEnd"
                                }
                            }
                        }

                        div {
                            id = "loader-container"
                        }

                        button(classes = "btn waves-effect waves-light") {
                            p {
                                text("Test")
                            }
                            onClickFunction = {
                                coordinateResult = CoordinateResult()

                                console.log("start test")
                                val coordinate = jsCoordinate()

                                console.log("coor -> $coordinate")
                                console.log("clear done...")
                            }
                        }
                    }
                }

                div(classes = "col") {
                    id = "container-list"
                }
            }
        }

        div {
            id = "map"
        }
    }
}

fun getHereApiKey(): String {
    // logic for custom here api (future)
    return hereApiKey
}

suspend fun searchApi(query: String) {
    val location = getCoordinate()
    when (val state = placeRepository.searchPlace(query, location, getHereApiKey())) {
        is SearchPlaceState.Success -> {
            val data = state.data
            console.log(data)

            val newVal = document.getElementById("inputStart") as? HTMLInputElement
            val mustVisible = (newVal?.value?.count() ?: 0) > 2
            if (mustVisible) {
                createList(data, inputIdFocus)
            } else {
                removeList()
            }

            loaderClear()
        }
        is SearchPlaceState.Failed -> {
            val error = state.reason
            console.log(error)
            showError(error)
        }
    }
}

fun getCoordinate(): Location {
    val coordinate = jsCoordinate()
    val latLng = coordinate.split(",").map { it.toDouble() }
    return Location(latitude = latLng[0], longitude = latLng[1])
}

fun jsCoordinate(): String {
    return js(
        "" +
                "var position = [pos.coords.latitude, pos.coords.longitude];\n" +
                "position.toString();" +
                ""
    ) as String
}

fun removeList() {
    val element = document.getElementById("container-list") as? HTMLDivElement
    element?.clear()
}

fun createList(newList: List<Place>, inputIdFocus: String) {
    val element = document.getElementById("container-list") as? HTMLDivElement
    element?.clear()
    element?.append {
        div(classes = "card") {
            id = "container-scroll"
            ul {
                newList.forEach { item ->
                    a {
                        id = "item-container"
                        href = "#"
                        onClickFunction = {
                            if (inputIdFocus == "inputStart") {
                                isInputStartReady = true
                                coordinateResult.apply {
                                    from = item.location
                                }
                                GlobalScope.launch {
                                    searchRoute()
                                }
                            }

                            if (inputIdFocus == "inputEnd") {
                                isInputEndReady = true
                                coordinateResult.apply {
                                    to = item.location
                                }
                                GlobalScope.launch {
                                    searchRoute()
                                }
                            }

                            val inputFocus = document.getElementById(inputIdFocus) as? HTMLInputElement
                            inputFocus?.value = item.title
                            removeList()
                        }

                        p {
                            id = "item-title"
                            text(item.title)
                        }
                        p {
                            id = "item-address"
                            text(item.address)
                        }
                        br
                    }
                }
            }
        }
    }
}

fun loaderShow() {
    val element = document.getElementById("loader-container") as? HTMLDivElement
    element?.clear()
    element?.append {
        div("progress") {
            div("indeterminate")
        }
    }
}

fun loaderClear() {
    val element = document.getElementById("loader-container") as? HTMLDivElement
    element?.clear()
}

fun showError(error: String) {
    val element = document.getElementById("loader-container") as? HTMLDivElement
    element?.clear()
    console.log("show progress...")
    element?.append {
        div {
            p {
                id = "error-text"
                text(error)
            }
        }
    }
}

suspend fun searchRoute() {
    if (coordinateResult.to.latitude != null && coordinateResult.from.latitude != null) {
        removeLayer()
        console.log(coordinateResult)

        loaderShow()
        when (val state = placeRepository.getRoutes(coordinateResult.from, coordinateResult.to, getHereApiKey())) {
            is RoutePlaceState.Success -> {
                val geocode = state.data.geocode
                showPolyline(geocode)
                loaderClear()
            }
            is RoutePlaceState.Failed -> {
                val error = state.reason
                console.log(error)
                showError(error)
            }
        }
    }
}


fun showPolyline(coordinates: List<Location>) {

    val coorString = coordinates.map {
        "[${it.longitude}, ${it.latitude}]"
    }.toString().replace("[", "")
        .replace("]", "")

    val arrayCoor = js("stringToCoor(coorString)")
    val randomId = Date().toDateString().replace(" ", "-")
    js("zoomPoly(randomId, arrayCoor);")
}

fun removeLayer() {
    js("clearLayers()")
}

fun <T> debounceJob(
    waitMs: Long = 800,
    destinationFunction: (T) -> Unit
): DebounceJob<T> {
    var debounceJob: Job? = null
    val pa = { param: T ->
        debounceJob?.cancel()
        debounceJob = GlobalScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }

    return DebounceJob(pa, debounceJob)
}

data class DebounceJob<T>(
    val param: (T) -> Unit,
    val job: Job?
)