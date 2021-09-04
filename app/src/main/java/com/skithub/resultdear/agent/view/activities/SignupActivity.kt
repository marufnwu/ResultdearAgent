package com.skithub.resultdear.agent.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.skithub.resultdear.agent.R
import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.databinding.ActivitySignupBinding
import com.skithub.resultdear.agent.utils.LoadingDialog
import com.skithub.resultdear.agent.viewmodel.SignupViewModel
import com.skithub.resultdear.agent.viewmodel.SignupViewModelFactory

class SignupActivity : AppCompatActivity() {
    lateinit var binding : ActivitySignupBinding
    lateinit var viewModel : SignupViewModel
    private lateinit var mAuth : FirebaseAuth
    private var mUser : FirebaseUser? = null
    private var gender: Int = 0
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val factory  = SignupViewModelFactory((application as MyApplication).iRetrofitApiCall)
        viewModel = ViewModelProvider(this, factory)[SignupViewModel::class.java]
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        loadingDialog = LoadingDialog(this)
        initObserver()

        mUser?.let {
            binding.edtEmail.setText(mUser?.email)
        }

        binding.btnSignup.setOnClickListener {
            validateSignUpData()
        }

        binding.radioGrpGender.setOnCheckedChangeListener {
                radioGroup, i -> when(i){
                    R.id.radioBtnMale->{
                        gender = 1
                        return@setOnCheckedChangeListener
                    }
                    R.id.radioBtnFemale->{
                        gender = 2
                        return@setOnCheckedChangeListener
                    }
                }

        }

    }

    private fun initObserver() {
        viewModel.loading.observe(
            this,
             {
                if(it){
                    loadingDialog.show()
                }else{
                    loadingDialog.hide()
                }
            }
        )

        viewModel.serverResponse.observe(
            this,
            {
                it?.let {
                    if(it.error){
                        Toast.makeText(this, it.error_description, Toast.LENGTH_SHORT).show()
                    }else{
                        startActivity(Intent(this, EntryActivity::class.java))
                        finish()
                    }
                }
            }
        )
    }

    private fun validateSignUpData() {
        val name = binding.edtName.text.toString()
        val email = mUser?.email
        val phone = binding.edtPhone.text.toString()
        val age = binding.edtAge.text.toString()

        if(TextUtils.isEmpty(name)){
            binding.edtName.error = "Name Is Required"
            return
        }

        if(TextUtils.isEmpty(email)){
            binding.edtEmail.error = "Email Is Required"
            return
        }

        if(gender==0){
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(age)){
            Toast.makeText(this, "Enter Age", Toast.LENGTH_SHORT).show()
            return
        }


        if (TextUtils.isEmpty(phone) or !phone.length.equals(10)){
            binding.edtPhone.error = "Enter 10 digit phone number"
            return
        }

        viewModel.signUp(name,email!!,age,gender, mUser!!.photoUrl.toString(), phone )

    }

    override fun onStart() {
        super.onStart()
        if(mUser == null){
            startActivity(Intent(this, EntryActivity::class.java))
            finish()
        }
    }
}


