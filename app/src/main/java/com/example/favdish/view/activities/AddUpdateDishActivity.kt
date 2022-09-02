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
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding_root: ActivityAddUpdateDishBinding

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
            binding_root.ivDishImage.setImageBitmap(it)
            binding_root.ivAddDishImage.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.ic_vector_edit))
        }
    }

    private val takeImageFromGalleryContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            result?.data?.let {
                val photoUri = it.data
                binding_root.ivDishImage.setImageURI(photoUri)
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

}