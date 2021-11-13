package com.assignment.photoeditor.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.activity.result.contract.ActivityResultContracts

import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.assignment.photoeditor.R
import com.assignment.photoeditor.databinding.FragmentOptionBinding
import com.assignment.photoeditor.sdk29AndUp
import com.assignment.photoeditor.viewmodels.EditorViewModels
import java.io.*


private const val TAG = "OptionFragment"
class OptionFragment : Fragment() {
    private var _binding:FragmentOptionBinding?=null
    private val binding get() = _binding!!
    lateinit var sharedViewModel:EditorViewModels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel=ViewModelProvider(requireActivity())[EditorViewModels::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentOptionBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.getBitmapFromStack.observe(viewLifecycleOwner, Observer {
            binding.editingImage.setImageBitmap(it)
        })
        for (i in 0 until binding.bottomNavigation.menu.size()) {
            binding.bottomNavigation.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigation.setOnItemSelectedListener {item->
            when(item.itemId) {
                R.id.rotate -> {
                    Log.d(TAG, "onViewCreated: kaykay rotate")
                    findNavController().navigate(OptionFragmentDirections.actionOptionFragmentToRotateFragment())
                    true
                }
                R.id.crop -> {
                    Log.d(TAG, "onViewCreated: kaykay crop")
                    findNavController().navigate(OptionFragmentDirections.actionOptionFragmentToCropFragment())
                    true
                }
                R.id.select_another->{
                    Log.d(TAG, "onViewCreated: kaykay select another")
                    findNavController().navigate(OptionFragmentDirections.actionOptionFragmentToHomeFragment())
                    true
                }
                R.id.save_images->{
                    Log.d(TAG, "onViewCreated: kaykay save Images")
                        val imageSaved=saveImage(sharedViewModel.getBitmapFromStack.value!!
                        ,System.currentTimeMillis().toString())
                    if (imageSaved){
                        Toast.makeText(context,getString(R.string.image_saved),Toast.LENGTH_LONG).show()
                    }
                    true
                }
                R.id.undo->{
                    Log.d(TAG, "onViewCreated: undo")
                    if (sharedViewModel.undo()==1){
                        Toast.makeText(context,getString(R.string.no_image),Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun saveImage(bitmap: Bitmap, name: String):Boolean {
        val imageCollection= sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }?:MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cotentValue=ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$name.jpg")
            put(MediaStore.Images.Media.WIDTH,bitmap.width)
            put(MediaStore.Images.Media.HEIGHT,bitmap.height)
        }
        return  try {
            requireActivity().contentResolver.insert(imageCollection,cotentValue)?.also { uri->
                requireActivity().contentResolver.openOutputStream(uri).use{output->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG,95,output)){
                        Log.d(TAG, "saveImage: Coudnt Save Bitmap")
                        throw  IOException("Coudnt Save Exception")
                    }
                }
            }?:throw IOException("Coudnt Create  Media Store Entry")
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }

    }


    private val savePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        binding.editingImage.setImageBitmap(it)
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}

