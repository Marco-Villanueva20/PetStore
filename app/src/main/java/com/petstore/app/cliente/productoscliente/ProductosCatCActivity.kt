package com.petstore.app.cliente.productoscliente

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.adaptadores.AdaptadorProductoC
import com.petstore.app.databinding.ActivityProductosCatCBinding
import com.petstore.app.modelos.Producto

class ProductosCatCActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductosCatCBinding

    private lateinit var adaptadorProducto: AdaptadorProductoC
    private lateinit var productos: ArrayList<Producto>

    private var nombreCategoria: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductosCatCBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Obtener el nombre de la categoria
        val intent = intent
        nombreCategoria = intent.getStringExtra("categoria").toString()
        listarProductos(nombreCategoria)

        binding.etBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(filtro: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consulta = filtro.toString()
                    adaptadorProducto.filter.filter(consulta)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.ibLimpiarCampo.setOnClickListener {
            val consulta = binding.etBuscarProducto.text.toString().trim()
            if (consulta.isNotEmpty()) {
                binding.etBuscarProducto.text.clear()
                Toast.makeText(this, "Campo limpio", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No has ingresado nada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listarProductos(nombreCategoria: String) {
        productos = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
            .orderByChild("categoria").equalTo(nombreCategoria)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productos.clear()
                    for (ds in snapshot.children) {
                        val modeloProducto = ds.getValue(Producto::class.java)
                        if (modeloProducto != null) {
                            productos.add(modeloProducto)
                        }
                        adaptadorProducto =
                            AdaptadorProductoC(productos, this@ProductosCatCActivity)
                        binding.rvProductos.adapter = adaptadorProducto
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}