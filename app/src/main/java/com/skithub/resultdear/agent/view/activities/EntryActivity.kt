package com.skithub.resultdear.agent.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.skithub.resultdear.agent.R
import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.databinding.ActivityEntryBinding
import com.skithub.resultdear.agent.model.GmailInfo
import com.skithub.resultdear.agent.utils.Constant
import com.skithub.resultdear.agent.utils.LoadingDialog
import com.skithub.resultdear.agent.viewmodel.EntryActivityViewModel
import com.skithub.resultdear.agent.viewmodel.EntryViewModelFactory
import io.paperdb.Paper

class EntryActivity : AppCompatActivity() {
    val TAG:String = "EntryActivity"
    private val RC_SIGN_IN: Int = 10001
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding : ActivityEntryBinding
    var mAuth: FirebaseAuth? = null
    lateinit var viewModel:EntryActivityViewModel
    lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)

        val factory = EntryViewModelFactory((application as MyApplication).iRetrofitApiCall)
        viewModel = ViewModelProvider(this, factory)[EntryActivityViewModel::class.java]

        setContentView(binding.root)
        initView()
        intObserver()
        mAuth = FirebaseAuth.getInstance()
        Paper.init(this);


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnSignin.setOnClickListener {
            gmailSignIn();
        }
    }

    private fun initView() {
        loadingDialog = LoadingDialog(this)
    }

    private fun intObserver() {
        viewModel.loading.observe(
            this,
            Observer {
                if(it){
                    loadingDialog.show()
                }else{
                    loadingDialog.hide()
                }
            }
        )

        viewModel.userLogin.observe(
            this,
            Observer{
                loadingDialog.hide()
                it?.let {
                    if(it.isError){
                        Toast.makeText(this, it.error_description!!, Toast.LENGTH_SHORT).show()
                    }else{
                        if (it.accountExits){
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }else{
                            startActivity(Intent(this, SignupActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        loadingDialog.show()
        if (mAuth?.currentUser==null){
            //user not logged go to login page
            loadingDialog.hide()
            Toast.makeText(this, getString(R.string.login_message), Toast.LENGTH_SHORT).show()
        }else{
            //check user exits or not
            gmailSignIn()
        }
    }

    private fun checkUser() {
        viewModel.isUserExits()
    }

    private fun gmailSignIn() {
        loadingDialog.show()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                loadingDialog.hide()
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser

                    user.let {
                        user!!.getIdToken(true).addOnCompleteListener {
                            val access_token = it.result.token!!

                            val gmailInfo = GmailInfo(access_token, user.uid, user.email!!)

                            Paper.book().write(Constant.GMAILINFO, gmailInfo)

                            checkUser()

                        }
                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                }
            }
    }
}