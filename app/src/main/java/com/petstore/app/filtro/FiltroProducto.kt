package com.petstore.app.filtro

import android.annotation.SuppressLint
import android.widget.Filter
import com.petstore.app.adaptadores.AdaptadorProductoC
import com.petstore.app.modelos.Producto
import java.util.Locale

class FiltroProducto(
    private val adaptador: AdaptadorProductoC,
    private val filtroLista: ArrayList<Producto>
) : Filter() {


    override fun performFiltering(filter: CharSequence?): FilterResults {
        var filtro = filter
        val resultados = FilterResults()

        if (!filtro.isNullOrEmpty()) {
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val listaFiltrada: ArrayList<Producto> = ArrayList()

            for (i in filtroLista.indices) {
                if (filtroLista[i].nombre.uppercase(Locale.getDefault()).contains(filtro)) {
                    listaFiltrada.add(filtroLista[i])
                }
            }
            resultados.count = listaFiltrada.size
            resultados.values = listaFiltrada
        } else {
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(filtro: CharSequence?, resultados: FilterResults) {
        val productosFiltrados = resultados.values as ArrayList<Producto>
        adaptador.productos = productosFiltrados
        adaptador.notifyDataSetChanged()
    }

}