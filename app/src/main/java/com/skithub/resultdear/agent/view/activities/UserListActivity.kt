package com.skithub.resultdear.agent.view.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.agent.application.MyApplication
import com.skithub.resultdear.agent.databinding.ActivityUserListBinding
import com.skithub.resultdear.agent.databinding.DialogPackageListBinding
import com.skithub.resultdear.agent.databinding.DialogUserInfoBinding
import com.skithub.resultdear.agent.model.Package
import com.skithub.resultdear.agent.model.ReferUser
import com.skithub.resultdear.agent.model.Transaction
import com.skithub.resultdear.agent.model.response.PlanListResponse
import com.skithub.resultdear.agent.model.response.PlanPackagesResponse
import com.skithub.resultdear.agent.model.response.PlanTransactionResponse
import com.skithub.resultdear.agent.model.response.ServerResponse
import com.skithub.resultdear.agent.utils.LoadingDialog
import com.skithub.resultdear.agent.view.adapter.PackageListAdapter
import com.skithub.resultdear.agent.view.adapter.ServiceItemAdapter
import com.skithub.resultdear.agent.view.adapter.UserListAdapter
import com.skithub.resultdear.agent.viewmodel.UserListViewFactory
import com.skithub.resultdear.agent.viewmodel.UserListViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

import java.util.*
import kotlin.collections.ArrayList

class UserListActivity : AppCompatActivity() {
    private var lastOrderRef: String? = null
    lateinit var binding: ActivityUserListBinding
    lateinit var viewModel:UserListViewModel
    lateinit var loadingDialog: LoadingDialog
    lateinit var userListAdapter : UserListAdapter
    var referUserList : List<ReferUser> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        val factory = UserListViewFactory((application as MyApplication).iRetrofitApiCall)
        viewModel = ViewModelProvider(this, factory)[UserListViewModel::class.java]

        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        intObserver()

        binding.rvReferList.layoutManager = LinearLayoutManager(this)
        binding.rvReferList.setHasFixedSize(true)



