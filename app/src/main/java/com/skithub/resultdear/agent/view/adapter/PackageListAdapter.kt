package com.skithub.resultdear.agent.view.adapter

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.agent.R
import com.skithub.resultdear.agent.databinding.LayoutServiceItemBinding
import com.skithub.resultdear.agent.databinding.PackageItemBinding
import com.skithub.resultdear.agent.model.Package
import com.skithub.resultdear.agent.model.ReferUser
import com.skithub.resultdear.agent.view.activities.UserListActivity

class PackageListAdapter (private val packageList: List<Package>,private val planName:String, val context:Context) : RecyclerView.Adapter<PackageListAdapter.MyViewHolder>() {

    var onBuyClickListener: ((Package) -> Unit)? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PackageListAdapter.MyViewHolder {

        val binding = PackageItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val `package` : Package = packageList.get(position)
        holder.tvName.text = planName
        holder.tvPrice.text = "Rs. ${`package`.price}"
        holder.tvExpire.text = "Validity\n ${`package`.validity} Days"

        holder.tvStatus.setOnClickListener {
            onBuyClickListener?.invoke(`package`)
        }
    }

    override fun getItemCount(): Int {
        return packageList.size
    }

    class MyViewHolder(itemView: PackageItemBinding):RecyclerView.ViewHolder(itemView.root) {
        val tvName = itemView.tvName
        val tvPrice = itemView.tvPrice
        val tvExpire = itemView.tvExpire
        val tvStatus = itemView.tvStatus
    }
}