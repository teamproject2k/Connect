package com.example.connect.presentation.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object ImageCompressor {
    fun compressImage(context: Context, imageUri: Uri?, quality: Int): Uri? {
        try {
            // Open an InputStream from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri!!)
            Log.e("abc", "compressImage: $inputStream")

            // Decode the InputStream into a Bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            Log.e("abc", "original bitmap: $originalBitmap")
            // Compress the bitmap
            val compressedByteArray = compressBitmap(originalBitmap, quality)
            Log.e("abc", "compressed bitmap: $compressedByteArray")


            // Save the compressed byte array to a temporary file
            val compressedFile = saveByteArrayToFile(context, compressedByteArray)
            Log.e("abc", "compressed file: $compressedFile")
            compressedFile.toURI()
            Log.e("abc", "compressed file uri: ${compressedFile.toURI()}")
            Log.e(
                "abc",
                "compressImage: ${FunctionHelper.formatFileSize(compressedFile.length())}",
            )

            // Return the URI of the compressed file
            return Uri.fromFile(compressedFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    // Compress the given Bitmap and return a byte array
    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // Compressed byte array
        return byteArrayOutputStream.toByteArray()
    }

    // Save a byte array to a temporary file
    @Throws(IOException::class)
    private fun saveByteArrayToFile(context: Context, byteArray: ByteArray): File {
        val cacheDir = context.externalCacheDir
        val compressedFile = File.createTempFile("compressed_image", ".jpg", cacheDir)

        FileOutputStream(compressedFile).use { fos ->
            fos.write(byteArray)
        }
        return compressedFile
    }


}