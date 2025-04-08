package com.petstore.app.vendedor.navfragmentsvendedor

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.petstore.app.R
import com.petstore.app.adaptadores.AdaptadorCategoriaV
import com.petstore.app.databinding.FragmentCategoriasVBinding
import com.petstore.app.modelos.Categoria

class FragmentCategoriasV : Fragment() {

    private lateinit var binding: FragmentCategoriasVBinding
    private lateinit var mContext: Context
    private lateinit var progressDialog: AlertDialog
    private lateinit var adaptadorCategoriaV: AdaptadorCategoriaV
    private var listaCategorias = ArrayList<Categoria>()

    private var imagenUri: Uri? = null
    private var categoria = ""

    // Se utiliza onAttach para obtener el contexto y evitar llamar a requireContext() repetidamente
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriasVBinding.inflate(inflater, container, false)
        setupProgressDialog()
        cargarCategorias()
        setupRecyclerView()
        setupListeners()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvCategorias.layoutManager = LinearLayoutManager(mContext)
        adaptadorCategoriaV = AdaptadorCategoriaV(mContext, listaCategorias)
        binding.rvCategorias.adapter = adaptadorCategoriaV
    }

    /**
     * Configura los listeners de los controles de la UI
     */
    private fun setupListeners() {
        binding.imgCategorias.setOnClickListener {
            seleccionarImagen()
        }
        binding.btnAgregarCategoria.setOnClickListener {
            validarInformacion()
        }
    }

    /**
     * Configura el diálogo de progreso
     */
    private fun setupProgressDialog() {
        progressDialog = AlertDialog.Builder(context)
            .setTitle("Cargando")//opcional
            .setMessage("Espere por favor")
            .setCancelable(false)
            .create()
    }

    /**
     * Lanza el ImagePicker para seleccionar y recortar una imagen
     */
    private fun seleccionarImagen() {
        ImagePicker.with(requireActivity())
            .crop().compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                resultadoImg.launch(intent)
            }
    }

    /**
     * Resultado del ImagePicker
     */
    private val resultadoImg =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                resultado.data?.data?.let { uri ->
                    imagenUri = uri
                    binding.imgCategorias.setImageURI(imagenUri)
                }
            } else {
                Toast.makeText(context, "No selecciono ninguna imagen", Toast.LENGTH_SHORT).show()
            }

        }

    /**
     * Valida la información ingresada en la UI antes de agregar la categoría
     */
    private fun validarInformacion() {
        categoria = binding.etCategoria.text.toString().trim()
        when {
            categoria.isEmpty() -> binding.etCategoria.error = "Ingrese una categoría"
            imagenUri == null -> Toast.makeText(
                requireContext(),
                "Seleccione una imagen",
                Toast.LENGTH_SHORT
            ).show()

            else -> agregarCategoriaBD()
        }
    }

    /**
     * Agrega la categoría a la base de datos
     */
    private fun agregarCategoriaBD() {
        progressDialog.setMessage("Agregando Categoría")
        progressDialog.show()

        val reference = FirebaseDatabase.getInstance().getReference("Categorias")
        val keyId = reference.push().key
        if (keyId == null) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Error al generar ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Datos básicos de la categoría
        val datosCategoria: HashMap<String, Any> = hashMapOf(
            "id" to keyId,
            "categoria" to categoria
        )

        reference.child(keyId)
            .setValue(datosCategoria)
            .addOnSuccessListener {
                // Una vez agregada la categoría, se sube la imagen
                subirImagenStorage(keyId)
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    context,
                    "No se pudo agregar la categoría debido a ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Sube la imagen a Firebase Storage y actualiza la categoría en la base de datos con la URL de la imagen
     */
    private fun subirImagenStorage(keyId: String) {
        progressDialog.setMessage("Subiendo Imagen")
        progressDialog.show()

        val carpetaImagen = "Categorias/$keyId"
        val storageReference = FirebaseStorage.getInstance().getReference(carpetaImagen)

        imagenUri?.let { uri ->
            storageReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Una vez subida, obtenemos la URL de descarga
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { urlImagen ->
                        actualizarCategoriaConImagen(keyId, urlImagen.toString())
                    }.addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            context,
                            "No se pudo agregar la categoría debido a ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun actualizarCategoriaConImagen(keyId: String, urlImagen: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Categorias")
        val hashMap: HashMap<String, Any> = hashMapOf("imagen" to urlImagen)

        reference.child(keyId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(mContext, "Categoría agregada con éxito", Toast.LENGTH_SHORT).show()

                limpiarControles()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar categoría: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun limpiarControles() {
        binding.etCategoria.text.clear()
        imagenUri = null
        binding.imgCategorias.setImageURI(null)
        binding.imgCategorias.setImageResource(R.drawable.categoria)
    }


    private fun cargarCategorias() {
        val reference = FirebaseDatabase.getInstance().getReference("Categorias")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val categoria = snapshot.getValue(Categoria::class.java)
                categoria?.let {
                    listaCategorias.add(it)
                    adaptadorCategoriaV.notifyItemInserted(listaCategorias.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val categoriaActualizada = snapshot.getValue(Categoria::class.java)
                categoriaActualizada?.let {
                    val index = listaCategorias.indexOfFirst { cat -> cat.id == it.id }
                    if (index != -1) {
                        listaCategorias[index] = it
                        adaptadorCategoriaV.notifyItemChanged(index)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val categoriaEliminada = snapshot.getValue(Categoria::class.java)
                categoriaEliminada?.let {
                    val index = listaCategorias.indexOfFirst { cat -> cat.id == it.id }
                    if (index != -1) {
                        listaCategorias.removeAt(index)
                        adaptadorCategoriaV.notifyItemRemoved(index)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // No se necesita acción en este caso
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    mContext,
                    "Error al cargar categorías: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}