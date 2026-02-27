package dk.itu.moapd.x9.gicu

import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.x9.gicu.databinding.ActivityTrafficlogBinding

class TrafficlogFragment : Fragment(R.layout.activity_trafficlog) {

    private var _binding: ActivityTrafficlogBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivityTrafficlogBinding.bind(view)

        binding.dateEditText.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select report date")
                .build()

            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(Date(selection))
                binding.dateEditText.setText(formattedDate)
            }
        }

        val incidentTypes = listOf(
            "Fire",
            "Theft",
            "Terrorism",
            "Other"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            incidentTypes
        )

        binding.incidentType.setAdapter(adapter)
        binding.incidentType.keyListener = null

        restoreState(savedInstanceState)

        binding.submitButton.setOnClickListener {
            val title = binding.titleText.editText?.text.toString()
            val loc = binding.locText.editText?.text.toString()
            val date = binding.dateText.editText?.text.toString()
            val type = binding.incidentType.text.toString()
            val desc = binding.descText.editText?.text.toString()
            val priority = binding.priorSlider.value

            val message = """
                Title: $title
                Location: $loc
                Date: $date
                Incident type: $type
                Description: $desc
                Priority: $priority
            """.trimIndent()

            reports.lastReport = message
            Toast.makeText(context, "Report received", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        unsubmitted.title = binding.titleText.editText?.text.toString()
        unsubmitted.loc = binding.locText.editText?.text.toString()
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        binding.titleText.editText?.setText(unsubmitted.title)
        binding.locText.editText?.setText(unsubmitted.loc)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}