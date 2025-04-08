package com.petstore.app.adaptadores

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.R
import com.petstore.app.databinding.ItemProductoCBinding
import com.petstore.app.detalleproducto.DetalleProductoActivity
import com.petstore.app.filtro.FiltroProducto
import com.petstore.app.modelos.Producto
import com.petstore.app.utils.Constantes

class AdaptadorProductoC(
    var productos: ArrayList<Producto>,
    private var mContext: Context,
    private var filtroLista: ArrayList<Producto> = productos,
    private var filtro: FiltroProducto? = null,
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : RecyclerView.Adapter<AdaptadorProductoC.HolderProducto>(), Filterable {
    private lateinit var binding: ItemProductoCBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProducto {
        binding = ItemProductoCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProducto(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HolderProducto, position: Int) {
        val modeloProducto = productos[position]

        val nombre = modeloProducto.nombre

        cargarPrimeraImagen(modeloProducto, holder)
        visualizarDescuento(modeloProducto, holder)
        comprobarFavorito(modeloProducto, holder)

        holder.itemNombreP.text = nombre

        //Evento al presionar favorito
        holder.ibFavorito.setOnClickListener {
            val favorito = modeloProducto.favorito

            if (favorito) {
                //Favorito es verdadero
                Constantes().eliminarProductoFav(mContext, modeloProducto.id)
            } else {
                //Favorito es falso
                Constantes().agregarProductoFav(mContext, modeloProducto.id)
            }
        }

        //Evento para dirigir a la actividad de detalle
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetalleProductoActivity::class.java)
            intent.putExtra("idProducto", modeloProducto.id)
            mContext.startActivity(intent)
        }

        //Evento para agregar al carrito el producto seleccionado
        holder.agregarCarrito.setOnClickListener {
            verCarrito(modeloProducto)
        }


    }

    private var costo: Double = 0.0
    private var costoFinal: Double = 0.0
    private var cantidadProducto: Int = 0

    @SuppressLint("SetTextI18n")
    private fun verCarrito(modeloProducto: Producto) {
        //Declarar vistas
        val sivImagen: ShapeableImageView
        val tvNombre: TextView
        val tvDescripcion: TextView
        val tvNotaDesc: TextView
        val tvPrecioOriginal: TextView
        val tvPrecioDescuento: TextView
        val tvPrecioFinal: TextView

        val btnDisminuir: ImageButton
        val tvCantidad: TextView
        val btnAumentar: ImageButton

        val btnAgregarCarrito: MaterialButton

        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.carrito_compras)//HACEMOS REFERENCIA AL CARRITO

        sivImagen = dialog.findViewById(R.id.imagenPCar)
        tvNombre = dialog.findViewById(R.id.nombrePCar)
        tvDescripcion = dialog.findViewById(R.id.descripcioPCar)
        tvNotaDesc = dialog.findViewById(R.id.notaDescPCar)
        tvPrecioOriginal = dialog.findViewById(R.id.precioOriginalPCar)
        tvPrecioDescuento = dialog.findViewById(R.id.precioDescPCar)
        tvPrecioFinal = dialog.findViewById(R.id.precioFinalPCar)
        btnDisminuir = dialog.findViewById(R.id.btnDisminuir)
        tvCantidad = dialog.findViewById(R.id.cantidadPCar)
        btnAumentar = dialog.findViewById(R.id.btnAumentar)
        btnAgregarCarrito = dialog.findViewById(R.id.btnAgregarCarrito)

        //obtener los datos del modelo
        val productoId = modeloProducto.id
        val nombre = modeloProducto.nombre
        val descripcion = modeloProducto.descripcion
        val precio = modeloProducto.precio
        val precioDescuento = modeloProducto.precioDescuento
        val notaDescuento = modeloProducto.notaDescuento

        if (precioDescuento != "0" && notaDescuento != "") {
            //El producto si tiene descuento
            //Mostrar las vistas
            tvNotaDesc.visibility = View.VISIBLE
            tvPrecioDescuento.visibility = View.VISIBLE


            tvNotaDesc.text = notaDescuento
            tvPrecioDescuento.text = precioDescuento.plus(" PEN")
            tvPrecioOriginal.text = precio.plus(" PEN")
            tvPrecioOriginal.paintFlags = tvPrecioOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            costo = precioDescuento.toDouble()//precio almacena el precio con descuento
        } else {
            //El producto no tiene descuento
            //Ocultar las vistas
            tvPrecioOriginal.text = precio.plus(" PEN")
            tvPrecioOriginal.paintFlags =
                tvPrecioOriginal.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() //Quitar el tachado del precio original
            costo = precio.toDouble()//precio almacena el precio original sin descuento
        }

        costoFinal = costo
        cantidadProducto = 1

        tvCantidad.text = cantidadProducto.toString()
        tvPrecioFinal.text = costoFinal.toString()

        //setear la informacion
        tvNombre.text = nombre
        tvDescripcion.text = descripcion

        /*Incrementar cantidad*/
        btnAumentar.setOnClickListener {
            cantidadProducto++                   // Primero incremento la cantidad
            costoFinal = costo * cantidadProducto // Luego recalculo el total
            tvCantidad.text = cantidadProducto.toString()
            tvPrecioFinal.text = costoFinal.toString()

        }
        /*Disminuir cantidad*/
        btnDisminuir.setOnClickListener {
            /*Disminuir cantidad solo si la cantidad es mayor a 1*/
            if (cantidadProducto > 1) {
                cantidadProducto--               // Disminuyo la cantidad
                costoFinal = costo * cantidadProducto // Recalculo el total
                tvCantidad.text = cantidadProducto.toString()
                tvPrecioFinal.text = costoFinal.toString()
            }
        }

        tvPrecioFinal.text = costo.toString()

        /*Obtener primera imagen*/
        cargarImagen(productoId, sivImagen)

        btnAgregarCarrito.setOnClickListener {
            agregarCarrito(mContext, modeloProducto, costoFinal, cantidadProducto)
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun agregarCarrito(mContext: Context, modeloProducto: Producto, costoFinal: Double, cantidadProducto: Int) {
        val firebaseAuth = FirebaseAuth.getInstance()

        val hashMap = HashMap<String, Any>()
        hashMap["idProducto"] = modeloProducto.id
        hashMap["nombre"] = modeloProducto.nombre
        hashMap["precio"] = modeloProducto.precio
        hashMap["precioDescuento"] = modeloProducto.precioDescuento
        hashMap["precioFinal"] = costoFinal.toString()
        hashMap["cantidad"] = cantidadProducto

        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
        referencia.child(firebaseAuth.uid!!).child("CarritoCompras").child(modeloProducto.id)
            .setValue(hashMap).addOnSuccessListener {
                Toast.makeText(mContext, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {e->
                Toast.makeText(mContext, "Error al agregar al carrito debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun cargarImagen(productoId: String, sivImagen: ShapeableImageView) {

        val referencia = FirebaseDatabase.getInstance().getReference("Productos")
        referencia.child(productoId).child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imagenUrl = "${ds.child("imagenUrl").value}"

                        try {

                            Glide.with(mContext)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(sivImagen)

                        }catch (e:Exception){

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun comprobarFavorito(
            modeloProducto: Producto,
            holder: AdaptadorProductoC.HolderProducto
        ) {
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference
                .child(firebaseAuth.uid!!)
                .child("Favoritos")
                .child(modeloProducto.id)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favorito = snapshot.exists()
                        if (favorito) {
                            holder.ibFavorito.setImageResource(R.drawable.ic_favorito)
                        } else {
                            holder.ibFavorito.setImageResource(R.drawable.ic_no_favorito)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

        }

        @SuppressLint("SetTextI18n")
        private fun visualizarDescuento(
            modeloProducto: Producto,
            holder: HolderProducto
        ) {
            if (modeloProducto.precioDescuento != "0" && modeloProducto.notaDescuento != "") {
                //Habilitar las vistas
                holder.itemNotaP.visibility = View.VISIBLE
                holder.itemPrecioPDec.visibility = View.VISIBLE

                //setear la informacion
                holder.itemNotaP.text = modeloProducto.notaDescuento
                holder.itemPrecioPDec.text = "${modeloProducto.precioDescuento}${" PEN"}"
                holder.itemPrecioP.text = "${modeloProducto.precio}${" PEN"}"

                holder.itemPrecioP.paintFlags =
                    holder.itemPrecioP.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG //tachando el precio original
            } else {
                //El producto no tiene descuento
                //Ocultar las vistas
                holder.itemNotaP.visibility = View.GONE
                holder.itemPrecioPDec.visibility = View.GONE

                //setear la informacion
                holder.itemPrecioP.text = "${modeloProducto.precio}${" PEN"}"
                holder.itemPrecioP.paintFlags =
                    holder.itemPrecioP.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() //Quitar el tachado del precio original
            }

        }


        private fun cargarPrimeraImagen(
            modeloProducto: Producto,
            holder: AdaptadorProductoC.HolderProducto
        ) {
            val idProducto = modeloProducto.id
            val referencia = FirebaseDatabase.getInstance().getReference("Productos")
            referencia.child(idProducto).child("Imagenes")
                .limitToFirst(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            val imagenUrl = "${ds.child("imagenUrl").value}"
                            try {
                                Glide.with(mContext).load(imagenUrl)
                                    .placeholder(R.drawable.item_img_producto)
                                    .into(holder.imagenProducto)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    mContext,
                                    "Error se debe a:${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

        override fun getItemCount(): Int {
            return productos.size
        }


        inner class HolderProducto(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imagenProducto = binding.imagenP
            var itemNombreP = binding.itemNombreP
            var itemPrecioP = binding.itemPrecioP
            var itemNotaP = binding.itemNotaP
            var itemPrecioPDec = binding.itemPrecioPDesc
            var ibFavorito = binding.ibFavorito
            var agregarCarrito = binding.itemAgregarCarritoP
        }

        override fun getFilter(): Filter {
            if (filtro == null) {
                filtro = FiltroProducto(this, filtroLista)
            }
            return filtro as FiltroProducto
        }

    }