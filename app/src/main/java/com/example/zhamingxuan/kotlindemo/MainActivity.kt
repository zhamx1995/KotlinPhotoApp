package com.example.zhamingxuan.kotlindemo

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v4.content.FileProvider.getUriForFile
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

const private val REQUEST_IMAGE_CAPTURE = 1

class MainActivity : AppCompatActivity() {

    private var selectedPhotoPath: Uri? = null
    private var pictureTaken: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageBtn = findViewById<ImageButton>(R.id.imageButton)
        imageBtn.setOnClickListener {
            dispatchTakePictureIntent()
//            takePictureWithCamera()
        }
    }

    //https://www.raywenderlich.com/305-android-intents-tutorial-with-kotlin
    private fun takePictureWithCamera() {
        // 1
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 2
        val imagePath = File(filesDir, "images")
        val newFile = File(imagePath, "default_image.jpg")
        if (newFile.exists()) {
            newFile.delete()
        } else {
            newFile.parentFile.mkdirs()
        }
        selectedPhotoPath = getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", newFile)

        // 3
        captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedPhotoPath)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            val clip = ClipData.newUri(contentResolver, "A photo", selectedPhotoPath)
            captureIntent.clipData = clip
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

    }

    //https://developer.android.com/training/camera/photobasics#kotlin
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    var saveURI : Uri = Uri.parse("content://com.example.android.fileprovider/my_images/temp.jpg")
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, saveURI)
//                    takePictureIntent.setDataAndType(photoURI, "image/*");
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, setFilePath());
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    //https://blog.csdn.net/u010298576/article/details/50537983?utm_source=itdadao&utm_medium=referral
    private fun setFilePath(): Uri{
        val outPath = Environment.getExternalStorageDirectory().absolutePath + "/takePic/" + System.currentTimeMillis()+".jpg";
        return Uri.fromFile(File(outPath))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            setImageViewWithImage()
            val imageBitmap = data!!.extras.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }



    var mCurrentPhotoPath = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun setImageViewWithImage() {
        val photoPath: Uri = selectedPhotoPath ?: return
        imageView.post {
            var input: InputStream
            try {
                input = this.contentResolver.openInputStream(photoPath)
            } catch (e: FileNotFoundException) {
                throw IllegalStateException(e)
            }
            val bmpFactoryOptions = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeStream(input, null, bmpFactoryOptions)
            imageView.setImageBitmap(bitmap)
        }
//        lookingGoodTextView.visibility = View.VISIBLE
        pictureTaken = true
    }

}
