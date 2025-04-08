package com.petstore.app.cliente.bottomnavfragmentscliente

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.adaptadores.AdaptadorProductoC
import com.petstore.app.databinding.FragmentFavoritosCBinding
import com.petstore.app.modelos.Producto

class FragmentFavoritosC : Fragment() {
    private lateinit var binding: FragmentFavoritosCBinding

    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productos : ArrayList<Producto>
    private lateinit var productosAdaptador: AdaptadorProductoC

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFavoritosCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        cargarProductosFavoritos()

    }

    private fun cargarProductosFavoritos() {
        productos = ArrayList()
        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(firebaseAuth.currentUser!!.uid).child("Favoritos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productos.clear()
                    for (ds in snapshot.children) {
                        val idProducto = "" + ds.child("idProducto").value

                        val referenciaProducto = FirebaseDatabase.getInstance().getReference("Productos")
                            .child(idProducto).addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val producto = snapshot.getValue(Producto::class.java)
                                        productos.add(producto!!)
                                    }catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        Handler().postDelayed({
            productosAdaptador = AdaptadorProductoC(productos, mContext)
            binding.rvFavoritos.adapter = productosAdaptador
        }, 500)
    }

}