package com.petstore.app.modelos

class Categoria {
    var id = ""
    var categoria = ""
    var imagen = ""

    constructor()
    constructor(id: String, categoria: String, imagen: String) {
        this.id = id
        this.categoria = categoria
        this.imagen = imagen
    }

    override fun toString(): String {
        return "Categoria(categoria='$categoria', id='$id', imagen='$imagen')"
    }


}