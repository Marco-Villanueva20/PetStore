package com.petstore.app.modelos

class Producto {
    var id: String = ""
    var nombre: String = ""
    var descripcion: String = ""
    var categoria: String = ""
    var precio: String = ""
    var precioDescuento: String = ""
    var notaDescuento: String = ""
    var favorito = false

    constructor()
    constructor(
        categoria: String,
        descripcion: String,
        id: String,
        nombre: String,
        notaDescuento: String,
        precio: String,
        precioDescuento: String,
        favorito: Boolean
    ) {
        this.categoria = categoria
        this.descripcion = descripcion
        this.id = id
        this.nombre = nombre
        this.notaDescuento = notaDescuento
        this.precio = precio
        this.precioDescuento = precioDescuento
        this.favorito = favorito

    }

}