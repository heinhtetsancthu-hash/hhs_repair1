package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.local.entity.TicketEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {
    fun exportTicketToPdf(context: Context, ticket: TicketEntity) {
        val pdfDocument = PdfDocument()
        
        // A5 Paper Size in PostScript Points: 420 wide by 595 high
        val pageInfo = PdfDocument.PageInfo.Builder(420, 595, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        // Color definitions matching our dashboard theme
        val brandIndigo = Color.rgb(79, 70, 229)
        val textDark = Color.rgb(30, 41, 59)
        val textMedium = Color.rgb(71, 85, 105)
        val textLight = Color.rgb(148, 163, 184)
        val backgroundLight = Color.rgb(248, 250, 252)
        val errorAmber = Color.rgb(194, 65, 12)
        
        // Paints
        val titlePaint = Paint().apply {
            color = textDark
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val subtitlePaint = Paint().apply {
            color = brandIndigo
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val textPaint = Paint().apply {
            color = textMedium
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textBoldPaint = Paint().apply {
            color = textDark
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val metaLabelPaint = Paint().apply {
            color = textLight
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            strokeWidth = 1f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225) // Slate 300
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        
        val accentBoxPaint = Paint().apply {
            color = backgroundLight
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw Outer Frame Border on the A5 sheet
        canvas.drawRect(15f, 15f, 405f, 580f, borderPaint)
        
        // Header Section
        canvas.drawText("MOBILE REPAIR SPECIALIST PORTAL", 30f, 40f, titlePaint)
        canvas.drawText("OFFICIAL WORKORDER SERVICE SLIP", 30f, 52f, subtitlePaint)
        
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(ticket.createdAt))
        canvas.drawText("Date: $dateString", 260f, 40f, textPaint)
        canvas.drawText("Ticket ID: ${ticket.id}", 260f, 52f, textBoldPaint)
        
        canvas.drawLine(30f, 65f, 390f, 65f, linePaint)
        
        // Column 1 Layout (Customer & Device Details side-by-side)
        canvas.drawText("CUSTOMER & CONTACT DETAILS", 30f, 85f, subtitlePaint)
        
        canvas.drawText("Customer Name:", 30f, 105f, textPaint)
        canvas.drawText(ticket.customerName, 120f, 105f, textBoldPaint)
        
        canvas.drawText("Phone Compact:", 30f, 120f, textPaint)
        canvas.drawText(ticket.phoneNumber, 120f, 120f, textBoldPaint)
        
        canvas.drawLine(30f, 135f, 390f, 135f, linePaint)
        
        // Device classification
        canvas.drawText("DEVICE GENERAL DIAGNOSTICS", 30f, 155f, subtitlePaint)
        
        canvas.drawText("Device Model:", 30f, 175f, textPaint)
        canvas.drawText("${ticket.deviceBrand} ${ticket.deviceModel}", 130f, 175f, textBoldPaint)
        
        canvas.drawText("IMEI Serial No:", 30f, 190f, textPaint)
        canvas.drawText(ticket.imei ?: "Not provided", 130f, 190f, textBoldPaint)
        
        canvas.drawLine(30f, 205f, 390f, 205f, linePaint)
        
        // Troubleshooting details section
        canvas.drawText("FAULT DETAIL & CLASSIFICATION", 30f, 225f, subtitlePaint)
        
        canvas.drawText("Primary Issue Type:", 30f, 245f, textPaint)
        canvas.drawText(ticket.errorType, 150f, 245f, textBoldPaint)
        
        canvas.drawText("Lock screen security type:", 30f, 260f, textPaint)
        canvas.drawText(ticket.screenLockType, 150f, 260f, textBoldPaint)
        
        if (ticket.screenLockType != "None" && ticket.screenLockValue.isNotBlank()) {
            canvas.drawText("Lock screen combination:", 30f, 275f, textPaint)
            // Draw lock credential
            canvas.drawText(ticket.screenLockValue, 150f, 275f, textBoldPaint)
        }
        
        canvas.drawText("Included Accessories:", 30f, 290f, textPaint)
        val accText = ticket.includedAccessories.ifBlank { "None" }.replace(",", ", ")
        canvas.drawText(accText, 150f, 290f, textBoldPaint)
        
        canvas.drawLine(30f, 305f, 390f, 305f, linePaint)
        
        // Service notes and remarks section
        canvas.drawText("ADDITIONAL TECHNICAL SERVICE REMARKS", 30f, 325f, subtitlePaint)
        val notes = ticket.serviceNotes.ifBlank { "No additional specifications recorded." }
        val lines = notes.chunked(65)
        var yPos = 345f
        for (i in 0 until minOf(lines.size, 5)) {
            canvas.drawText(lines[i], 30f, yPos, textPaint)
            yPos += 14f
        }
        
        // Service cost box (using beautiful filled rectangle with indigo bounds)
        canvas.drawRect(30f, 440f, 390f, 490f, accentBoxPaint)
        
        val costLabelPaint = Paint().apply {
            color = brandIndigo
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val costValPaint = Paint().apply {
            color = brandIndigo
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("ESTIMATED TOTAL SERVICE COSTS & FEES", 42f, 458f, costLabelPaint)
        canvas.drawText("$" + String.format("%.2f", ticket.estimatedCost), 42f, 480f, costValPaint)
        canvas.drawText("Status: ${ticket.status.uppercase()}", 240f, 470f, textBoldPaint)

        // Signatures area
        canvas.drawText("TECHNICIAN SIGNATURE", 30f, 520f, metaLabelPaint)
        canvas.drawLine(30f, 545f, 170f, 545f, linePaint)
        
        canvas.drawText("CUSTOMER AGREED SIGNATURE", 230f, 520f, metaLabelPaint)
        canvas.drawLine(230f, 545f, 370f, 545f, linePaint)
        
        canvas.drawText("Thank you for choosing Mobile Repair Portal. Guaranteed repair excellence.", 30f, 565f, textPaint)

        pdfDocument.finishPage(page)
        
        // Build safe file name: customer name + device brand + device model (e.g. John_Samsung_Galaxy_S24.pdf)
        val cleanCustomer = ticket.customerName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanBrand = ticket.deviceBrand.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val cleanModel = ticket.deviceModel.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        
        val baseFileName = "${cleanCustomer}_${cleanBrand}_${cleanModel}.pdf"
        
        try {
            // Write to Public Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val destinationFile = File(downloadsDir, baseFileName)
            val outputStream = FileOutputStream(destinationFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            
            Toast.makeText(context, "Exported A5 PDF to Downloads: $baseFileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to internal downloads directory
            try {
                val fallbackFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), baseFileName)
                val outputStream = FileOutputStream(fallbackFile)
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                Toast.makeText(context, "Exported A5 PDF: $baseFileName", Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "Failed exporting PDF: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            pdfDocument.close()
        }
    }
}
