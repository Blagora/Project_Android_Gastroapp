package com.example.gastroapp.presentation.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gastroapp.databinding.FragmentProfileBinding
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
        setupProfileImage()
    }

    private fun setupListeners() {
        binding.finishButton.setOnClickListener {
            // TODO: Implementar guardado de cambios
        }

        binding.profileImageView.setOnClickListener {
            openImagePicker()
        }
    }

    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            with(binding) {
                nameText.text = userData.name
                nameEditText.setText(userData.name)
                emailEditText.setText(userData.email)
                phoneEditText.setText(userData.phone)
                addressEditText.setText(userData.address)
                locationText.text = userData.address
            }
        }
    }

    private fun setupProfileImage() {
        binding.profileImageView.shapeAppearanceModel = binding.profileImageView
            .shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(ShapeAppearanceModel.PILL)
            .build()
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { updateProfileImage(it) }
    }

    private fun openImagePicker() {
        getContent.launch("image/*")
    }

    private fun updateProfileImage(uri: Uri) {
        binding.profileImageView.setImageURI(uri)
        viewModel.updateProfileImage(uri.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
