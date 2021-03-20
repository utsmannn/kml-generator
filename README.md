# KML GENERATOR
Build with Kotlin-JS

### Live sample
https://utsmannn.github.io/kml-generator/

### Screenshot
![](img/img1.png)

### Feature
- Search location
- Generate and download KML file

### Build with
- Kotlin-JS
- Gradle KTS  
- Mapbox GL JS
- HEREMAP Rest API 
- Kotlin Coroutine
- Ktor
- Ktor Serialization

### How to build
Open terminal and type
```
./gradlew buildOpen
```

### Folder
Task `buildOpen` will generate `distributions` folder in root level of folder project
```
.
├── distributions // generate folder
│   ├── index.css
│   ├── index.html
│   ├── index.js
│   ├── kotlin-js.js
│   └── kotlin-js.js.map
│ 
└── kml-generator // this root project
    ├── README.md
    ├── build
    ├── build.gradle.kts
    ├── gradle
    ├── gradle.properties
    ├── gradlew
    ├── gradlew.bat
    ├── img
    ├── settings.gradle.kts
    ├── src
    └── static.json
```
