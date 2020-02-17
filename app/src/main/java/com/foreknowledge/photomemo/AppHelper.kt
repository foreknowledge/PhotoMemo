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
    fun deleteFile(filePath: String) = File(filePath).delete()

    fun deleteFiles(filePaths: List<String>) {
        for (filePath in filePaths)
            deleteFile(filePath)
    }
}

object BitmapHelper {
    fun bitmapToImageFile(context: Context, bitmap: Bitmap?): String {
        if (bitmap == null) return ""

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

        FileOutputStream(imageFile)
            .use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()

                return imageFile.absolutePath
            }
    }

    fun rotateAndCompressBitmap(bitmap: Bitmap?, filePath: String): String {
        if (bitmap == null) return ""

        val options = BitmapFactory.Options()
        options.inSampleSize = 2

        val rotatedBitmap = BitmapFactory.decodeFile(filePath, options).getRotateBitmap(filePath)

        FileOutputStream(filePath)
            .use {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, it)
                it.flush()

                return filePath
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