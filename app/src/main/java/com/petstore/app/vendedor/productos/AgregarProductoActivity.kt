package com.petstore.app.vendedor.productos

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.petstore.app.adaptadores.AdaptadorImagenSeleccionada
import com.petstore.app.databinding.ActivityAgregarProductoBinding
import com.petstore.app.modelos.Categoria
import com.petstore.app.modelos.ImagenSeleccionada
import com.petstore.app.utils.Constantes
import com.petstore.app.vendedor.MainActivityVendedor

class AgregarProductoActivity : AppCompatActivity() {

    // Vistas y adaptador
    private lateinit var binding: ActivityAgregarProductoBinding
    private lateinit var adaptadorImagenSeleccionada: AdaptadorImagenSeleccionada
    private lateinit var progressDialog: AlertDialog

    // Datos locales
    private val imagenSeleccionada = arrayListOf<ImagenSeleccionada>()
    private val categorias = arrayListOf<Categoria>()
    private var imagenUri: Uri? = null

    private var idCat = ""
    private var tituloCat = ""

    private var edicion = false
    private var idProducto = ""

    private var nombreP = ""
    private var descripcionP = ""
    private var categoriaP = ""
    private var precioP = ""
    private var descuentoHabilitado = false
    private var precioDescuentoP = ""
    private var notaDescuentoP = ""
    private var porcentajeDescP = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        edicion = intent.getBooleanExtra("Edicion", false)

