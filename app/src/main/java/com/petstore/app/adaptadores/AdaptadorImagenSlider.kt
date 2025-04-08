package com.petstore.app.adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.petstore.app.R
import com.petstore.app.databinding.ItemImagenSliderBinding
import com.petstore.app.modelos.ImagenSlider

class AdaptadorImagenSlider : RecyclerView.Adapter<AdaptadorImagenSlider.HolderImagenSlider> {
    private lateinit var binding: ItemImagenSliderBinding

    private var context: Context
    private var imagenSliderArrayList: ArrayList<ImagenSlider>

    constructor(context: Context, imagenSliderArrayList: ArrayList<ImagenSlider>) {
        this.context = context
        this.imagenSliderArrayList = imagenSliderArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSlider(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val modeloImagenSlider = imagenSliderArrayList[position]

        val imagenUrl = modeloImagenSlider.imagenUrl
        val imagenContador = "${position + 1}/${imagenSliderArrayList.size}"// Ej: 2/4 o 3/4
        holder.tvImagenContador.text = imagenContador

        try {
            Glide.with(context)
                .load(imagenUrl)
                .placeholder(R.drawable.item_img_producto)
                .into(holder.sivImagen)
        } catch (e: Exception) {
            print(e.message)
        }

        holder.itemView.setOnClickListener {
            zoomImg(imagenUrl)
        }
    }

    override fun getItemCount(): Int {
        return imagenSliderArrayList.size
    }

    inner class HolderImagenSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sivImagen = binding.sivImagen
        var tvImagenContador = binding.tvImagenContador

    }

    private fun zoomImg(imagen: String) {
        val pv: PhotoView
        val btnCerrarZoom: MaterialButton

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.zoom_imagen)

        pv = dialog.findViewById(R.id.zoomImg)
        btnCerrarZoom = dialog.findViewById(R.id.btnCerrarZoom)
        try {
            Glide.with(context)
                .load(imagen)
                .placeholder(R.drawable.item_img_producto)
                .into(pv)
            dialog.show()
        } catch (e: Exception) {
            print(e.message)
        }

        btnCerrarZoom.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }


}