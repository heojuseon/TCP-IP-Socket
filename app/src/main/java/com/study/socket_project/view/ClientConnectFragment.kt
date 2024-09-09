package com.study.socket_project.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.study.socket_project.databinding.FragmentClientconnectBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread

class ClientConnectFragment : Fragment() {
    private lateinit var binding: FragmentClientconnectBinding

    private var socket: Socket? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientconnectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ip_arg = arguments?.getString("ip")
        val port_arg = arguments?.getString("port")
        Log.d("!@!@", "ip: $ip_arg | port: $port_arg")

        thread {
            try {
                //server 연결
                socket = Socket(ip_arg.toString(), Integer.parseInt(port_arg!!))
            } catch (e: Exception){
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "서버 연결 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        }

        binding.btnToServer.setOnClickListener {
            val message = binding.toServer.text.toString()
            // 메시지가 비어있는지 체크
            if (message.isNotEmpty()) {
                    sendToServer(message)
            } else {
                Toast.makeText(requireContext(), "메시지를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendToServer(message: String) {
        thread {
            try {
                //데이터 송신(== server 로 응답 전송)
                val outputStream = socket?.getOutputStream()
                val writer = DataOutputStream(outputStream)
                writer.writeUTF(message)
                writer.flush()

                //stream 추출
                val inputStream = socket?.getInputStream()
                val reader = DataInputStream(inputStream)

                //server 로 부터 메시지 수신
                val receivedMessage = reader.readUTF()
                requireActivity().runOnUiThread {
                    binding.fromServer.text = receivedMessage
                }

//                //client 로 응답을 보내면 바로 socket 종료
//                socket?.close()

            } catch (e: Exception){
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "메시지 전송 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("!@!@", "ClientConnectFragment onDestroyView")
        socket?.close()
    }
}