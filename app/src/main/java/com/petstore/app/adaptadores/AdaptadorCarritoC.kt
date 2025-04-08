package com.petstore.app.adaptadores

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.R
import com.petstore.app.databinding.ItemCarritoCBinding
import com.petstore.app.modelos.ProductoCarrito

class AdaptadorCarritoC : RecyclerView.Adapter<AdaptadorCarritoC.HolderProductoCarrito> {

    private lateinit var binding: ItemCarritoCBinding
    private var mContexto: Context
    var productos = ArrayList<ProductoCarrito>()
    private var firebaseAuth: FirebaseAuth


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoCarrito {
        binding = ItemCarritoCBinding.inflate(LayoutInflater.from(mContexto), parent, false)
        return HolderProductoCarrito(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HolderProductoCarrito, position: Int) {
        val productoCarrito = productos[position]

        val nombre = productoCarrito.nombre
        var cantidad = productoCarrito.cantidad
        val precioFinal = productoCarrito.precioFinal
        val precio = productoCarrito.precio
        val precioDesc = productoCarrito.precioDescuento
        println("holaaaaaaaaaaa$precioDesc")

        holder.nombrePCar.text = nombre
        holder.cantidadPCar.text = cantidad.toString()

        cargarPrimeraImg(productoCarrito, holder)
        visualizarDescuento(productoCarrito, holder)

        holder.btnEliminar.setOnClickListener {
            eliminarProdCarrito(mContexto, productoCarrito.idProducto)
        }

        var miPrecioFinalDouble: Double

        holder.btnAumentar.setOnClickListener {
            // Obtener el precio de descuento ya sin espacios
            val discountStr = precioDesc.trim()
            // Determinar el precio unitario
            val unitCost = if (precioDesc != "0" && precioDesc.isNotEmpty()) {
                discountStr.toDoubleOrNull() ?: 0.0
            } else {
                precio.trim().toDoubleOrNull() ?: 0.0
            }

            // Incrementar cantidad
            cantidad++

            // Recalcular el precio final basado en la cantidad
            miPrecioFinalDouble = unitCost * cantidad

            // Actualizar las vistas
            holder.cantidadPCar.text = cantidad.toString()
            holder.precioFinalPCar.text = miPrecioFinalDouble.toString()

            // Actualizar en Firebase
            val precioFinalString = miPrecioFinalDouble.toString()
            calcularPrecio(mContexto, productoCarrito.idProducto, precioFinalString, cantidad)
        }

        holder.btnDisminuir.setOnClickListener {
            if (cantidad > 1) {
                // Obtener el precio de descuento ya sin espacios
                val discountStr = precioDesc.trim()
                // Determinar el precio unitario
                val unitCost = if (precioDesc != "0" && precioDesc.isNotEmpty()) {
                    discountStr.toDoubleOrNull() ?: 0.0
                } else {
                    precio.trim().toDoubleOrNull() ?: 0.0
                }

                // Disminuir cantidad
                cantidad--

                // Recalcular el precio final basado en la nueva cantidad
                miPrecioFinalDouble = unitCost * cantidad

                // Actualizar las vistas
                holder.cantidadPCar.text = cantidad.toString()
                holder.precioFinalPCar.text = miPrecioFinalDouble.toString()

                // Actualizar en Firebase
                val precioFinalString = miPrecioFinalDouble.toString()
                calcularPrecio(mContexto, productoCarrito.idProducto, precioFinalString, cantidad)
            }
        }

    }

    private fun calcularPrecio(
        mContexto: Context,
        idProducto: String,
        precioFinalString: String,
        cantidad: Int
    ) {
        val hashMap = HashMap<String, Any>()

        hashMap["cantidad"] = cantidad
        hashMap["precioFinal"] = precioFinalString

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras").child(idProducto)
            .updateChildren(hashMap).addOnSuccessListener {

                Toast.makeText(mContexto, "Precio actualizado", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {

                Toast.makeText(
                    mContexto,
                    "Error al actualizar precio debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    private fun eliminarProdCarrito(mContexto: Context, idProducto: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras").child(idProducto)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContexto, "Producto eliminado del carrito", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(
                    mContexto,
                    "Error al eliminar producto debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    @SuppressLint("SetTextI18n")
    private fun visualizarDescuento(
        productoCarrito: ProductoCarrito,
        holder: AdaptadorCarritoC.HolderProductoCarrito
    ) {
        if (productoCarrito.precioDescuento != "0") {
            holder.precioFinalPCar.text = productoCarrito.precioFinal.plus(" PEN")
            holder.precioOriginalPCar.text = productoCarrito.precio.plus(" PEN")
            holder.precioOriginalPCar.paintFlags =
                holder.precioOriginalPCar.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.precioOriginalPCar.visibility = View.GONE
            holder.precioFinalPCar.text = productoCarrito.precioFinal.plus(" PEN")
        }
    }

    private fun cargarPrimeraImg(
        productoCarrito: ProductoCarrito,
        holder: AdaptadorCarritoC.HolderProductoCarrito
    ) {
        val idProducto = productoCarrito.idProducto
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(mContexto)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(holder.imagenPCar)
                        } catch (e: Exception) {
                            print("Error: $e")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    override fun getItemCount(): Int {
        return productos.size
    }


    constructor(
        mContexto: Context,
        productos: ArrayList<ProductoCarrito>
    ) : super() {
        this.mContexto = mContexto
        this.productos = productos
        this.firebaseAuth = FirebaseAuth.getInstance()
    }


    inner class HolderProductoCarrito(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenPCar = binding.imagenPCar
        var nombrePCar = binding.nombrePCar
        var precioFinalPCar = binding.precioFinalPCar
        var precioOriginalPCar = binding.precioOriginalPCar

        var btnDisminuir = binding.btnDisminuir
        var cantidadPCar = binding.cantidadPCar
        var btnAumentar = binding.btnAumentar

        var btnEliminar = binding.btnEliminar

    }


}