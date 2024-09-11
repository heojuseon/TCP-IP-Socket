package com.study.socket_project.view

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.study.socket_project.databinding.FragmentClientconnectBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileOutputStream
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
        Log.d("!@!@_ClientConnectFragment", "ip: $ip_arg | port: $port_arg")

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

                if (message == "photo"){    //서버로 부터 메시지 수신
                    receivePhotoFromServer()
                } else {    //일반 텍스트 수신
                    //stream 추출
                    val inputStream = socket?.getInputStream()
                    val reader = DataInputStream(inputStream)
                    //server 로 부터 메시지 수신
                    val receivedMessage = reader.readUTF()
                    requireActivity().runOnUiThread {
                        binding.fromServer.text = receivedMessage
                    }
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

    private fun receivePhotoFromServer() {
        try {
            val inputStream = socket?.getInputStream()
            val reader = DataInputStream(inputStream)

            //서버에서 파일크기 수신
            val fileSize = reader.readLong()
            //server 로 부터 메시지 수신
            val receivedMessage = reader.readUTF()
            requireActivity().runOnUiThread {
                binding.fromServer.text = receivedMessage
            }
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0L

            // 사진 파일을 저장할 경로 설정
//            val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/Finebyme_20240627_114904914.jpg"
//            val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/FineByMe_20240701_153257232.jpg"
            //동영상 파일
            val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/20240911_142144.mp4"
            val fileOutputStream = FileOutputStream(filePath)

            //서버에서 파일 데이터 수신
            while (totalBytesRead < fileSize){
                bytesRead = inputStream?.read(buffer) ?: -1
                if (bytesRead == -1) break  // 스트림 끝일 때 루프 종료

                fileOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                //파일 다운로드 진행률 계산
                val progress = ((totalBytesRead * 100) / fileSize).toInt()
                Log.d("!@!@_ClientConnectFragment", "progress: $progress")
                //UI 스레드에서 progressbar 업데이트
//                requireActivity().runOnUiThread {
//                    binding.downLoadProgressBar.visibility = View.VISIBLE
//                    binding.downLoadProgressBar.progress = progress
//                }
                requireActivity().runOnUiThread {
                    if (progress == 100){
                        binding.downLoadProgressBar.visibility = View.GONE
                        binding.downLoadProgressBar.progress = progress
                    } else {
                        binding.downLoadProgressBar.visibility = View.VISIBLE
                        binding.downLoadProgressBar.progress = progress
                    }
                }
            }

            fileOutputStream.close()
            Log.d("!@!@_ClientConnectFragment", "사진 수신 완료")
        } catch (e: Exception){
            Log.d("!@!@_ClientConnectFragment", "사진 수신 실패: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("!@!@_ClientConnectFragment", "ClientConnectFragment onDestroyView")
        socket?.close()
    }
}