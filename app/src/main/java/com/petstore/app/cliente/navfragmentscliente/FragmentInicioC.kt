package com.petstore.app.cliente.navfragmentscliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.petstore.app.R
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentCarritoC
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentFavoritosC
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentMisOrdenesC
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentTiendaC
import com.petstore.app.databinding.FragmentInicioCBinding


class FragmentInicioC : Fragment() {
    private lateinit var binding: FragmentInicioCBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInicioCBinding.inflate(inflater, container, false)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.op_tienda_c -> {
                    replaceFragment(FragmentTiendaC())
                }
                R.id.op_favoritos_c-> {
                    replaceFragment(FragmentFavoritosC())
                }
                R.id.op_carrito_c -> {
                    replaceFragment(FragmentCarritoC())
                }
                R.id.op_mis_ordenes_c -> {
                    replaceFragment(FragmentMisOrdenesC())
                }
            }
            true
        }
        replaceFragment(FragmentTiendaC())
        binding.bottomNavigation.selectedItemId = R.id.op_tienda_c

        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment, fragment)
            .commit()
    }
}