package com.skithub.resultdear.agent.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.databinding.ActivitySplashBinding
import com.skithub.resultdear.agent.viewmodel.SplashActivityViewModel
import com.skithub.resultdear.agent.viewmodel.SplashActivityViewModelFactory

class SplashActivity : AppCompatActivity() {

    lateinit var binding : ActivitySplashBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var splashViewModel : SplashActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val viewModelFactory  = SplashActivityViewModelFactory((application as MyApplication).iRetrofitApiCall)
        splashViewModel = ViewModelProvider(this, viewModelFactory)[SplashActivityViewModel::class.java]
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        initObserver();
    }

    private fun initObserver() {
        splashViewModel.userLogin.observe(
            this,
            {
                if (it!=null){
                    Log.d("Messgae", it.error_description!!)
                }

            }
        )
    }

    override fun onStart() {
        super.onStart()
        if (mAuth==null){
            //user not logged go to login page
            startActivity(Intent(this, EntryActivity::class.java))
            finish()
        }else{
            //check user exits or not
            splashViewModel.isUserExits()
        }
    }
}