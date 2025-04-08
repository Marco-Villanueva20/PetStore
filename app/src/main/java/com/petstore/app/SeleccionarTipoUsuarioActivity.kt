package com.petstore.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.cliente.LoginClienteActivity
import com.petstore.app.cliente.MainActivityCliente
import com.petstore.app.databinding.ActivitySeleccionarTipoUsuarioBinding
import com.petstore.app.vendedor.LoginVendedorActivity
import com.petstore.app.vendedor.MainActivityVendedor

class SeleccionarTipoUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeleccionarTipoUsuarioBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instancia FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Verifica si el usuario ya está autenticado ANTES de mostrar la UI
        comprobarTipoUsuario()

    }

    private fun comprobarTipoUsuario() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null) {
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tipoUsuario = snapshot.child("tipoUsuario").value

                        val intent = when (tipoUsuario) {
                            "Vendedor" -> Intent(
                                applicationContext,
                                MainActivityVendedor::class.java
                            )
                            "Cliente" -> Intent(
                                applicationContext,
                                MainActivityCliente::class.java
                            )
                            else -> null
                        }
                        if (intent != null) {
                            startActivity(intent)
                            finishAffinity() // Cierra todas las actividades previas
                        } else {
                            // Si no hay tipo de usuario definido, mostramos la UI
                            mostrarPantallaSeleccion()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SeleccionarTipoUsuarioActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        mostrarPantallaSeleccion()
                    }
                })
        }else{
            // Si no hay usuario autenticado, mostrar UI
            mostrarPantallaSeleccion()
        }
    }

    private fun mostrarPantallaSeleccion() {
        // Ahora sí inflamos el layout
        binding = ActivitySeleccionarTipoUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tipoVendedor.setOnClickListener {
            val intent = Intent(this, LoginVendedorActivity::class.java)
            startActivity(intent)
        }
        binding.tipoCliente.setOnClickListener {
            val intent = Intent(this, LoginClienteActivity::class.java)
            startActivity(intent)
        }
    }
}

