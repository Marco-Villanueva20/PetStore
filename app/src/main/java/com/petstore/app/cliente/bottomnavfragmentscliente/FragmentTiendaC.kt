package com.petstore.app.cliente.bottomnavfragmentscliente

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.adaptadores.AdaptadorCategoriaC
import com.petstore.app.databinding.FragmentTiendaCBinding
import com.petstore.app.modelos.Categoria


class FragmentTiendaC : Fragment() {

    private lateinit var binding: FragmentTiendaCBinding
    private lateinit var mContext: Context

    private lateinit var categorias: ArrayList<Categoria>
    private lateinit var adaptadorCategoria: AdaptadorCategoriaC

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentTiendaCBinding.inflate(LayoutInflater.from(mContext), container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listarCategorias()
    }

    private fun listarCategorias() {
        categorias = ArrayList()

        val reference = FirebaseDatabase.getInstance().getReference("Categorias")
            .orderByChild("categoria")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categorias.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(Categoria::class.java)
                    categorias.add(modelo!!)
                }
                println(categorias[0].toString())
                adaptadorCategoria = AdaptadorCategoriaC(mContext, categorias)
                binding.rvCategorias.adapter = adaptadorCategoria
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}