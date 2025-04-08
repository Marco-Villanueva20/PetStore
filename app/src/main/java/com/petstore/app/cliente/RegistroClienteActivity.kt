package com.petstore.app.cliente

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.petstore.app.databinding.ActivityRegistroClienteBinding
import com.petstore.app.utils.Constantes

class RegistroClienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroClienteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupProgressDialog()
        binding.btnRegistrar.setOnClickListener {
            validarInformacion()
        }
    }

    private var nombres = ""
    private var email = ""
    private var password = ""
    private var confirmarPassword = ""

    private fun validarInformacion() {
        nombres = binding.etNombres.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        confirmarPassword = binding.etConfirmarPassword.text.toString().trim()

        if (nombres.isEmpty()) {
            binding.etNombres.error = "Ingrese sus nombres"
            binding.etNombres.requestFocus()
        } else if (email.isEmpty()) {
            binding.etEmail.error = "Ingrese un email"
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
        } else if (confirmarPassword.isEmpty()) {
            binding.etConfirmarPassword.error = "Ingrese una contraseña"
            binding.etConfirmarPassword.requestFocus()
        } else if (password != confirmarPassword) {
            binding.etConfirmarPassword.error = "Las contraseñas no coinciden"
            binding.etConfirmarPassword.requestFocus()
        } else {
            registrarCliente()
        }
    }

    private fun registrarCliente() {
        progressDialog.setMessage("Creando Cuenta")
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            insertarInformacionClienteBD()
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "No se pudo crear la cuenta debido a ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun insertarInformacionClienteBD() {
        progressDialog.setMessage("Guardando Información")

        val uid = firebaseAuth.uid
        val nombresBD = nombres
        val emailBD = email
        val tipoUsuarioBD = "Cliente"
        val tiempoRegistro = Constantes().obtenerTiempoDispositivo()

        val datosCliente = HashMap<String, Any>()

        datosCliente["uid"] = "$uid"
        datosCliente["nombres"] = nombresBD
        datosCliente["email"] = emailBD
        datosCliente["tipoUsuario"] = tipoUsuarioBD
        datosCliente["imagen"] = ""
        datosCliente["tiempo_registro"] = "$tiempoRegistro"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid!!)
            .setValue(datosCliente)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this@RegistroClienteActivity, MainActivityCliente::class.java))
                Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo crear la cuenta debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun setupProgressDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Espere por favor")
        progressDialog = builder.create()
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)
    }
}