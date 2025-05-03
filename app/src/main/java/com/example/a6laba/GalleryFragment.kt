package com.example.a6laba

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.a6laba.adapter.ImageAdapter
import com.example.a6laba.data.ImageItem
import com.example.a6laba.databinding.FragmentGalleryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasReadPermission()) {
            loadImages()
        } else {
            requestPermissions()
        }
    }

    private fun hasReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImages()
        }
    }

    private fun loadImages() {
        val imageList = mutableListOf<ImageItem>()
        val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        requireContext().contentResolver.query(
            uriExternal,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                imageList.add(ImageItem(contentUri, name))
            }
        }

        setupRecyclerView(imageList)
    }

    private fun setupRecyclerView(imageList: List<ImageItem>) {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = ImageAdapter(imageList)
        binding.recyclerView.adapter = adapter

        adapter.onItemClick = { imageItem ->
            val action = GalleryFragmentDirections.actionGalleryToDetail(
                imageUri = imageItem.uri.toString(),
                description = imageItem.description
            )
            findNavController().navigate(action)
        }

        adapter.onItemLongClick = { imageItem ->
            showDescriptionDialog(imageItem, adapter)
        }
    }

    private fun showDescriptionDialog(item: ImageItem, adapter: ImageAdapter) {
        val editText = android.widget.EditText(requireContext())
        editText.setText(item.description)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Description")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                item.description = editText.text.toString()
                adapter.updateList(adapter.items) // ✅ Использование геттера
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}