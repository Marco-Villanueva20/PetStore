package com.petstore.app.detalleproducto

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.adaptadores.AdaptadorImagenSlider
import com.petstore.app.databinding.ActivityDetalleProductoBinding
import com.petstore.app.modelos.ImagenSlider
import com.petstore.app.modelos.Producto

class DetalleProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleProductoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idProductoBinding = ""

    private lateinit var imagenSliderArrayList: ArrayList<ImagenSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //Obtenenmos el id del producto envaido desde el adaptador
        idProductoBinding = intent.getStringExtra("idProducto").toString()


        cargarImagenesProd()
        cargarInfoProducto()

    }

    private fun cargarInfoProducto() {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProductoBinding).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modeloProducto = snapshot.getValue(Producto::class.java)
                val nombre = modeloProducto!!.nombre
                val descripcion = modeloProducto.descripcion
                val precio = modeloProducto.precio
                val precioDescuento = modeloProducto.precioDescuento
                val notaDescuento = modeloProducto.notaDescuento

                binding.nombrePD.text = nombre
                binding.descripcionPD.text = descripcion
                binding.precioPD.text = precio.plus(" PEN")

                if (precioDescuento != "" && notaDescuento != "") {
                    /*Producto con descuento*/
                    binding.precioDescPD.text = precioDescuento
                    binding.notaDescPD.text = notaDescuento

                    binding.precioPD.paintFlags = binding.precioPD.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    /*Producto sin descuento*/
                    binding.precioDescPD.visibility = View.GONE
                    binding.notaDescPD.visibility = View.GONE

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun cargarImagenesProd() {
        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")

        ref.child(idProductoBinding).child("Imagenes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modeloImagenSlider = ds.getValue(ImagenSlider::class.java)
                            imagenSliderArrayList.add(modeloImagenSlider!!)
                        } catch (e: Exception) {
                            print(e.message)
                        }
                    }
                    val adaptadorImagenSlider =
                        AdaptadorImagenSlider(this@DetalleProductoActivity, imagenSliderArrayList)
                    binding.vpImagen.adapter = adaptadorImagenSlider
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}