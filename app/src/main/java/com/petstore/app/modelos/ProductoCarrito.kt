package com.petstore.app.modelos

class ProductoCarrito {
    var idProducto: String = ""
    var nombre: String = ""
    var precio: String = ""
    var precioFinal: String = ""
    var precioDescuento: String = ""
    var cantidad: Int = 0

    constructor()
    constructor(
        idProducto: String,
        nombre: String,
        precio: String,
        precioDescuento: String,
        precioFinal: String,
        cantidad: Int
    ) {
        this.idProducto = idProducto
        this.nombre = nombre
        this.precio = precio
        this.precioDescuento = precioDescuento
        this.precioFinal = precioFinal
        this.cantidad = cantidad
    }


}