        getReferList()

    }

    private fun getReferList() {
        viewModel.getAllReferUser()
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

        viewModel.referUserResponse.observe(
            this,
            Observer{
                it?.let {
                    if(it.error!!){
                        Toast.makeText(this, it.error_description!!, Toast.LENGTH_SHORT).show()
                    }else{
                        it.users?.let {listOfRefer->
                            userListAdapter = UserListAdapter(listOfRefer, this)
                            binding.rvReferList.adapter = userListAdapter
                            userListAdapter.onInfoClickListener = {
                                it.let {
                                    showUserInfoDialog(it)
                                }
                            }

                        }
                    }
                }
            }
        )
    }

    private fun showUserInfoDialog(user: ReferUser) {
        loadingDialog.show()
        val dialog = Dialog(this)

        val binding: DialogUserInfoBinding =
            DialogUserInfoBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        binding.tvPhone.text = user.phone!!
        val rvPlan:RecyclerView = binding.rvServicePlan
        rvPlan.layoutManager = LinearLayoutManager(this)
        rvPlan.setHasFixedSize(true)


        (application as MyApplication).iRetrofitApiCall
            .getUserPlan(user.id!!)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSingleObserver<PlanListResponse>(){
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onSuccess(res: PlanListResponse?) {
                        dialog.show()
                        res?.let {
                            if(it.error){
                                Toast.makeText(this@UserListActivity, "${it.error_description}", Toast.LENGTH_SHORT).show()
                            }
                            val serviceItemList  = ArrayList<ServiceItem>()
                            for (item in res.planList!!){
                                val service = item.service
                                val licence = item.lisence

                                service?.let { service ->

                                    var expire :String? = null

                                    licence?.expireDate?.let {
                                        expire = getDate(licence.expireDate.toLong(), "dd-MM-yy")
                                    }

                                    serviceItemList.add(ServiceItem(
                                        service.id!!,
                                        service.name!!,
                                        service.price!!,
                                        expire,
                                        licence?.status!!)
                                    )

                                }


                            }
                            val planAdapter: ServiceItemAdapter = ServiceItemAdapter(serviceItemList, this@UserListActivity)
                            planAdapter.onItemClickListener = {
                                showPackageList(it, user.id)
                            }
                            rvPlan.adapter = planAdapter

                        }

                        loadingDialog.hide()
                    }

                    override fun onError(e: Throwable?) {
                        loadingDialog.hide()
                    }

                }
            )



    }

    private fun showPackageList(servicePlan: ServiceItem, userId:String) {
        loadingDialog.show()
        val dialog = Dialog(this)

        val binding: DialogPackageListBinding =
            DialogPackageListBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        binding.tvPlan.text = "${servicePlan.name} Packages"
        binding.rvServicePlan.layoutManager = LinearLayoutManager(this)
        binding.rvServicePlan.setHasFixedSize(true)

        (application as MyApplication).iRetrofitApiCall
            .getPlanPackages(servicePlan.id)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSingleObserver<PlanPackagesResponse>(){
                    override fun onSuccess(res: PlanPackagesResponse?) {
                        loadingDialog.hide()
                        res?.let {
                            if (res.error){
                                Toast.makeText(this@UserListActivity, res.error_description, Toast.LENGTH_SHORT).show()
                                return
                            }

                            var packageListAdapter = it.packages?.let { packList ->
                                PackageListAdapter(
                                    packList, servicePlan.name, this@UserListActivity)
                            }

                            packageListAdapter?.onBuyClickListener = {
                                buyPackage(it, userId)
                            }

                            binding.rvServicePlan.adapter = packageListAdapter
                        }
                    }

                    override fun onError(e: Throwable?) {
                        loadingDialog.hide()
                    }

                }
            )

        dialog.show()
    }

    private fun buyPackage(pack: Package, userId: String) {
        loadingDialog.show()
        (application as MyApplication).iRetrofitApiCall
            .getPlanTransaction(userId, pack.id!!)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSingleObserver<PlanTransactionResponse>(){
                    override fun onSuccess(res: PlanTransactionResponse?) {
                        res?.let {
                            if(res.error){
                                Toast.makeText(this@UserListActivity, res.error_description, Toast.LENGTH_SHORT).show()
                                return
                            }
                            res.transaction?.let {
                                startUpiIntentPayment(transaction = res.transaction)
                            }
                        }
                    }

                    override fun onError(e: Throwable?) {
                        loadingDialog.hide()
                    }

                }
            )
    }

    private fun startUpiIntentPayment(transaction: Transaction) {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", "Try3x@apl") // virtual ID
            //.appendQueryParameter("pa", "Q98610597@ybl")       // virtual ID
            .appendQueryParameter("pn", "Try3x App") // name
            //.appendQueryParameter("mc", "022552")          // optional
            .appendQueryParameter("tr", transaction.transactionRef) // optional
            .appendQueryParameter(
                "tn",
                transaction.transactionRef
            ) // any note about payment
            .appendQueryParameter("am", transaction.price.toString()) // amount
            .appendQueryParameter("cu", "INR") // currency
            //.appendQueryParameter("url", "https://try3x.xyz/api/upi/upi.paymentCallback.php?orderid="+buyCoinTransResponse.transaction_id)       // optional
            .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri

        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
        if (null != chooser.resolveActivity(packageManager)) {
            lastOrderRef = transaction.transactionRef
            startActivityForResult(chooser, 10001)
        } else {
            Toast.makeText(
                this,
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (RESULT_OK == resultCode && requestCode == 10001) {
            if (data != null) {
                val trxt: String? = data.getStringExtra("response")

               trxt?.let {
                   Log.e("UPI", "onActivityResult: $trxt")
                   val dataList: ArrayList<String> = ArrayList()
                   dataList.add(trxt)
                   upiPaymentDataOperation(dataList)
               }
            } else {
                Log.e("UPI", "onActivityResult: " + "Return data is null")
                val dataList: ArrayList<String> = ArrayList()
                dataList.add("nothing")
                upiPaymentDataOperation(dataList)
            }
        } else {
            //when user simply back without payment
            Log.e("UPI", "onActivityResult: " + "Return data is null")
            val dataList: ArrayList<String> = ArrayList()
            dataList.add("nothing")
            upiPaymentDataOperation(dataList)
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String>) {

        var str = data[0]
        Log.e("UPIPAY", "upiPaymentDataOperation: $str")
        var paymentCancel = ""
        var status = ""
        var approvalRefNo = ""
        val response = str.split("&").toTypedArray()
        for (i in response.indices) {
            val equalStr = response[i].split("=").toTypedArray()
            if (equalStr.size >= 2) {
                if (equalStr[0].lowercase(Locale.getDefault()) == "Status".toLowerCase()) {
                    status = equalStr[1].toLowerCase()
                } else if (equalStr[0].lowercase(Locale.getDefault()) == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                    approvalRefNo = equalStr[1]
                }
            } else {
                paymentCancel = "Payment cancelled by user."
            }
        }
        if (status == "success") {
            //Code to handle successful transaction here.
            Toast.makeText(this, "Transaction successful.", Toast.LENGTH_SHORT)
                .show()
            Log.e("UPI", "payment successfull: $approvalRefNo")
            upiCallback(approvalRefNo)
        } else if ("Payment cancelled by user." == paymentCancel) {
            Toast.makeText(
                this,
                "Payment cancelled by user.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("UPI", "Cancelled by user: $approvalRefNo")
        } else {
            Toast.makeText(
                this,
                "Transaction failed.Please try again",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("UPI", "failed payment: $approvalRefNo")
        }

        loadingDialog.hide()
    }

    private fun upiCallback(approvalRefNo: String) {
        loadingDialog.show()
        (application as MyApplication).iRetrofitApiCall
            .upiCallback(approvalRefNo)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                object : DisposableSingleObserver<ServerResponse>(){
                    override fun onSuccess(t: ServerResponse?) {
                        loadingDialog.hide()

                        t?.let {
                            Toast.makeText(this@UserListActivity, t.error_description, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(e: Throwable?) {
                        loadingDialog.hide()
                    }

                }
            )
    }

    fun getDate(milliSeconds:Long, dateFormat:String) : String
    {
        val formatter:SimpleDateFormat = SimpleDateFormat(dateFormat)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar:Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    data class ServiceItem(
        var id:String,
        var name:String,
        var price:String,
        var expire:String?,
        var active: Int = 0,
    )
}

