package  com.petstore.app.vendedor.bottomnavfragmentsvendedor

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
import com.petstore.app.adaptadores.AdaptadorProducto
import com.petstore.app.databinding.FragmentMisProductosVBinding
import com.petstore.app.modelos.Producto


class FragmentMisProductosV : Fragment() {
    private lateinit var binding: FragmentMisProductosVBinding
    private lateinit var mContext: Context

    private lateinit var productos: ArrayList<Producto>
    private lateinit var adaptadorProductos: AdaptadorProducto

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentMisProductosVBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listarProductos()
    }

    private fun listarProductos() {
        productos = ArrayList()

        val reference = FirebaseDatabase.getInstance().getReference("Productos")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productos.clear()
                for (ds in snapshot.children) {
                    val modeloProducto = ds.getValue(Producto::class.java)
                    if (modeloProducto != null) {
                        productos.add(modeloProducto)
                    }
                }
                adaptadorProductos = AdaptadorProducto(productos, mContext)
                binding.rvProductos.adapter = adaptadorProductos

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
}