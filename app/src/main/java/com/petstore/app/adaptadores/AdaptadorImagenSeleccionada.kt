package com.petstore.app.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.petstore.app.R
import com.petstore.app.databinding.ItemImagenesSeleccionadasBinding
import com.petstore.app.modelos.ImagenSeleccionada

class AdaptadorImagenSeleccionada(
    private val context: Context,
    private val imagenesSeleccionadas: ArrayList<ImagenSeleccionada>,
    private val idProducto: String
) : RecyclerView.Adapter<AdaptadorImagenSeleccionada.HolderImagenSeleccionada>() {
    private lateinit var binding: ItemImagenesSeleccionadasBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding =
            ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSeleccionadas[position]

        if (modelo.deInternet) {
            try {
                val imagenUrl = modelo.imagenUrl
                Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.imagenItem)
            } catch (e: Exception) {
                println(e.message)
            }
        } else {
            val imageUri = modelo.imagenUri
            try {
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.imagenItem)

            } catch (e: Exception) {
                holder.imagenItem.setImageResource(R.drawable.item_imagen)
            }
        }

        //Evento para eliminar una imagen de la lista
        holder.btnBorrar.setOnClickListener {
            if (modelo.deInternet) {
                //Eliminar de Firebase
                eliminarImagen(modelo, position)
            }
            imagenesSeleccionadas.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(
                position, imagenesSeleccionadas.size
            ) // Ajusta los índices,dice los elementos desde position en adelante han cambiado, asegurando que se actualicen correctamente.
        }
    }

    private fun eliminarImagen(
        modelo: ImagenSeleccionada,
        position: Int
    ) {
        val idImagen = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
            ref.child(idProducto)
            .child("Imagenes").child(idImagen)
            .removeValue().addOnSuccessListener {
                try {
                    imagenesSeleccionadas.remove(modelo)
                    notifyItemRemoved(position)
                    eliminarImagenStorage(modelo)
                    Toast.makeText(context, "Imagen eliminada", Toast.LENGTH_SHORT).show()
                }catch (e: Exception){
                    Toast.makeText(context, "No se pudo eliminar la imagen debido a: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(context, "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show()
            }

    }

    private fun eliminarImagenStorage(modelo: ImagenSeleccionada) {
        val rutaImagen = "Productos/${modelo.id}"
        val storageReference = FirebaseStorage.getInstance().getReference(rutaImagen)
        storageReference.delete().addOnSuccessListener {
            Toast.makeText(context, "Imagen eliminada de Storage", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "No se pudo eliminar la imagen de Storage", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun getItemCount(): Int {
        return imagenesSeleccionadas.size
    }

    inner class HolderImagenSeleccionada(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenItem = binding.imagenItem
        var btnBorrar = binding.borrarItem

    }

}