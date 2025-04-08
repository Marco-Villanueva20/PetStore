package com.petstore.app.cliente

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import com.petstore.app.databinding.ActivityLoginTelBinding
import com.petstore.app.utils.Constantes
import java.util.concurrent.TimeUnit

class LoginTelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginTelBinding
    private lateinit var progressDialog: AlertDialog
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Variables para Phone Auth
    private var forceResendingToken: ForceResendingToken? = null
    private lateinit var mCallback: OnVerificationStateChangedCallbacks
    private var mVerificationId: String? = null

    // Variables para el número de teléfono
    private var codigoTel: String = ""  // Ejemplo: "+51"
    private var numeroTel: String = ""  // Ejemplo: "123456789"
    private var codTelNumTel: String = ""  // Ejemplo: "+51123456789"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginTelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProgressDialog()
        phoneLoginCallback()
        setupInitialUI()
        initListeners()

    }

    private fun setupProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Espere por favor")
            .setCancelable(false)
            .create()
    }

    private fun setupInitialUI() {
        // Mostrar el layout de teléfono y ocultar el de verificación
        binding.rlTelefono.visibility = View.VISIBLE
        binding.rlCodigoVer.visibility = View.GONE
    }

    private fun initListeners() {
        binding.btnEnviarCodigo.setOnClickListener {
            validarDatos()
        }
        binding.btnVerificarCodigo.setOnClickListener {
            val otp = binding.etCodVer.text.toString().trim()
            when {
                otp.isEmpty() -> {
                    binding.etCodVer.error = "Ingrese el código de verificación"
                    binding.etCodVer.requestFocus()
                }

                otp.length < 6 -> {
                    binding.etCodVer.error = "El código debe tener 6 dígitos"
                    binding.etCodVer.requestFocus()
                }

                else -> verificarCodigoTel(otp)
            }

        }

        binding.tvReenviarCodigo.setOnClickListener {
            if (forceResendingToken != null) {
                reenviarCodigoVer()
            } else {
                Toast.makeText(this, "No se pudo reenviar el código", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun validarDatos() {

        codigoTel = binding.telCodePicker.selectedCountryCodeWithPlus
        numeroTel = binding.etTelfonoC.text.toString().trim()
        codTelNumTel = codigoTel + numeroTel

        Log.d(TAG, "Número completo: $codTelNumTel")

        if (numeroTel.isEmpty()) {
            binding.etTelfonoC.error = "Ingrese un número de teléfono"
            binding.etTelfonoC.requestFocus()
        } else {
            verificarNumeroTel()
        }
    }

    private fun verificarNumeroTel() {
        progressDialog.setMessage("Enviando código de verificación a $codTelNumTel")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelNumTel)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun reenviarCodigoVer() {
        progressDialog.setMessage("Enviando código de verificación a $codTelNumTel")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelNumTel)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .setForceResendingToken(forceResendingToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verificarCodigoTel(otp: String) {
        progressDialog.setMessage("Verificando código de verificación")
        progressDialog.show()

        if (mVerificationId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error: El código de verificación es nulo", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, otp)
        signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage("Ingresando...")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                progressDialog.dismiss()
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    guardarInformacionUsuario()
                } else {
                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }

            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun guardarInformacionUsuario() {
        progressDialog.setMessage("Guardando Información")
        progressDialog.show()

        val uid = firebaseAuth.uid ?: return
        val tiempoReg = Constantes().obtenerTiempoDispositivo()

        val datos = hashMapOf(
            "uid" to uid,
            "nombres" to "",
            "tRegistro" to codTelNumTel,
            "tiempo_registro" to tiempoReg,
            "imagen" to "",
            "tipoUsuario" to "Cliente"
        )

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid).setValue(datos).addOnSuccessListener {
            progressDialog.dismiss()
            startActivity(Intent(this, MainActivityCliente::class.java))
            finishAffinity()
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Ocurrió un error al guardar la información: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    private fun phoneLoginCallback() {
        mCallback = object : OnVerificationStateChangedCallbacks() {


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.d(TAG, "Verificación completada: $phoneAuthCredential")
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "Verificación fallida", e)

                progressDialog.dismiss()
                Toast.makeText(
                    this@LoginTelActivity,
                    "Fallo en la verificación: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                mVerificationId = verificationId
                forceResendingToken = token

                progressDialog.dismiss()
                binding.rlTelefono.visibility = View.GONE
                binding.rlCodigoVer.visibility = View.VISIBLE

                Toast.makeText(
                    this@LoginTelActivity,
                    "Enviando código de verificación: $codTelNumTel",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    companion object {
        private const val TAG = "LoginTelActivity"
    }
}