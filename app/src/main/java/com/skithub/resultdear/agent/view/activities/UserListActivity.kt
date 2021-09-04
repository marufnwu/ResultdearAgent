package com.skithub.resultdear.agent.view.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.skithub.resultdear.agent.databinding.DialogUserInfoBinding
import com.skithub.resultdear.agent.model.ReferUser
import com.skithub.resultdear.agent.model.response.PlanListResponse
import com.skithub.resultdear.agent.utils.LoadingDialog
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

