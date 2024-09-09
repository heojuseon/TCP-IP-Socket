package com.study.socket_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.study.socket_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //NavController 명시: NavHost 에 어떤 화면을 띄울 것인지 컨트롤 하는 역할을 수행
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navigation graph 가 작동할 위치에 set
        navController = binding.fragmentContainerView.getFragment<NavHostFragment>().navController
    }
}