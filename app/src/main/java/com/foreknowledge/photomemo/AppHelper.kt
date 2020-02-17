package com.foreknowledge.photomemo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object RequestCode {
    const val PERMISSION_REQUEST_CODE = 100
    const val CHOOSE_CAMERA_IMAGE = 101
    const val CHOOSE_GALLERY_IMAGE = 102
}

fun switchTo(context: Context, activity: Class<*>, bundle: Bundle? = null) {
    val intent = Intent(context, activity)
    bundle?.let{ intent.putExtras(bundle) }

    context.startActivity(intent)
}

object FileHelper {
    fun createJpgFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    fun deleteFile(filePath: String) = File(filePath).delete()

    fun deleteFiles(filePaths: List<String>) {
        for (filePath in filePaths)
            deleteFile(filePath)
    }
}

object BitmapHelper {
    fun bitmapToImageFile(context: Context, bitmap: Bitmap): String {
        val imageFile = FileHelper.createJpgFile(context)

        return compressBitmapToImageFile(imageFile.absolutePath, bitmap)
    }

    fun rotateAndCompressImage(filePath: String): String {
        val options = BitmapFactory.Options()
        if (File(filePath).length() > 1000000)
            options.inSampleSize = 2

        val rotatedBitmap = BitmapFactory.decodeFile(filePath, options).getRotateBitmap(filePath)

        return compressBitmapToImageFile(filePath, rotatedBitmap)
    }

    private fun compressBitmapToImageFile(imagePath: String, bitmap: Bitmap): String {
        FileOutputStream(imagePath)
            .use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it)
                it.flush()

                return imagePath
            }
    }

    private fun Bitmap.getRotateBitmap(photoPath: String): Bitmap {
        val exifInterface = ExifInterface(photoPath)
        val orientation: Int = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> this.rotateImage(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> this.rotateImage(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> this.rotateImage(270f)
            else -> this
        }
    }

    private fun Bitmap.rotateImage(angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }
}