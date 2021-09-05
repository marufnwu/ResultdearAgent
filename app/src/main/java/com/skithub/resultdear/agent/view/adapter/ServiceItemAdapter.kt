package com.skithub.resultdear.agent.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.agent.R
import com.skithub.resultdear.agent.databinding.LayoutServiceItemBinding
import com.skithub.resultdear.agent.view.activities.UserListActivity

class ServiceItemAdapter (public val serviceItemList: List<UserListActivity.ServiceItem>,val context:Context) : RecyclerView.Adapter<ServiceItemAdapter.MyViewHolder>() {

    var onItemClickListener : ((UserListActivity.ServiceItem)->Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceItemAdapter.MyViewHolder {
        val binding = LayoutServiceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceItemAdapter.MyViewHolder, position: Int) {
        val serviceItem : UserListActivity.ServiceItem = serviceItemList.get(position)
        holder.tvName.text = serviceItem.name
        holder.tvPrice.text = "Rs. ${serviceItem.price}"
        holder.tvStatus.text = "Packs"
        if(serviceItem.active==1){

            holder.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.round_bg_nan)
            if(serviceItem.expire!=null){
                holder.tvExpire.text = "Expire At\n${serviceItem.expire}"
            }else{
                holder.tvExpire.text = "N/A"
            }

            holder.tvStatus.setOnClickListener {
                onItemClickListener?.invoke(serviceItem)
            }
        }else{
            holder.tvExpire.text = "N/A"
            holder.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.round_bg_orange)
        }
    }

    override fun getItemCount(): Int {
        return serviceItemList.size
    }

    class MyViewHolder(itemView: LayoutServiceItemBinding):RecyclerView.ViewHolder(itemView.root) {
        val tvName = itemView.tvName
        val tvPrice = itemView.tvPrice
        val tvExpire = itemView.tvExpire
        val tvStatus = itemView.tvStatus

    }
}