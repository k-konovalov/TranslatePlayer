package ru.konovalovk.translateplayer.ui.export

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import ru.konovalovk.repository.db.AppDatabase
import ru.konovalovk.repository.db.entity.Library
import ru.konovalovk.translateplayer.R

class ChoiceDestinationFragment: Fragment(R.layout.fragment_choice_destination) {
    val viewModel: ChoiceDestinationViewModel by viewModels()

    var words = listOf<Library>()
    val mbAnki by lazy {requireView().findViewById<MaterialButton>(R.id.mbAnki)}
    val mbLinguaLeo by lazy {requireView().findViewById<MaterialButton>(R.id.mbLinguaLeo)}
    val mbTxt by lazy {requireView().findViewById<MaterialButton>(R.id.mbTxt)}
    val mbPdf by lazy {requireView().findViewById<MaterialButton>(R.id.mbPdf)}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        words = when(arguments?.getString("Export_Word_Mode")){
            "All" -> {
                AppDatabase.instance.libraryDAO.getAll()
            }
            else -> listOf()
        }
        initOnClickListeners()
    }

    fun initOnClickListeners(){
        mbAnki.setOnClickListener {
            viewModel.sendToAnki()
        }
        mbLinguaLeo.setOnClickListener {
            viewModel.sendToLinguaLeo()
        }
        mbTxt.setOnClickListener {
            val path = viewModel.sendToTxt(requireContext(), words)
            if(path.isNotEmpty()) Toast.makeText(requireContext(),"Create txt file in\n$path", Toast.LENGTH_LONG).show()
        }
        mbPdf.setOnClickListener {
            viewModel.sendToPdf()
        }
    }
}