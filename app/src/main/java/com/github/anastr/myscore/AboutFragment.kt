package com.github.anastr.myscore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.anastr.myscore.databinding.FragmentAboutBinding
import com.google.android.material.transition.MaterialFadeThrough

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGithubProfile.setOnClickListener { openUrl("https://github.com/anastr") }
        binding.buttonLinkedIn.setOnClickListener { openUrl("https://linkedin.com/in/anas-altair") }
    }

    private fun openUrl(url: String) =
        startActivity(Intent(Intent.ACTION_VIEW).apply { this.data = Uri.parse(url) })

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
