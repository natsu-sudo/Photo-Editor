package com.assignment.photoeditor.fragments

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.assignment.photoeditor.R
import com.assignment.photoeditor.databinding.FragmentRotateBinding
import com.assignment.photoeditor.viewmodels.EditorViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "RotateFragment"
class RotateFragment : Fragment() {

    private var _binding:FragmentRotateBinding?=null
    private val binding get() = _binding!!
    lateinit var sharedViewModel: EditorViewModels
    var bitmap:Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel= ViewModelProvider(requireActivity())[EditorViewModels::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentRotateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.getBitmapFromStack.observe(viewLifecycleOwner, Observer {
            binding.rotateImageView.setImageBitmap(it)
            bitmap=it
        })
        binding.bottomNavigationRotation.setOnItemSelectedListener{
            when(it.itemId){
                R.id.rotate_left->{
                    doRotation(-90)
                    true
                }
                R.id.rotate_right->{
                    doRotation(90)
                    true
                }
                R.id.save_images_rotation->{
                    saveImageAndGoBack()
                    true
                }
                else->{
                    false
                }
            }
        }


    }

    private fun saveImageAndGoBack() {
        sharedViewModel.addBitmap(bitmap!!)
        findNavController().navigate(RotateFragmentDirections.actionRotateFragmentToOptionFragment())

    }

    private fun doRotation(degree: Int) {
        Log.d(TAG, "doRotation: $degree")
        lifecycleScope.launch {
            withContext(Dispatchers.Default){
                val matrix = Matrix()
                matrix.postRotate(degree.toFloat())
                bitmap?.let {
                    val scaledBitmap = Bitmap.createScaledBitmap(it, it.width, it.height, true)
                    val rotatedBitmap = Bitmap.createBitmap(
                        scaledBitmap,
                        0,
                        0,
                        scaledBitmap.width,
                        scaledBitmap.height,
                        matrix,
                        true
                    )
                    withContext(Dispatchers.Main){
                        binding.rotateImageView.setImageBitmap(rotatedBitmap)
                        bitmap=rotatedBitmap
                    }
                }
        }

        }

    }


}