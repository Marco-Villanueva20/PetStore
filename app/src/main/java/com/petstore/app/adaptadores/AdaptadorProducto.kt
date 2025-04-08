package com.petstore.app.adaptadores

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.R
import com.petstore.app.databinding.ItemProductoBinding
import com.petstore.app.modelos.Producto
import com.petstore.app.vendedor.MainActivityVendedor
import com.petstore.app.vendedor.productos.AgregarProductoActivity

class AdaptadorProducto : Adapter<AdaptadorProducto.HolderProducto> {
    private lateinit var binding: ItemProductoBinding

    private var mContext: Context
    private var productos: ArrayList<Producto>

    constructor(productos: ArrayList<Producto>, mContext: Context) {
        this.productos = productos
        this.mContext = mContext
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProducto {
        binding = ItemProductoBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProducto(binding.root)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HolderProducto, position: Int) {
        val modeloProducto = productos[position]

        val nombre = modeloProducto.nombre


        cargarPrimeraImagen(modeloProducto, holder)

        visualizarDescuento(modeloProducto, holder)

        holder.itemNombreP.text = nombre

        holder.ibEditar.setOnClickListener {
            val intent = Intent(mContext, AgregarProductoActivity::class.java)
            intent.putExtra("Edicion", true)
            intent.putExtra("idProducto", modeloProducto.id)
            mContext.startActivity(intent)
        }
        holder.ibEliminar.setOnClickListener {
            val mAlertDialog = MaterialAlertDialogBuilder(mContext)
            mAlertDialog
                .setTitle("Eliminar Producto")
                .setMessage("¿Estas seguro de eliminar el producto?")
                .setPositiveButton("Si") { _, _ ->
                    eliminarProductoBD(modeloProducto)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

    }

    private fun eliminarProductoBD(modeloProducto: Producto) {
        val referencia = FirebaseDatabase.getInstance().getReference("Productos")
        referencia.child(modeloProducto.id)
            .removeValue().addOnSuccessListener {
                val intent = Intent(mContext, MainActivityVendedor::class.java)
                mContext.startActivity(intent)
                Toast.makeText(mContext, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {e->
                Toast.makeText(mContext, "No se pudo eliminar debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }


    @SuppressLint("SetTextI18n")
    private fun visualizarDescuento(modeloProducto: Producto, holder: HolderProducto) {
        if (modeloProducto.precioDescuento != "0" && modeloProducto.notaDescuento != "") {
            //Habilitar las vistas
            holder.itemNotaP.visibility = View.VISIBLE
            holder.itemPrecioPDec.visibility = View.VISIBLE

            //setear la informacion
            holder.itemNotaP.text = modeloProducto.notaDescuento
            holder.itemPrecioPDec.text = "${modeloProducto.precioDescuento}${" PEN"}"
            holder.itemPrecioP.text = "${modeloProducto.precio}${" PEN"}"

            holder.itemPrecioP.paintFlags =
                holder.itemPrecioP.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //tachando el precio original
        } else {
            //El producto no tiene descuento
            //Ocultar las vistas
            holder.itemNotaP.visibility = View.GONE
            holder.itemPrecioPDec.visibility = View.GONE

            //setear la informacion
            holder.itemPrecioP.text = "${modeloProducto.precio}${" PEN"}"
            holder.itemPrecioP.paintFlags =
                holder.itemPrecioP.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() //Quitar el tachado del precio original
        }



    }


    private fun cargarPrimeraImagen(
        modeloProducto: Producto,
        holder: AdaptadorProducto.HolderProducto
    ) {
        val idProducto = modeloProducto.id
        val referencia = FirebaseDatabase.getInstance().getReference("Productos")
        referencia.child(idProducto).child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(mContext).load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(holder.imagenProducto)
                        } catch (e: Exception) {
                            Toast.makeText(
                                mContext,
                                "Error se debe a:${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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


    inner class HolderProducto(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenProducto = binding.imagenP
        var itemNombreP = binding.itemNombreP
        var itemPrecioP = binding.itemPrecioP
        var itemNotaP = binding.itemNotaP
        var itemPrecioPDec = binding.itemPrecioPDesc
        var ibEditar = binding.ibEditar
        var ibEliminar = binding.ibEliminar
    }


}