import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.serialization.json.JsonBuilder
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import kotlin.coroutines.resume
import kotlin.js.Json


val hereApiKey = "rujdNo1Z-UlY47ipLzcvIkgXpiNSvdYdTxpiDRV-Z6I"

fun main() {
    window.onload = {
        document.head?.headConfig()
        document.body?.sayHello()
    }
}

fun Node.headConfig() {
    append {
        style {
            unsafe {
                raw("""
                                body { margin: 0; padding: 0; }
                               
                                #map {
                                   position: relative;
                                   width: 100vw; height: 100vh;
                                }
                                #form {
                                   position: absolute; z-index:10; margin: 10px;
                                }
                                
                                #card-content {
                                   margin: 10px;
                                }
                                #input-container {
                                    width: 30vw
                                }
                                
                            """.trimIndent())
            }
        }
    }
}

var isInputStartReady = false
var isInputEndReady = false

fun Node.sayHello() {
    var inputIdFocus = ""
    val httpClient = HttpClient(Js) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    val placeRepository: PlaceRepository = PlaceRepositoryImpl(httpClient)

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

                                val search = debounce<String> {
                                    GlobalScope.launch {
                                        console.log("search for --> $it")
                                        val location = getCoordinate()
                                        when (val state = placeRepository.searchPlace(it, location, hereApiKey)) {
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
                                            }
                                            is SearchPlaceState.Failed -> {
                                                val error = state.reason
                                                console.log(error)
                                            }
                                        }
                                    }
                                }


                                onInputFunction = {
                                    val newVal = document.getElementById("inputStart") as? HTMLInputElement
                                    val newString = newVal?.value
                                    if (newString != null && newString.count() > 2) {
                                        search(newString)
                                    }

                                    inputIdFocus = "inputStart"
                                }
                            }
                        }
                        div(classes = "input-field s6") {
                            input(type = InputType.text) {
                                placeholder = "End location"
                                id = "inputEnd"

                                val search = debounce<String> {
                                    GlobalScope.launch {
                                        console.log("search for --> $it")
                                        val location = getCoordinate()
                                        when (val state = placeRepository.searchPlace(it, location, hereApiKey)) {
                                            is SearchPlaceState.Success -> {
                                                val data = state.data

                                                val newVal = document.getElementById("inputEnd") as? HTMLInputElement
                                                val mustVisible = (newVal?.value?.count() ?: 0) > 2

                                                if (mustVisible) {
                                                    createList(data, inputIdFocus)
                                                } else {
                                                    removeList()
                                                }
                                            }
                                            is SearchPlaceState.Failed -> {
                                                val error = state.reason
                                                console.log(error)
                                            }
                                        }
                                    }
                                }

                                onInputFunction = {
                                    val newVal = document.getElementById("inputEnd") as? HTMLInputElement
                                    val newString = newVal?.value
                                    if (newString != null && newString.count() > 2) {
                                        search(newString)
                                    }

                                    inputIdFocus = "inputEnd"
                                }
                            }
                        }

                        button(classes = "btn waves-effect waves-light") {
                            p {
                                text("Test")
                            }
                            onClickFunction = {
                                console.log("start test")
                                val coordinate = jsCoordinate()
                                val latLng = coordinate.split(",").map { it.toDouble() }

                                console.log("coor -> $coordinate")
                                console.log("centering maps...")
                                js("centerMaps(pos);")
                                console.log("centering done...")
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

fun getCoordinate(): Location {
    val coordinate = jsCoordinate()
    val latLng = coordinate.split(",").map { it.toDouble() }
    return Location(latitude = latLng[0], longitude = latLng[1])
}

fun jsCoordinate(): String {
    return js("" +
            "var position = [pos.coords.latitude, pos.coords.longitude];\n" +
            "position.toString();" +
            "") as String
}

fun removeList() {
    val doc = document.getElementById("container-list") as? HTMLDivElement
    doc?.clear()
}

fun createList(newList: List<PlaceResponse>, inputIdFocus: String) {
    val doc = document.getElementById("container-list") as? HTMLDivElement
    console.log(newList)
    doc?.clear()
    doc?.append {
        div(classes = "card") {
            id = "container-2"
            ul(classes = "collection") {
                newList.forEach { item ->
                    a(classes = "collection-item") {
                        href = "#!"
                        onClickFunction = {
                            if (inputIdFocus == "inputStart") {
                                isInputStartReady = true
                            }

                            if (inputIdFocus == "inputEnd") {
                                isInputEndReady = true
                            }

                            val inputFocus = document.getElementById(inputIdFocus) as? HTMLInputElement
                            inputFocus?.value = item.title
                            removeList()
                        }

                        div {
                            p {
                                text(item.title)
                            }

                            p {
                                text(item.address)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun <T> debounce(
    waitMs: Long = 500,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = GlobalScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}