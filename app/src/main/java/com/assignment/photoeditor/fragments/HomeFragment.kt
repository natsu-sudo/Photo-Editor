package com.assignment.photoeditor.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.assignment.photoeditor.BuildConfig
import com.assignment.photoeditor.R
import com.assignment.photoeditor.createFiles
import com.assignment.photoeditor.databinding.FragmentHomeBinding
import com.assignment.photoeditor.viewmodels.EditorViewModels
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "HomeFragment"
class HomeFragment : Fragment() {
    private  var selectedPhotoPath: String?=null
    private var _binding: FragmentHomeBinding? =null
    private val binding get() = _binding!!
    lateinit var sharedViewModel: EditorViewModels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel= ViewModelProvider(requireActivity())[EditorViewModels::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding.getImage.setOnClickListener {
            val items = arrayOf(getString(R.string.from_camera), getString(R.string.from_gallary), getString(R.string.cancel))
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(view.context, R.style.Theme_AppCompat_Dialog_Alert)
            builder.setTitle(getString(R.string.add_photo))
            builder.setItems(items) { dialogInterface, i ->
                when {
                    items[i] == getString(R.string.from_camera) -> {
                        clickPhotoAfterPermission(binding.root)
                    }
                    items[i] == getString(R.string.from_gallary) -> {
                        pickPhotoAfterPermission()
                    }
                    else -> {
                        dialogInterface.dismiss()
                    }
                }
            }
            builder.show()
        }
    }

    private fun pickPhotoAfterPermission() {
        //if Permission Granted Pick Photo
        if (ActivityCompat.checkSelfPermission(binding.root.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            pickPhoto()
        } else {//Ask for Permission
            requestReadRightPermission.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    }

    private fun clickPhotoAfterPermission(it: View) {
        //if Permission Granted Click Photo
        if (ActivityCompat.checkSelfPermission(it.context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            clickPhoto()
        } else {//Ask for Permission
            Log.d(TAG, "clickPhotoAfterPermission: ")
            requestCameraPermission()
        }
    }

    private fun clickPhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePickuteIntent->
            takePickuteIntent.resolveActivity(requireActivity().packageManager).also {
                val photoFile: File?=try {
                    createFiles(requireContext(), Environment.DIRECTORY_PICTURES, "jpg")
                }catch (ex: IOException){
                    Toast.makeText(
                        context,
                        getString(R.string.error, ex.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                    null
                }
                photoFile?.also {
                    selectedPhotoPath=it.absolutePath
                    Log.d(TAG, "clickPhoto: " + it.absolutePath)
                    val photoUri: Uri = FileProvider.getUriForFile(
                        requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    takePickuteIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    cameraCaptureLauncher.launch(takePickuteIntent)

                }
            }
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                clickPhoto()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d(TAG, "Permission Granted")
                Snackbar.make(
                    binding.getImage,
                    getString(R.string.permission_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Ok") {

                    }
                    .show()
            }
        }

    private val requestReadRightPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                pickPhoto()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d(TAG, "Permission Granted")
                Snackbar.make(
                    binding.getImage,
                    getString(R.string.permission_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Ok") {

                    }
                    .show()
            }
        }

    private val cameraCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, ": cameraCaptureLauncher " + result.resultCode)
            val imageFile=File(selectedPhotoPath)
            val bitmap = BitmapFactory.decodeFile(imageFile.path)
            sharedViewModel.addBitmap(bitmap)
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOptionFragment())

        }
    }


    private fun pickPhoto() {
        //checking for any app which we can use to select file from app
        val pickPhotoIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
        pickPhotoIntent.resolveActivity(requireActivity().packageManager)?.also {
            takePictureFromFile.launch(pickPhotoIntent)
        }
    }

    private val takePictureFromFile =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //Here What we are doing is That the we are creating a file and after that
            //the Image which we have selected through the App we are using its uri
            result.data?.data.also { uri->
                val photo:File?=try {
                    createFiles(requireContext(), Environment.DIRECTORY_PICTURES, "jpg")
                }catch (ex: IOException){
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    null
                }
                photo?.also {
                    try {
                        //creating resolver to resolve the uri
                        val resolver= activity?.applicationContext?.contentResolver
                        if (uri != null) {
                            //converting selected file to Stream
                            resolver?.openInputStream(uri).use { stream->
                                //copying selected file using stream
                                val output= FileOutputStream(photo)
                                stream!!.copyTo(output)
                            }
                            val bitmap = BitmapFactory.decodeFile(photo.path)
                            sharedViewModel.addBitmap(bitmap)
                            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToOptionFragment())
                        }
                    }catch (ex: FileNotFoundException){
                        ex.printStackTrace()
                    }catch (ex: IOException){
                        ex.printStackTrace()
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: kaykay")
        super.onDestroyView()
    }

}