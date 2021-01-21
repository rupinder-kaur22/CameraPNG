package com.web.camerapng

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.slazzer.bgremover.Slazzer
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import okio.sink
import org.json.JSONObject
import java.io.File
import java.io.FileDescriptor
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var image_uri: Uri? = null
    internal lateinit var resultUri: Uri
    //c9bb6c49b00045ae9461efe2585a45aa
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        runtimePermissions()
        Slazzer.init("5e60821210b6426f97482d8eeedf0cf0")
//        Slazzer.init("0ab301f986f3437d871edaa3ef2d8f5b")
//        RemoveBg.init("2JcTW3z98oWL69T9EdYJkNA5")
        img_updateprofile_index.setOnClickListener(View.OnClickListener {
            takePhoto()
        })
        img_updateprofile_index2.setOnClickListener(View.OnClickListener {
            choosePhoto()
        })
    }

    fun api(file: File) {
        Slazzer.from(file,
            object : Slazzer.ResponseCallback {

                override fun onProgressStart() {
                    Log.e("ErrStartore", "Start")
                    Toast.makeText(this@MainActivity, "uploading start", Toast.LENGTH_SHORT)
                        .show()
                    //  will be invoked on progress start
                }

                override fun onSuccess(response: String) {
                    val jsonObject = JSONObject(response)
//            Glide.with(this@MainActivity).load(jsonObject.optString("output_image_url")).into(img_updateprofile_index)
                    Toast.makeText(this@MainActivity, "Progress sucess", Toast.LENGTH_SHORT).show()
                    Picasso.get().load(jsonObject.optString("output_image_url"))
                        .into(img_updateprofile_index2)
                }

                override fun onProgressUpdate(percentage: Float) {
                    runOnUiThread {
                        //  will be invoked when progress update
                    }

                }

                override fun onProgressEnd() {
                    Log.e("progress", "end")
                    //  will be invoked when progress end
                }

                override fun onError(errors: String) {
                    Log.e("Error", errors.toString())
                    Toast.makeText(this@MainActivity, "Error:  $errors", Toast.LENGTH_SHORT)
                        .show()
                    //  will be invoked when error occurred
                }
            })
    }

    private fun runtimePermissions() {
        Dexter.withActivity(this@MainActivity)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) { // do you work now
                    }
                    for (i in report.deniedPermissionResponses.indices) { //                            Log.d("dennial permision res", report.getDeniedPermissionResponses().get(i).getPermissionName());
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) { // permission is denied permenantly, navigate user to app settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }


    fun takePhoto() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf<String>(Manifest.permission.CAMERA),
                100
            )
            return
        }
//        isCamera = true

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri =
            this@MainActivity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, 101)

    }

    fun choosePhoto() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
            return
        }
//        isCamera = false
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        if (intent.resolveActivity(this@MainActivity.getPackageManager()) != null) {
            startActivityForResult(intent, 123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
/*
          if (requestCode == (CutOut.CUTOUT_ACTIVITY_REQUEST_CODE).toInt()) {


//            when (resultCode) {
//                case Activity.RESULT_OK:
//                    Uri imageUri = CutOut.getUri(data);
//                    // Save the image using the returned Uri here
//                    break;
//                case CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE:
//                    Exception ex = CutOut.getError(data);
//                    break;
//                default:
//                    System.out.print("User cancelled the CutOut screen");
//            }
        }
*/
        //https://github.com/slazzercom/Slazzer-Automatic-Remove-Image-Background-Android

        if (requestCode == 101 && resultCode == AppCompatActivity.RESULT_OK) {


            CropImage.activity(image_uri)
                .start(this@MainActivity)
//            CutOut.activity()
//                .src(image_uri)
//                .bordered()
//                .noCrop()
//                .intro()
//                .start(this)
        } else if (requestCode == 123 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            Log.e("selectedImage", "" + selectedImage);
//            CutOut.activity()
//                .src(selectedImage)
//                .bordered()
//                .noCrop()
//                .intro()
//                .start(this)
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            CropImage.activity(selectedImage)
                .start(this@MainActivity)

        }
//2JcTW3z98oWL69T9EdYJkNA5
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                resultUri = result.getUri()
//                Glide.with(this@MainActivity).load(resultUri).into(img_updateprofile_index)
                val file = File(resultUri.path)
                api(file)
//                uriToBitmap(resultUri)

//                CutOut.activity()
//                    .src(resultUri)
//                    .bordered()
//                    .noCrop()
//                    .intro()
//                    .start(this)
            }
        } else {
//            Log.e("ImageUri", CutOut.getUri(data).toString())
        }
    }

    private fun firebaseAPI(resultUri: Uri?) {

//        val image = FirebaseVisionImage.fromBitmap(bitmap);

    }

    private fun uriToBitmap(resultUri: Uri) {
        try {
            val parcelFileDescriptor =
                getContentResolver().openFileDescriptor(resultUri, "r");
            val fileDescriptor = parcelFileDescriptor?.getFileDescriptor();
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor?.close()
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }
}
