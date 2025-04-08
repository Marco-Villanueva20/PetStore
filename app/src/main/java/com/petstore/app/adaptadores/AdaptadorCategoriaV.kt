package com.petstore.app.adaptadores

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.petstore.app.databinding.ItemCategoriaVBinding
import com.petstore.app.modelos.Categoria

class AdaptadorCategoriaV(
    private val mContext: Context,
    private var listaCategorias: ArrayList<Categoria>
) : RecyclerView.Adapter<AdaptadorCategoriaV.HolderCategoriaV>() {

    private lateinit var binding: ItemCategoriaVBinding

    inner class HolderCategoriaV(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item_nombre_c_v = binding.itemNombreCV
        var item_eliminar_c_v = binding.itemEliminarC
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoriaV {
        binding = ItemCategoriaVBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderCategoriaV(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategoriaV, position: Int) {
        val modelo = listaCategorias[position]

        val id = modelo.id
        val categoria = modelo.categoria

        holder.item_nombre_c_v.text = categoria

        holder.item_eliminar_c_v.setOnClickListener {
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("Eliminar Categoría")
                .setMessage("¿Estás seguro(a) de que deseas eliminar esta categoría?")
                .setPositiveButton("Sí") { _, _ ->
                    Toast.makeText(mContext, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    eliminarCategoria(modelo, holder)
                }
                .setNegativeButton("No") { a, _ ->
                    a.dismiss()
                    Toast.makeText(mContext, "Acción cancelada", Toast.LENGTH_SHORT).show()
                }
            builder.show()
        }
    }

    private fun eliminarCategoria(modelo: Categoria, holder: AdaptadorCategoriaV.HolderCategoriaV) {
        val id = modelo.id
        val ref = FirebaseDatabase.getInstance().getReference("Categorias")

        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                eliminarImgCat(id)
            }.addOnFailureListener { e ->
                Toast.makeText(
                    mContext,
                    "No se pudo eliminar la categoría debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun eliminarImgCat(id: String) {
        val rutaImgen = "Categorias/$id"
        val storageReference = FirebaseStorage.getInstance().getReference(rutaImgen)
        storageReference.delete().addOnSuccessListener {
            Toast.makeText(mContext, "Imagen eliminada de la categoria", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(mContext, "No se pudo eliminar la imagen", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return listaCategorias.size
    }


}