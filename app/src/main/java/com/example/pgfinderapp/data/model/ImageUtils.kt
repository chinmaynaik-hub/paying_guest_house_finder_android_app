package com.example.pgfinderapp.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val COMPRESSION_QUALITY = 80
    
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    
    /**
     * Compresses an image from URI and returns the compressed byte array
     */
    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            Log.d(TAG, "Starting image compression for: $imageUri")
            
            // Get input stream from URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream")
                return null
            }
            
            // Decode bitmap with options to get dimensions first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calculate sample size for memory efficiency
            val sampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
            Log.d(TAG, "Original size: ${options.outWidth}x${options.outHeight}, sampleSize: $sampleSize")
            
            // Decode bitmap with sample size
            val decodingOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val newInputStream = context.contentResolver.openInputStream(imageUri)
            var bitmap = BitmapFactory.decodeStream(newInputStream, null, decodingOptions)
            newInputStream?.close()
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                return null
            }
            
            // Handle EXIF rotation
            bitmap = handleExifRotation(context, imageUri, bitmap)
            
            // Scale down if still too large
            bitmap = scaleBitmapIfNeeded(bitmap, MAX_WIDTH, MAX_HEIGHT)
            
            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            
            val compressedBytes = outputStream.toByteArray()
            Log.d(TAG, "Compressed image size: ${compressedBytes.size / 1024} KB")
            
            // Recycle bitmap to free memory
            bitmap.recycle()
            
            compressedBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Uploads compressed image to Firebase Storage and returns the download URL
     */
    suspend fun uploadCompressedImage(context: Context, imageUri: Uri, pgId: String): String? {
        return try {
            val compressedBytes = compressImage(context, imageUri)
            if (compressedBytes == null) {
                Log.e(TAG, "Compression failed")
                return null
            }
            
            val fileName = "pg_images/$pgId/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)
            
            Log.d(TAG, "Uploading image to: $fileName")
            
            // Upload compressed bytes
            imageRef.putBytes(compressedBytes).await()
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Upload successful, URL: $downloadUrl")
            
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Uploads multiple images and returns list of URLs
     */
    suspend fun uploadMultipleImages(context: Context, imageUris: List<Uri>, pgId: String): List<String> {
        val urls = mutableListOf<String>()
        for (uri in imageUris) {
            val url = uploadCompressedImage(context, uri, pgId)
            if (url != null) {
                urls.add(url)
            }
        }
        return urls
    }
    
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        Log.d(TAG, "Scaling bitmap from ${width}x${height} to ${newWidth}x${newHeight}")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun handleExifRotation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            }
            
            if (!matrix.isIdentity) {
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                rotatedBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling EXIF rotation: ${e.message}")
            bitmap
        }
    }
}
