package com.petstore.app.cliente

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.petstore.app.R
import com.petstore.app.databinding.ActivityLoginClienteBinding
import com.petstore.app.utils.Constantes

class LoginClienteActivity : AppCompatActivity() {
    // View Binding para acceder a las vistas
    private lateinit var binding: ActivityLoginClienteBinding

    // Firebase Auth para el inicio de sesión
    private lateinit var firebaseAuth: FirebaseAuth

    // Progress dialog para mostrar mensajes de carga
    private lateinit var progressDialog: androidx.appcompat.app.AlertDialog

    // Cliente de Google para el login con Google
    private lateinit var googleSignInClient: GoogleSignInClient

    private var email = ""
    private var password = ""

    // Callback para el resultado del login con Google
    private val googleSignInResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta =
                    task.getResult(ApiException::class.java)
                firebaseAuthConGoogle(cuenta.idToken)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al iniciar sesión con Google: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSetup()
        initListeners()
    }

    private fun initSetup() {
        // Inicialización de Firebase y del progress dialog
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Por favor, espera...")
            .setCancelable(false)
            .create()

        // Configuración del login con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegurate de tener este string en strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun initListeners() {
        binding.btnLoginTel.setOnClickListener {
            startActivity(Intent(this@LoginClienteActivity, LoginTelActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            validarInformacion()
        }

        binding.tvRegistrar.setOnClickListener {
            startActivity(Intent(this@LoginClienteActivity, RegistroClienteActivity::class.java))
        }

        //Iniciar sesion con google
        binding.btnLoginGoogle.setOnClickListener {
            googleLogin()
        }
    }

    private fun validarInformacion() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email requerido"
                binding.etEmail.requestFocus()
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email inválido"
                binding.etEmail.requestFocus()
            }

            password.isEmpty() -> {
                binding.etPassword.error = "Contraseña requerida"
            }

            else -> {
                loginCliente()
            }
        }
    }

    private fun googleLogin() {
        val googleSignInIntent = googleSignInClient.signInIntent
        googleSignInResultLauncher.launch(googleSignInIntent)
    }

    private fun loginCliente() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this@LoginClienteActivity, MainActivityCliente::class.java))
                finishAffinity()// Cierra todas las actividades previas
                Toast.makeText(this, "Bienvenido(a)", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al iniciar sesión debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Autenticación en Firebase usando el token de Google
    private fun firebaseAuthConGoogle(idToken: String?) {
        val credenciales = GoogleAuthProvider.getCredential(idToken, null)
        progressDialog.setMessage("Iniciando sesión con Google")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credenciales)
            .addOnSuccessListener { resultadoAuth ->
                progressDialog.dismiss()
                // Si el usuario es nuevo, guardamos su información en la base de datos
                if (resultadoAuth.additionalUserInfo!!.isNewUser) {
                    guardarInformacionUsuarioEnBD()
                    Toast.makeText(this, "Cuenta nueva", Toast.LENGTH_SHORT).show()
                } else {
                    //SI EL USUARIO YA EXISTE
                    Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            this@LoginClienteActivity,
                            MainActivityCliente::class.java
                        )
                    )
                    finishAffinity()
                }
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al iniciar sesión con google debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun guardarInformacionUsuarioEnBD() {
        progressDialog.setMessage("Guardando información...")

        val uid = firebaseAuth.uid ?: return
        val nombreC = firebaseAuth.currentUser?.displayName ?: "Sin nombre"
        val emailC = firebaseAuth.currentUser?.email ?: "Sin email"
        val tiempoRegistro = Constantes().obtenerTiempoDispositivo()

        val datos = HashMap<String, Any>()
        datos["uid"] = uid
        datos["nombreC"] = nombreC
        datos["emailC"] = emailC
        datos["tiempoRegistro"] = tiempoRegistro

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")

        ref.child(uid).setValue(datos)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this@LoginClienteActivity, MainActivityCliente::class.java))
                finishAffinity()
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al guardar la información debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
