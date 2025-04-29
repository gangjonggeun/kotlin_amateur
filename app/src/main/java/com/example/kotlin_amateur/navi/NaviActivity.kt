package com.example.kotlin_amateur.navi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.kotlin_amateur.R
import com.example.kotlin_amateur.databinding.ActivityNaviBinding

private const val TAG_HOME = "home_fragment"
private const val TAG_CHAT = "chat_fragment"
private const val TAG_MY_HOME = "profile_fragment"

class NaviActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Jetpack Navigation 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)

        //네비게이션 뷰 관리
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.chatFragment, R.id.myProfileFragment -> {
                    // 홈, 채팅, 프로필 프래그먼트일 때는 바텀 네비게이션 보이게
                    binding.navigationView.visibility = View.VISIBLE
                }
                else -> {
                    // 그 외에는 숨기기
                    binding.navigationView.visibility = View.GONE
                }
            }
        }
    }
}