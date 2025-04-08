package  com.petstore.app.vendedor.navfragmentsvendedor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.petstore.app.R
import com.petstore.app.databinding.FragmentInicioVBinding
import com.petstore.app.vendedor.bottomnavfragmentsvendedor.FragmentMisProductosV
import com.petstore.app.vendedor.bottomnavfragmentsvendedor.FragmentOrdenesV
import com.petstore.app.vendedor.productos.AgregarProductoActivity


class FragmentInicioV : Fragment() {

    private lateinit var binding: FragmentInicioVBinding
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioVBinding.inflate(inflater, container, false)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.op_mis_productos_v -> {
                    replaceFragment(FragmentMisProductosV())
                }

                R.id.op_mis_ordenes -> {
                    replaceFragment(FragmentOrdenesV())
                }

            }
            true
        }

        replaceFragment(FragmentMisProductosV())
        binding.bottomNavigation.selectedItemId = R.id.op_mis_productos_v

        binding.addFab.setOnClickListener {
           val intent = Intent(mContext, AgregarProductoActivity::class.java)
            intent.putExtra("Edicion", false)
            startActivity(intent)
        }
        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment, fragment)
            .commit()
    }

}