package com.petstore.app.modelos

class ImagenSlider {
    var id: String = ""
    var imagenUrl: String = ""

    constructor()
    constructor(id: String, imagenUrl: String) {
        this.id = id
        this.imagenUrl = imagenUrl
    }
}