package com.petstore.app.vendedor

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.petstore.app.databinding.ActivityLoginVendedorBinding

class LoginVendedorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = AlertDialog.Builder(this)
            .setMessage("Espere por favor")
            .setTitle("Espere por favor")
            .create()
        progressDialog.setCanceledOnTouchOutside(false)

        dirigirAlRegistroVendedor()
        loginVendedor()
    }

    private fun loginVendedor() {
        binding.btnLogin.setOnClickListener {
            validarInformacion()
        }
    }

    private fun validarInformacion() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "Ingrese un email"
            binding.etEmail.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Ingrese un email válido"
            binding.etEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.etPassword.error = "Ingrese una contraseña"
            binding.etPassword.requestFocus()
        } else {
            login()
        }
    }

    private fun login() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivityVendedor::class.java))
                finishAffinity()
                Toast.makeText(
                    this,
                    "Bienvenido(a)",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "No se pudo iniciar sesión debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun dirigirAlRegistroVendedor() {
        binding.tvRegistrar.setOnClickListener {
            startActivity(Intent(applicationContext, RegistroVendedorActivity::class.java))
        }
    }
}