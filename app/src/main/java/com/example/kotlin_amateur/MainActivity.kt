package com.example.kotlin_amateur


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kotlin_amateur.databinding.ActivityMainBinding
import com.example.kotlin_amateur.navi.profile.RightSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.example.kotlin_amateur.login.ProfileSetupBottomSheet
import com.example.kotlin_amateur.viewmodel.ProfileViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProfileSetupBottomSheet.OnProfileSetupCompleteListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 시스템 창 침범 방지
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Jetpack Navigation 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)

        //네비게이션 뷰 관리
        binding.navigationView.setOnItemSelectedListener { item ->
            val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            when (item.itemId) {
                R.id.homeFragment, R.id.kakaoMapFragment -> {
                    navController?.navigate(item.itemId)
                    true
                }
                R.id.myProfileFragment -> {
                    RightSheetDialogFragment().show(supportFragmentManager, "RightSheet")
                    false // ❗ Navigation 전환 안함!
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.kakaoMapFragment -> {
                    binding.navigationView.visibility = View.VISIBLE
                }
                else -> {
                    binding.navigationView.visibility = View.GONE
                }
            }
        }
    }

    override fun onProfileSetupComplete() {
        Log.d("MainActivity", "🔥 프로필 설정 완료")

        // 프로필 업데이트 브로드캐스트 전송
        val intent = Intent("PROFILE_UPDATED")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        Toast.makeText(this, "프로필이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
    }
}