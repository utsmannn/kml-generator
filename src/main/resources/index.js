var pos;
const layerId = 'layer-id';

const mapboxApiKey = 'pk.eyJ1Ijoia3VjaW5nYXBlcyIsImEiOiJjazk4eXFpbXgwNm8zM2tuMnhibXk4YjRwIn0.HMAEerCtOfeIEL6mU5iP_w';
mapboxgl.accessToken = mapboxApiKey;

let map;

function initMap() {
    map = new mapboxgl.Map({
        container: 'map',
        style: 'mapbox://styles/mapbox/streets-v11',
        center: [106.816666, -6.200000],
        zoom: 14
    });
}

function requestLocation() {
    if (navigator.geolocation) {
        console.log(isMobile)
        navigator.geolocation.getCurrentPosition(getPosition);
    } else {
        alert("Oops! This browser does not support HTML Geolocation.");
    }
}

function getPosition(position) {
    pos = position;
    centerMaps(position);
}

function setupIsMobile() {
    if (isMobile) {
        console.log("is mobile")
    } else {
        console.log("is not mobile")
    }

    return isMobile
}

function centerMaps(position) {
    var lat = position.coords.latitude;
    var lon = position.coords.longitude;
    map.flyTo({
        center: [lon, lat],
        essential: true
    });
}

function stringToCoor(rawString) {
    var string = rawString.split(',');

    var a = string.length;
    for (i = 0; i < a; i++) {
        string[i] = parseFloat(string[i]);
    }

    var b = string.length / 2;
    var array = [];
    for (i = 0; i < b; i++) {
        array[i] = [0, 0];
    }

    var k = 0;
    for (i = 0; i < b; i++) {
        for (j = 0; j < 2; j++) {
            array[i][j] = string[k];
            k++;
        }
    }
    return array
}

function clearLayers() {
    if (map.getLayer(layerId)) {
        map.removeLayer(layerId);
    }
    if (map.getSource(layerId)) {
        map.removeSource(layerId);
    }
}

function zoomPoly(lineId, coorString) {
    var geojson = {
        'type': 'FeatureCollection',
        'features': [
            {
                'type': 'Feature',
                'geometry': {
                    'type': 'LineString',
                    'properties': {},
                    'coordinates': coorString
                }
            }
        ]
    };

    map.addSource(layerId, {
        'type': 'geojson',
        'data': geojson
    });
    map.addLayer({
        'id': layerId,
        'type': 'line',
        'source': layerId,
        'layout': {
            'line-join': 'round',
            'line-cap': 'round'
        },
        'paint': {
            'line-color': '#eb4034',
            'line-width': 5
        }
    });

    var coordinates = geojson.features[0].geometry.coordinates;

    var bounds = coordinates.reduce(function (bounds, coord) {
        return bounds.extend(coord);
    }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));

    map.fitBounds(bounds, {
        padding: 220
    });
}

function download(filename, text) {
    var pom = document.createElement('a');
    pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    pom.setAttribute('download', filename);

    if (document.createEvent) {
        var event = document.createEvent('MouseEvents');
        event.initEvent('click', true, true);
        pom.dispatchEvent(event);
    } else {
        pom.click();
    }
}