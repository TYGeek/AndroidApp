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
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.CustomListItemAdaptor
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.*
import java.util.*


class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private var mImagePath: String = ""
    private lateinit var mCustomListDialog: Dialog

    private val mFavDishViewModel : FavDishViewModel by viewModels {
        FavDishViewModelFactory( (application as FavDishApplication).repository )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()

        mBinding.ivAddDishImage.setOnClickListener(this)
        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)
        mBinding.btnAddDish.setOnClickListener(this)
    }

    private fun setupActionBar(){
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.iv_add_dish_image -> {
                customImageSelectionDialog()
                return
            }
            R.id.et_type -> {
                customItemsDialog(resources.getString(R.string.title_select_dish_type),
                    Constants.dishTypes(),
                    Constants.DISH_TYPE)
                return
            }
            R.id.et_category -> {
                customItemsDialog(resources.getString(R.string.title_select_dish_category),
                    Constants.dishCategories(),
                    Constants.DISH_CATEGORY)
                return
            }
            R.id.et_cooking_time -> {
                customItemsDialog(resources.getString(R.string.title_select_dish_cooking_time),
                    Constants.dishCookTime(),
                    Constants.DISH_COOKING_TIME)
                return
            }
            R.id.btn_add_dish -> {
                // trim empty spaces from string
                val title = mBinding.etTitle.text.toString().trim{ it <= ' ' }
                val type = mBinding.etType.text.toString().trim{ it <= ' ' }
                val category = mBinding.etCategory.text.toString().trim{ it <= ' ' }
                val ingredients = mBinding.etIngredients.text.toString().trim{ it <= ' ' }
                val cookingTime = mBinding.etCookingTime.text.toString().trim{ it <= ' ' }

                when{
                    TextUtils.isEmpty(mImagePath)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                        resources.getString(R.string.error_msg_select_dish_image),
                        Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(title)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                            resources.getString(R.string.error_msg_enter_dish_title),
                            Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(type)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                            resources.getString(R.string.error_msg_select_dish_type),
                            Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(category)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                            resources.getString(R.string.error_msg_select_dish_category),
                            Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(ingredients)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                            resources.getString(R.string.error_msg_select_dish_ingredients),
                            Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(cookingTime)->{
                        Toast.makeText(this@AddUpdateDishActivity,
                            resources.getString(R.string.error_msg_select_dish_cooking_time),
                            Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Create FavDish entity object to save it in database
                        val favDishDetails: FavDish = FavDish(
                            mImagePath,
                            Constants.DISH_IMAGE_SOURCE_LOCAL,
                            title,
                            type,
                            category,
                            ingredients,
                            cookingTime )

                        // insert FavDish entity to database
                        mFavDishViewModel.insert(favDishDetails)

                        // create Toast to show that is ok
                        Toast.makeText(this@AddUpdateDishActivity,
                            "Successfully added FavDish details",
                            Toast.LENGTH_SHORT).show()
                        Log.i("Insertion", "Success")

                        // fihish() -- mean close AddUpdateDishActivity
                        finish()
                    }
                }

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
                .into(mBinding.ivDishImage)

            // save image to internal storage
            mImagePath = saveImageToInternalStorage(it)

            Log.e("ImagePath", mImagePath)

            // chancge ivAddDishImage
            mBinding.ivAddDishImage.setImageDrawable(
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
                                mImagePath = saveImageToInternalStorage(bitmap)
                                Log.e("ImagePath", mImagePath)
                            }
                            return false
                        }
                    })
                    .into(mBinding.ivDishImage)

                mBinding.ivAddDishImage.setImageDrawable(
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

    private fun customItemsDialog(title: String, itemList: List<String>, selection: String){
        // Create dialog in this context
        mCustomListDialog = Dialog(this)
        // Binding dialog_custom_list.xml layout
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        // set binding layout to dialog
        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adaptor = CustomListItemAdaptor(this, itemList, selection)
        binding.rvList.adapter = adaptor
        mCustomListDialog.show()
    }

    fun selectedListItem(item: String, selection: String){
        when(selection){
            Constants.DISH_TYPE->{
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_CATEGORY->{
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME->{
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }

    companion object{
        private val IMAGE_DIRECTORY = "FavDishImages"
    }

}