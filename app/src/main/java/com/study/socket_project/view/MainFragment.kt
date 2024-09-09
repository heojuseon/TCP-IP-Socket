package com.study.socket_project.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.study.socket_project.R
import com.study.socket_project.databinding.FragmentMainBinding


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnServer.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_serverFragment)
        }

        binding.btnClient.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_clientFragment)
        }
    }
}