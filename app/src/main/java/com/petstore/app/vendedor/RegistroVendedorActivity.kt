package com.petstore.app.vendedor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.petstore.app.databinding.ActivityRegistroVendedorBinding
import com.petstore.app.utils.Constantes


class RegistroVendedorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProgressDialog()


        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegistrar.setOnClickListener {
            validarInformacion()
        }
    }

    private fun setupProgressDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Espere por favor")
        progressDialog = builder.create()
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCancelable(false)
    }

    private var nombres = ""
    private var email = ""
    private var password = ""
    private var confirmarPasswordV = ""

    private fun validarInformacion() {
        nombres = binding.etNombres.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        confirmarPasswordV = binding.etConfirmarPassword.text.toString().trim()

        if (nombres.isEmpty()) {
            binding.etNombres.error = "Ingrese sus nombres"
            binding.etNombres.requestFocus()
        } else if (email.isEmpty()) {
            binding.etEmail.error = "Ingrese un email válido"
            binding.etEmail.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Ingrese un email válido"
            binding.etEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.etPassword.error = "Ingrese una contraseña"
            binding.etPassword.requestFocus()
        } else if (password.length < 6) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            binding.etPassword.requestFocus()
        } else if (confirmarPasswordV.isEmpty()) {
            binding.etConfirmarPassword.error = "Ingrese una contraseña"
            binding.etConfirmarPassword.requestFocus()
        } else if (password != confirmarPasswordV) {
            binding.etConfirmarPassword.error = "Las contraseñas no coinciden"
            binding.etConfirmarPassword.requestFocus()
        } else {
            registrarVendedor()
        }
    }

    private fun registrarVendedor() {
        if (!progressDialog.isShowing){
            progressDialog.setMessage("Creando cuenta")
            progressDialog.show()
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            insertarInformacionVendedorBD()
        }.addOnFailureListener {e->
            if (progressDialog.isShowing){ progressDialog.dismiss()}
            Toast.makeText(this, "No se pudo crear la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertarInformacionVendedorBD() {
        progressDialog.dismiss()
        val uidBD = firebaseAuth.uid
        val nombresBD = nombres
        val emailBD = email
        val tipoUsuarioBD = "Vendedor"
        val tiempoBD = Constantes().obtenerTiempoDispositivo()
        val datosVendedor = HashMap<String, Any>()

        datosVendedor["uid"] = "$uidBD"
        datosVendedor["nombres"] = nombresBD
        datosVendedor["email"] = emailBD
        datosVendedor["tipoUsuario"] = tipoUsuarioBD
        datosVendedor["tiempo_registro"] = "$tiempoBD"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidBD!!)
            .setValue(datosVendedor)
            .addOnSuccessListener {
                if (progressDialog.isShowing) progressDialog.dismiss()
                startActivity(Intent(this, MainActivityVendedor::class.java))
                finish()
            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo crear la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }