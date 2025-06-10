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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64

import androidx.appcompat.app.AlertDialog
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import java.security.MessageDigest

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProfileSetupBottomSheet.OnProfileSetupCompleteListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showKeyHash()
        // ✅ 시스템 창 침범 방지
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 EmojiCompat 메모리 최적화
        disableEmojiCompat()

        //Jetpack Navigation 연결
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)

        //네비게이션 뷰 관리
        binding.navigationView.setOnItemSelectedListener { item ->
            val navController =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            when (item.itemId) {
                R.id.homeFragment -> {
                    Log.d("MainActivity", "🏠 Home 선택됨")
                    navController?.navigate(item.itemId)
                    true
                }

                R.id.kakaoMapFragment -> {
                    Log.d("MainActivity", "🗺️ KakaoMap 선택됨")
                    navController?.navigate(item.itemId)
                    true
                }

                R.id.myProfileFragment -> {
                    Log.d("MainActivity", "👤 Profile 선택됨")
                    RightSheetDialogFragment().show(supportFragmentManager, "RightSheet")
                    false // Navigation 전환 안함!
                }

                else -> {
                    Log.d("MainActivity", "❓ 알 수 없는 item: ${item.itemId}")
                    false
                }
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

    private fun showKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim()
                Log.d("🔑KeyHash", "현재 기기 키해시: $keyHash")
            }
        } catch (e: Exception) {
            Log.e("🔑KeyHash", "키해시 추출 실패: ${e.message}")
        }
    }

    private fun disableEmojiCompat() {
        try {
            // EmojiCompat 비활성화로 메모리 절약
            val config = BundledEmojiCompatConfig(this)
                .setReplaceAll(false) // 🔥 모든 이모지 교체 비활성화
            EmojiCompat.init(config)

            Log.d("MainActivity", "✅ EmojiCompat 최적화 완료")
        } catch (e: Exception) {
            Log.w("MainActivity", "⚠️ EmojiCompat 비활성화 실패: ${e.message}")
        }
    }

}