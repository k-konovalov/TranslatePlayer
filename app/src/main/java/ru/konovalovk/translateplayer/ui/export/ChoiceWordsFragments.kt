package ru.konovalovk.translateplayer.ui.export

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import ru.konovalovk.translateplayer.R

class ChoiceWordsFragments: Fragment(R.layout.fragment_choice_words) {
    val rgChoiceWordsOptions by lazy {requireView().findViewById<RadioGroup>(R.id.rgChoiceWordsOptions)}
    val mbChoiceWordsNext by lazy {requireView().findViewById<MaterialButton>(R.id.mbChoiceWordsNext)}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rgChoiceWordsOptions.setOnCheckedChangeListener { group, checkedId ->

        }
        mbChoiceWordsNext.setOnClickListener {
            val bundle = Bundle().apply {
                putString("Export_Word_Mode","All")
            }
            findNavController().navigate(R.id.action_choiceWordsFragments_to_choiceDestinationFragment, bundle)
        }
    }
}