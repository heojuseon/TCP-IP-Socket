package com.study.socket_project.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.study.socket_project.R
import com.study.socket_project.databinding.FragmentServerBinding
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class ServerFragment : Fragment() {
    private lateinit var binding: FragmentServerBinding

    private var server: ServerSocket? = null
    private var isServerRunning = false //버튼 처리
    private var isServerStopped = false
    private var serverThread: Thread? = null

    private var writer: DataOutputStream? = null
    private var outputStream: OutputStream? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //SERVER_SOCKET 구현
        binding.btnServerRunning.setOnClickListener {
            if (isServerRunning) {
                stopServer()
            } else {
                startServer()
            }
        }
    }

    private fun startServer() {
        isServerStopped = false
        serverThread = Thread {
            try {
                // 사용할 포트 번호와 server 객체 생성
                val port = 8090
                server = ServerSocket(port)
                isServerRunning = true

                // UI 스레드에서 버튼 텍스트 및 상태 변경
                requireActivity().runOnUiThread {
                    binding.btnServerRunning.text = "Stop_Server"
                }

                // 서버가 종료될 때까지 요청 대기
                while (!isServerStopped) {
                    try {
                        // 클라이언트 연결 대기
                        val socket = server?.accept()

                        // 클라이언트가 연결되었을 경우 UI 업데이트
                        requireActivity().runOnUiThread {
                            binding.serverState.text = "연결됨"
                        }

                        // 클라이언트가 연결되었을 때 새로운 스레드에서 처리(각 클라이언트 연결마다 새로운 스레드에서 실행)
                        Thread {
                            handleClient(socket)
                        }.start()

                    } catch (e: Exception) {
                        if (isServerStopped) {
                            break // 서버가 종료된 경우 루프 종료
                        } else {
                            e.printStackTrace() // 다른 예외는 로그 출력
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    binding.serverState.text = "서버 오류 발생"
                }
            }
        }
        // 서버 스레드를 시작
        serverThread?.start()
    }

    private fun handleClient(socket: Socket?) {
        try {
            requireActivity().runOnUiThread {
                binding.serverState.text = "연결됨"
            }

            val inputStream = socket?.getInputStream()
//            val outputStream = socket?.getOutputStream()
            outputStream = socket?.getOutputStream()
            val reader = DataInputStream(inputStream)
//            val writer = DataOutputStream(outputStream)
            writer = DataOutputStream(outputStream)

            //socket 이 이벤트에 대한 응답을 지속적으로 받기 위한 로직
            //클라이언트와 통신이 끊어질 때까지 반복적으로 데이터를 주고받음
            while (!socket?.isClosed!!) {
                val receivedMessage = reader.readUTF()
                requireActivity().runOnUiThread {
                    //기존 텍스트에 새로운 메시지 추가
                    val currentText = binding.fromClient.text.toString()
                    val formatText = getString(R.string.client_message_format, currentText, receivedMessage)
                    binding.fromClient.text = formatText
//                    binding.fromClient.text = receivedMessage
                }

                if (receivedMessage.equals("hi")) {
                    writer?.writeUTF("client 로 응답 전송: hello")
                    writer?.flush()
                } else if (receivedMessage.equals("photo")) {
                    //권한 확인 후 사진 전송
                    checkPermissionPhoto()
                } else {
                    writer?.writeUTF("client 로 응답 전송: $receivedMessage")
                    writer?.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            /**
             * 이 블록에는 try 블록에서 일어난 일에 관계없이 무조건 실행될 코드가 위치한다.
             * 이 코드는 try 블록이 어떻게든 종료되면 실행된다.
             * try 블록이 종료되는 상황은 다음과 같다.
             * 1) 정상적으로 블록의 끝에 도달했을 때
             * 2) break, continue 또는 return 문에 의해서
             * 3) 예외가 발생했지만 catch 절에서 처리했을 때
             * 4) 예외가 발생했고 그것이 잡히지 않은 채 퍼져나갈 때
             */
            Log.d("!@!@_ServerFragment", "finally 블록 수행")
            socket?.close() //통신이 완료되면 소켓 닫음
        }
    }

    private fun checkPermissionPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {    //TIRAMISU 이상의 버전
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                sendPhotoToClient()
            } else {
                // 권한 요청
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Android 12 이하인 경우 READ_EXTERNAL_STORAGE 권한을 확인
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                sendPhotoToClient()
            } else {
                // 권한 요청
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun sendPhotoToClient() {
        // 사진 파일 경로 설정 (서버에 저장된 사진 파일 경로)
        val photoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "FineByMe_20240701_153257232.jpg"
        )

        if (photoFile.exists()) {
            Log.d("!@!@_ServerFragment", "fileExists: $photoFile")
            //파일 크기 정보를 먼저 전송
            writer?.writeLong(photoFile.length())
            writer?.writeUTF("client 로 응답 전송: 사진 전송 완료")
            writer?.flush()

            // 파일 데이터를 전송
            val fileInputStream = FileInputStream(photoFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream?.write(buffer, 0, bytesRead)
            }

            // 파일 전송이 끝나면 flush() 호출
            outputStream?.flush()
            fileInputStream.close()

//            writer?.writeUTF("client 로 응답 전송: 사진 전송 완료")
//            writer?.flush()
            Log.d("!@!@_ServerFragment", "사진 전송 완료")
        } else {
            Log.d("!@!@_ServerFragment", "사진 정보 없음")
        }
    }

    private fun stopServer() {
        isServerStopped = true
        try {
            // 서버 소켓을 닫아 서버 종료
            server?.close()

            // UI 스레드에서 버튼 텍스트 및 상태 변경
            requireActivity().runOnUiThread {
                binding.btnServerRunning.text = "Start_Server"
            }

            // 서버 실행 상태 업데이트
            isServerRunning = false
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                binding.serverState.text = "서버 종료 중 오류 발생"
            }
        }
    }


    // 권한 요청 콜백 설정
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 사진 전송을 처리
            sendPhotoToClient()
        } else {
            // 권한이 거부된 경우 처리
            Toast.makeText(requireContext(), "이미지 읽기 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}