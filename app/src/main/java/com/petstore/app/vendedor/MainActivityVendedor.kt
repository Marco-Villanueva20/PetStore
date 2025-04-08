package  com.petstore.app.vendedor

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
import com.petstore.app.databinding.ActivityMainVendedorBinding
import com.petstore.app.vendedor.bottomnavfragmentsvendedor.FragmentMisProductosV
import com.petstore.app.vendedor.bottomnavfragmentsvendedor.FragmentOrdenesV
import com.petstore.app.vendedor.navfragmentsvendedor.FragmentCategoriasV
import com.petstore.app.vendedor.navfragmentsvendedor.FragmentInicioV
import com.petstore.app.vendedor.navfragmentsvendedor.FragmentMiTiendaV
import com.petstore.app.vendedor.navfragmentsvendedor.FragmentReseniasV

class MainActivityVendedor : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setupToolbarAndDrawer(toolbar)

        comprobarSesion()
        setupNavigation()
        replaceFragment(FragmentInicioV())

        binding.navigationView.setCheckedItem(R.id.op_inicio_v)
    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupToolbarAndDrawer(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun cerrarSesion() {
        firebaseAuth.signOut()
        navegarASeleccionarTipoUsuario()
        Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null) {
            navegarASeleccionarTipoUsuario()
        } else {
            Toast.makeText(applicationContext, "Vendedor en Línea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navegarASeleccionarTipoUsuario() {
        startActivity(Intent(this, SeleccionarTipoUsuarioActivity::class.java))
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_v -> {
                replaceFragment(FragmentInicioV())
            }

            R.id.op_categorias_v -> {
                replaceFragment(FragmentCategoriasV())
            }

            R.id.op_mi_tienda_v -> {
                replaceFragment(FragmentMiTiendaV())
            }

            R.id.op_resenia_v -> {
                replaceFragment(FragmentReseniasV())
            }

            R.id.op_cerrar_sesion_v -> {
                cerrarSesion()
            }

            R.id.op_mis_productos_v -> {
                replaceFragment(FragmentMisProductosV())
            }

            R.id.op_mis_ordenes -> {
                replaceFragment(FragmentOrdenesV())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}