        if (edicion) {
            idProducto = intent.getStringExtra("idProducto")!!
            binding.txtAgregarProductos.text = "Editar Producto"
            cargarInformacion()
        } else {
            binding.txtAgregarProductos.text = "Agregar Producto"
        }
        setupUI()
        cargarCategorias()
        setupProgressDialog()
        setupRecyclerView()
        setupListeners()

    }

    private fun cargarInformacion() {
        val reference = FirebaseDatabase.getInstance().getReference("Productos")
        reference.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoria = snapshot.child("categoria").value.toString()
                val descripcion = snapshot.child("descripcion").value.toString()
                val nombre = snapshot.child("nombre").value.toString()
                val notaDescuento = snapshot.child("notaDescuento").value.toString()
                val precio = snapshot.child("precio").value.toString()
                val precioDescuento = snapshot.child("precioDescuento").value.toString()

                if (notaDescuento != "" && precioDescuento != "0") {
                    binding.descuentoSwitch.isChecked = true
                }

                binding.etNombresP.setText(nombre)
                binding.etDescripcionP.setText(descripcion)
                binding.categoria.text = categoria
                binding.etPrecioP.setText(precio)
                binding.etPrecioConDescuentoP.text = precioDescuento
                binding.etNotaDescuentoP.setText(notaDescuento)

                val refImagenes = snapshot.child("Imagenes").ref
                refImagenes.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            val id = ds.child("id").value.toString()
                            val imagenUrl = ds.child("imagenUrl").value.toString()
                            val imagen = ImagenSeleccionada(
                                id = id,
                                imagenUri = null,
                                imagenUrl = imagenUrl,
                                deInternet = true
                            )
                            imagenSeleccionada.add(imagen)
                        }
                        cargarImagen()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    /**
     * Configura la visibilidad de las vistas y el comportamiento del switch de descuento.
     */
    private fun setupUI() {

        // Ocultar vistas de descuento
        binding.etPorcentajeDescuentoP.visibility = View.GONE
        binding.btnCalcularPrecioDesc.visibility = View.GONE
        binding.txtPrecioConDescuentoP.visibility = View.GONE

        binding.etPrecioConDescuentoP.visibility = View.GONE
        binding.etNotaDescuentoP.visibility = View.GONE

        binding.descuentoSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //switch esta activado
                binding.etPorcentajeDescuentoP.visibility = View.VISIBLE
                binding.btnCalcularPrecioDesc.visibility = View.VISIBLE
                binding.txtPrecioConDescuentoP.visibility = View.VISIBLE

                binding.etPrecioConDescuentoP.visibility = View.VISIBLE
                binding.etNotaDescuentoP.visibility = View.VISIBLE
            } else {
                //switch esta desactivado
                binding.etPorcentajeDescuentoP.visibility = View.GONE
                binding.btnCalcularPrecioDesc.visibility = View.GONE
                binding.txtPrecioConDescuentoP.visibility = View.GONE

                binding.etPrecioConDescuentoP.visibility = View.GONE
                binding.etNotaDescuentoP.visibility = View.GONE
            }
        }
    }

    /**
     * Inicializa un progress dialog para mostrar durante operaciones de red.
     */
    private fun setupProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Cargando")
            .setMessage("Espere por favor")
            .setCancelable(false)
            .create()
    }

    /**
     * Carga las categorías desde Firebase y las almacena en la lista local.
     */
    private fun cargarCategorias() {
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categorias.clear()
                for (ds in snapshot.children) {
                    ds.getValue(Categoria::class.java)?.let { categorias.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AgregarProductoActivity,
                    "Error al cargar categorías: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Muestra un AlertDialog para que el usuario seleccione una categoría.
     */
    private fun seleccionarCategoria() {
        if (categorias.isEmpty()) {
            Toast.makeText(this, "No hay categorías disponibles", Toast.LENGTH_SHORT).show()
            return
        }
        val categoriasArray = categorias.map { it.categoria }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Categoría")
            .setItems(categoriasArray) { _, which ->
                idCat = categorias[which].id
                tituloCat = categorias[which].categoria
                binding.categoria.text = tituloCat
            }
            .show()
    }

    /**
     * Configura los listeners para las interacciones de la interfaz.
     */
    private fun setupListeners() {
        binding.imgAgregarProducto.setOnClickListener { seleccionarImagen() }
        binding.categoria.setOnClickListener { seleccionarCategoria() }
        binding.btnAgregarProducto.setOnClickListener { validarInformacion() }
        binding.btnCalcularPrecioDesc.setOnClickListener { calcularPrecioConDescuento() }
    }

    @SuppressLint("SetTextI18n")
    private fun calcularPrecioConDescuento() {
        val precioOriginal = binding.etPrecioP.text.toString()
        val notaDescuento = binding.etNotaDescuentoP.text.toString()
        val porcentaje = binding.etPorcentajeDescuentoP.text.toString()

        if (precioOriginal.isEmpty()) {
            Toast.makeText(this, "Ingrese el precio original", Toast.LENGTH_SHORT).show()
        } else if (notaDescuento.isEmpty()) {
            Toast.makeText(this, "Ingrese la nota de descuento", Toast.LENGTH_SHORT).show()
        } else if (porcentaje.isEmpty()) {
            Toast.makeText(this, "Ingrese el porcentaje de descuento", Toast.LENGTH_SHORT).show()
        } else {
            val precioOriginalDouble = precioOriginal.toDouble()
            val porcentajeDouble = porcentaje.toDouble()
            val descuento = precioOriginalDouble * (porcentajeDouble / 100) // Calcular el descuento
            val precioConDescuento =
                (precioOriginalDouble - descuento) // Calcular el precio con descuento
            binding.etPrecioConDescuentoP.text = precioConDescuento.toInt().toString()
        }

    }


    @SuppressLint("SetTextI18n")
    private fun validarInformacion() {
        nombreP = binding.etNombresP.text.toString().trim()
        descripcionP = binding.etDescripcionP.text.toString().trim()
        categoriaP = binding.categoria.text.toString().trim()
        precioP = binding.etPrecioP.text.toString().trim()
        descuentoHabilitado = binding.descuentoSwitch.isChecked


        if (nombreP.isEmpty()) {
            binding.etNombresP.error = "Ingrese un nombre"
            binding.etNombresP.requestFocus()

        } else if (descripcionP.isEmpty()) {
            binding.etDescripcionP.error = "Ingrese una descripción"
            binding.etDescripcionP.requestFocus()
        } else if (categoriaP.isEmpty()) {
            binding.categoria.error = "Seleccione una categoría"
            binding.categoria.requestFocus()
        } else if (precioP.isEmpty()) {
            binding.etPrecioP.error = "Ingrese un precio"
            binding.etPrecioP.requestFocus()
        } else {
            // Validar campos de descuento si están habilitados

            if (descuentoHabilitado) {
                notaDescuentoP = binding.etNotaDescuentoP.text.toString().trim()
                porcentajeDescP = binding.etPorcentajeDescuentoP.text.toString().trim()
                precioDescuentoP = binding.etPrecioConDescuentoP.text.toString().trim()

                if (notaDescuentoP.isEmpty()) {
                    binding.etNotaDescuentoP.error = "Ingrese una nota de descuento"
                    binding.etNotaDescuentoP.requestFocus()
                } else if (porcentajeDescP.isEmpty()) {
                    binding.etPorcentajeDescuentoP.error = "Ingrese un porcentaje de descuento"
                    binding.etPorcentajeDescuentoP.requestFocus()
                } else if (precioDescuentoP.isEmpty()) {
                    binding.etPrecioConDescuentoP.text = "No se estableció el precio con descuento"
                } else {
                    if (edicion) {
                        actualizarProducto()
                    } else {
                        if (imagenUri == null) {
                            Toast.makeText(
                                this,
                                "Seleccione al menos una imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Una vez validado, procede a agregar el producto
                            agregarProducto()
                        }
                    }
                }
            } else {
                precioDescuentoP = "0"
                notaDescuentoP = ""

                if (edicion) {
                    actualizarProducto()
                } else {
                    if (imagenUri == null) {
                        Toast.makeText(this, "Seleccione al menos una imagen", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // Una vez validado, procede a agregar el producto
                        agregarProducto()
                    }
                }

            }


        }


    }

    private fun actualizarProducto() {
        progressDialog.setMessage("Actualizando Producto")
        progressDialog.show()

        val productoData: HashMap<String, Any> = hashMapOf(
            "nombre" to nombreP,
            "descripcion" to descripcionP,
            "categoria" to categoriaP,
            "precio" to precioP,
            "precioDescuento" to precioDescuentoP,
            "notaDescuento" to notaDescuentoP
        )

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).updateChildren(productoData).addOnSuccessListener {
            progressDialog.dismiss()
            subirImagenesStorage(idProducto)
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Error al actualizar el producto debido a: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    /**
     * Agrega el producto a Firebase y luego sube las imágenes asociadas.
     */
    private fun agregarProducto() {
        progressDialog.setMessage("Agregando Producto")
        progressDialog.show()

        val reference = FirebaseDatabase.getInstance().getReference("Productos")
        val keyId = reference.push().key ?: ""
        val productoData = hashMapOf(
            "id" to keyId,
            "nombre" to nombreP,
            "descripcion" to descripcionP,
            "categoria" to categoriaP,
            "precio" to precioP,
            "precioDescuento" to precioDescuentoP,
            "notaDescuento" to notaDescuentoP
        )

        reference.child(keyId)
            .setValue(productoData)
            .addOnSuccessListener {
                subirImagenesStorage(keyId)
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al agregar producto debido a: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Sube cada imagen seleccionada a Firebase Storage y la asocia al producto.
     * Se usa un contador para saber cuándo se han subido todas las imágenes.
     */
    private fun subirImagenesStorage(keyId: String) {
        for (i in imagenSeleccionada.indices) {
            val modeloImagenSeleccionada = imagenSeleccionada[i]

            if (!modeloImagenSeleccionada.deInternet) {
                val storageReference =
                    FirebaseStorage.getInstance()
                        .getReference("Productos/${modeloImagenSeleccionada.id}")//ruta de imagen

                storageReference.putFile(modeloImagenSeleccionada.imagenUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl
                            .addOnSuccessListener { uri ->
                                val imagenUrl = uri.toString()
                                val imagenData: HashMap<String, Any> = hashMapOf(
                                    "id" to modeloImagenSeleccionada.id,
                                    "imagenUrl" to imagenUrl
                                )
                                FirebaseDatabase.getInstance().getReference("Productos")
                                    .child(keyId)
                                    .child("Imagenes")
                                    .child(modeloImagenSeleccionada.id)
                                    .updateChildren(imagenData)
                                    .addOnCompleteListener {
                                        if (edicion) {
                                            progressDialog.dismiss()
                                            val intent = Intent(
                                                this,
                                                MainActivityVendedor::class.java
                                            )
                                            startActivity(intent)
                                            finishAffinity()
                                            Toast.makeText(
                                                this,
                                                "Producto actualizado correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            progressDialog.dismiss()
                                            Toast.makeText(
                                                this,
                                                "Producto agregado correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            limpiarCampos()
                                        }
                                    }

                            }
                            .addOnFailureListener { uriError ->
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Error al obtener URL de la imagen: ${uriError.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { error ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this, "Error al subir imagen: ${error.message}", Toast.LENGTH_SHORT
                        ).show()
                    }
            }


        }
    }

    /**
     * Limpia los campos de entrada y reinicia la lista de imágenes.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun limpiarCampos() {
        imagenSeleccionada.clear()
        adaptadorImagenSeleccionada.notifyDataSetChanged()

        binding.etNombresP.text?.clear()
        binding.etDescripcionP.text?.clear()
        binding.etPrecioP.text?.clear()
        binding.categoria.text = ""
        binding.descuentoSwitch.isChecked = false
        binding.etNotaDescuentoP.text?.clear()
        binding.etPorcentajeDescuentoP.text?.clear()
        binding.etPrecioConDescuentoP.text = ""
        imagenUri = null
    }

    /**
     * Configura el RecyclerView para mostrar las imágenes seleccionadas.
     */
    private fun setupRecyclerView() {
        //cargar Imagenes
        adaptadorImagenSeleccionada = AdaptadorImagenSeleccionada(this, imagenSeleccionada, idProducto)
        binding.rvImagenesProductos.adapter = adaptadorImagenSeleccionada
    }

    /**
     * Lanza el selector de imágenes usando la librería ImagePicker.
     */
    private fun seleccionarImagen() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                resultadoImg.launch(intent)
            }
    }


    /**
     * Resultado del selector de imagen.
     */
    private val resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val data = resultado.data
                imagenUri = data?.data
                imagenUri?.let { uri ->
                    val nuevaImagen = ImagenSeleccionada(
                        Constantes().obtenerTiempoDispositivo().toString(),
                        uri,
                        null,
                        false
                    )
                    imagenSeleccionada.add(nuevaImagen)
                    cargarImagen()
                }
            } else {
                Toast.makeText(this, "No seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Agrega la imagen seleccionada a la lista y notifica al adaptador.
     */
    private fun cargarImagen() {
        adaptadorImagenSeleccionada.notifyItemInserted(imagenSeleccionada.size - 1)
    }
}