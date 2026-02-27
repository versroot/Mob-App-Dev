package dk.itu.moapd.x9.gicu

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import dk.itu.moapd.x9.gicu.databinding.FragmentLatestpageBinding


class Latestpage : Fragment() {
    private var _binding: FragmentLatestpageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatestpageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.oldreport.setOnClickListener {
            val message = reports.lastReport

            if (message != null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Report Summary")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("No Report")
                    .setMessage("No report has been submitted yet.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}