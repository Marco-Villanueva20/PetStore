package com.petstore.app.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Constantes {
    fun obtenerTiempoDispositivo(): Long {
        return System.currentTimeMillis()
    }

    fun agregarProductoFav(context: Context, idProducto: String) {
        var firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = Constantes().obtenerTiempoDispositivo()

        val hasMap = HashMap<String, Any>()

        hasMap["idProducto"] = idProducto
        hasMap["idFav"] = tiempo

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference
            .child(firebaseAuth.uid!!)
            .child("Favoritos")
            .child(idProducto)
            .setValue(hasMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Producto agregado a favoritos", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error al agregar a favoritos debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    fun eliminarProductoFav(context: Context, idProducto: String) {
        var firebaseAuth = FirebaseAuth.getInstance()

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference
            .child(firebaseAuth.uid!!)
            .child("Favoritos")
            .child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Producto eliminado de favoritos", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error al eliminar de favoritos debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}