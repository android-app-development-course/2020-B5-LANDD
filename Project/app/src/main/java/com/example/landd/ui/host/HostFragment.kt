package com.example.landd.ui.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.landd.R

class HostFragment : Fragment() {

    private lateinit var hostViewModel: HostViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        hostViewModel =
                ViewModelProvider(this).get(HostViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_host, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        hostViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}