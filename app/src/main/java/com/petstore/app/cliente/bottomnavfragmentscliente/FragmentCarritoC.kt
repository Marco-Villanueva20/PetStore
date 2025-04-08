package com.petstore.app.cliente.bottomnavfragmentscliente

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petstore.app.adaptadores.AdaptadorCarritoC
import com.petstore.app.databinding.FragmentCarritoCBinding
import com.petstore.app.modelos.ProductoCarrito
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FragmentCarritoC : Fragment() {

    private lateinit var binding: FragmentCarritoCBinding

    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosCarrito: ArrayList<ProductoCarrito>
    private lateinit var adaptadorCarritoC: AdaptadorCarritoC

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCarritoCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarCarrito()
        sumaProducto()
        binding.btnGenerarPDF.setOnClickListener {
            generarPDF()
        }
    }


    private fun sumaProducto() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    var suma = 0.0

                    for (producto in snapshot.children) {
                        val precioFinal = producto.child("precioFinal").getValue(String::class.java)

                        if (precioFinal != null) {
                            suma += precioFinal.toDoubleOrNull() ?: 0.0
                        }
                    }
                    // Actualizar la suma después de recorrer todos los productos
                    binding.sumaProductos.text = "$suma PEN"
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun cargarCarrito() {
        productosCarrito = ArrayList()

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(firebaseAuth.uid!!).child("CarritoCompras")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productosCarrito.clear()
                    for (ds in snapshot.children) {
                        val productoCarrito = ds.getValue(ProductoCarrito::class.java)
                        if (productoCarrito != null) {
                            productosCarrito.add(productoCarrito)
                        }
                    }
                    adaptadorCarritoC = AdaptadorCarritoC(mContext, productosCarrito)
                    binding.rvCarrito.adapter = adaptadorCarritoC
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    private fun generarPDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(600, 900, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Título del reporte
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titulo = "Reporte de Carrito"
        val textWidth = paint.measureText(titulo)
        val x = (pageInfo.pageWidth - textWidth) / 2
        canvas.drawText(titulo, x, 50f, paint)

        // Texto de productos
        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT

        var yPosition = 100f
        var total = 0.0

        for (producto in productosCarrito) {
            val nombre = producto.nombre
            val cantidad = producto.cantidad
            val precio = producto.precioFinal.toDoubleOrNull() ?: 0.0
            total += precio

            canvas.drawText("$nombre (x$cantidad) - S/$precio", 50f, yPosition, paint)
            yPosition += 30f
        }

        // Línea divisoria
        paint.strokeWidth = 2f
        canvas.drawLine(50f, yPosition + 10f, 550f, yPosition + 10f, paint)
        yPosition += 30f

        // Total
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total: S/$total", 50f, yPosition, paint)

        pdfDocument.finishPage(page)

        // Guardar el archivo PDF
        val file = File(requireContext().getExternalFilesDir(null), "reporte_carrito.pdf")
        try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            Toast.makeText(requireContext(), "PDF generado correctamente", Toast.LENGTH_SHORT).show()

            // Compartir PDF
            compartirPDF(file)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun compartirPDF(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }
}