package com.petstore.app.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.petstore.app.R
import com.petstore.app.cliente.productoscliente.ProductosCatCActivity
import com.petstore.app.databinding.ItemCategoriaCBinding
import com.petstore.app.modelos.Categoria

class AdaptadorCategoriaC(
    private val mContext: Context,
    private var categorias: ArrayList<Categoria>
) :
    RecyclerView.Adapter<AdaptadorCategoriaC.HolderCategoriaC>() {
    private lateinit var binding: ItemCategoriaCBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoriaC {
        binding = ItemCategoriaCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderCategoriaC(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategoriaC, position: Int) {
        val modelo = categorias[position]
        val categoria = modelo.categoria
        val imagenUrl = modelo.imagen

        holder.itemNombreCC.text = categoria

        Glide.with(mContext)
            .load(imagenUrl)
            .placeholder(R.drawable.categoria)
            .into(holder.itemImgCategoria)

        //Evento para ver los productos de la categoria
        holder.itemVerProductos.setOnClickListener {
            val intent = Intent(mContext, ProductosCatCActivity::class.java)
            intent.putExtra("categoria", categoria)
            Toast.makeText(mContext, "Categoria seleccionada: $categoria", Toast.LENGTH_SHORT)
                .show()
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return categorias.size
    }

    inner class HolderCategoriaC(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemNombreCC = binding.itemNombreCC
        var itemImgCategoria = binding.imagenCategoria
        var itemVerProductos = binding.itemVerProductos
    }


}