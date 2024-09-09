package com.study.socket_project.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.study.socket_project.R
import com.study.socket_project.databinding.FragmentClientBinding

class ClientFragment : Fragment() {
    private lateinit var binding: FragmentClientBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConnect.setOnClickListener {

            //bundle 로 데이터 넘기기
            val bundle = Bundle().apply {
                putString("ip", binding.ip.text.toString())
                putString("port", binding.port.text.toString())
            }

            if (binding.ip.text.isNullOrEmpty() || binding.port.text.isNullOrEmpty()){
                Toast.makeText(requireContext(), "IP 주소 또는 포트 번호가 유효하지 않습니다.", Toast.LENGTH_LONG).show()
            } else {
                findNavController().navigate(
                    R.id.action_clientFragment_to_clientConnectFragment, bundle
                )
            }

        }
    }
}