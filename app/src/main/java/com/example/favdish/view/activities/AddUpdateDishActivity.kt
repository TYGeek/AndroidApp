package com.example.favdish.view.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.favdish.R
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.karumi.dexter.Dexter
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.*
import java.util.*


class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding_root: ActivityAddUpdateDishBinding
    private var imagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding_root = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding_root.root)

        setupActionBar()

        binding_root.ivAddDishImage.setOnClickListener(this)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding_root.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding_root.toolbarAddDishActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.iv_add_dish_image -> {
                customImageSelectionDialog()
                return
            }
        }
    }

    private val requestPermissionContract =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Do something if permission granted
            if (isGranted) {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                takeImageFromGalleryContract.launch(galleryIntent)
            } else {
                Toast.makeText(this@AddUpdateDishActivity,
                    "You have denied gallery gallery permission",
                    Toast.LENGTH_SHORT).show()
                showRationalDialogForPermissions()
            }
        }

    private val takeImageFromCameraContract = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            //binding_root.ivDishImage.setImageBitmap(it)

            // draw image in ivDishImage
            Glide.with(this)
                .load(it)
                .centerCrop()
                .into(binding_root.ivDishImage)

            // save image to internal storage
            imagePath = saveImageToInternalStorage(it)

            Log.e("ImagePath", imagePath)

            // chancge ivAddDishImage
            binding_root.ivAddDishImage.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.ic_vector_edit))
        }
    }

    private val takeImageFromGalleryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            result?.data?.let {
                val photoUri = it.data
                //binding_root.ivDishImage.setImageURI(photoUri)
                Glide.with(this)
                    .load(photoUri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(e: GlideException?, model: Any?,
                                                  target: Target<Drawable>?,
                                                  isFirstResource: Boolean ): Boolean {
                            Log.e("TAG","Error loading image",e)
                            return false // indicate error
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            resource?.let {
                                val bitmap: Bitmap = resource.toBitmap()
                                imagePath = saveImageToInternalStorage(bitmap)
                                Log.e("ImagePath", imagePath)
                            }
                            return false
                        }
                    })
                    .into(binding_root.ivDishImage)

                binding_root.ivAddDishImage.setImageDrawable(
                    ContextCompat.getDrawable(this,R.drawable.ic_vector_edit))
            }
        } else if(result.resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(this@AddUpdateDishActivity,
                "You have canceled Activity",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
           //     Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let{ it ->
                        if(it.areAllPermissionsGranted()) {
                            takeImageFromCameraContract.launch()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()

            // after click close dialog
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            // uses jetpack ActivityResultContracts
            requestPermissionContract.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            // after click close dialog
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions")
            .setPositiveButton("GO TO SETTINGS") { _,_ ->
                try {
                    val intent =Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){ dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):String{
        val wrapper = ContextWrapper(applicationContext)

        // MODE_PRIVATE -> file creation mode
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        // file = File( folder_to_store, unique_name )
        file = File(file, "${UUID.randomUUID()}.jpg")

        // compress file
        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        return file.absolutePath
    }

    companion object{
        private val IMAGE_DIRECTORY = "FavDishImages"
    }

}