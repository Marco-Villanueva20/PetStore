package com.petstore.app.cliente

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.petstore.app.R
import com.petstore.app.SeleccionarTipoUsuarioActivity
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentMisOrdenesC
import com.petstore.app.cliente.bottomnavfragmentscliente.FragmentTiendaC
import com.petstore.app.cliente.navfragmentscliente.FragmentInicioC
import com.petstore.app.cliente.navfragmentscliente.FragmentMiPerfilCliente
import com.petstore.app.databinding.ActivityMainClienteBinding

class MainActivityCliente : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainClienteBinding
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        binding.navigationView.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        binding.drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        replaceFragment(FragmentInicioC())
    }

    private fun comprobarSesion() {
        if (firebaseAuth!!.currentUser == null) {
            startActivity(
                Intent(
                    this@MainActivityCliente,
                    SeleccionarTipoUsuarioActivity::class.java
                )
            )
            finishAffinity()
        } else {
            Toast.makeText(this, "Usuario en Línea", Toast.LENGTH_SHORT).show()
        }
    }
    private fun cerrarSesion() {
        firebaseAuth!!.signOut()
        startActivity(Intent(this@MainActivityCliente, SeleccionarTipoUsuarioActivity::class.java))
        finishAffinity()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.navFragment, fragment).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_c -> {
                replaceFragment(FragmentInicioC())
            }

            R.id.op_mi_perfil_c -> {
                replaceFragment(FragmentMiPerfilCliente())
            }

            R.id.op_cerrar_sesion_c -> {
                cerrarSesion()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
