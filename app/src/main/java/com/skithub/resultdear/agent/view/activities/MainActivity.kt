package com.skithub.resultdear.agent.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.skithub.resultdear.agent.R
import com.skithub.resultdear.agent.databinding.ActivityMainBinding
import com.skithub.resultdear.agent.databinding.ActivityUserListBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llReferUser.setOnClickListener{
            startActivity(Intent(this, UserListActivity::class.java))
        }
         binding.llClaim.setOnClickListener{
             Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show()
                }


    }
}