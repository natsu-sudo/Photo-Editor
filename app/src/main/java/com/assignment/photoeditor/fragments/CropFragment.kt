package com.assignment.photoeditor.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.assignment.photoeditor.R
import com.assignment.photoeditor.databinding.FragmentCropBinding
import com.assignment.photoeditor.databinding.FragmentOptionBinding
import com.assignment.photoeditor.viewmodels.EditorViewModels
import com.theartofdev.edmodo.cropper.CropImageView


class CropFragment : Fragment() {

    private var _binding:FragmentCropBinding?=null
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
        _binding= FragmentCropBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.getBitmapFromStack.observe(viewLifecycleOwner, Observer {
            binding.cropImageView.setImageBitmap(it)
            binding.cropImageView.setFixedAspectRatio(false)
            binding.cropImageView.guidelines = CropImageView.Guidelines.ON
        })
        binding.saveCropImage.setOnClickListener {
            sharedViewModel.addBitmap(binding.cropImageView.croppedImage)
            Toast.makeText(context, getString(R.string.cropped), Toast.LENGTH_SHORT).show()
            findNavController().navigate(CropFragmentDirections.actionCropFragmentToOptionFragment())
        }
    }


}