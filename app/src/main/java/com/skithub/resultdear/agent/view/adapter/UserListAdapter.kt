package com.skithub.resultdear.agent.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.agent.databinding.LayoutUserListBinding
import com.skithub.resultdear.agent.model.ReferUser

class UserListAdapter(userList:List<ReferUser>, context: Context) : RecyclerView.Adapter<UserListAdapter.MyViewHolder>() {

    var context:Context = context
    private var userList: List<ReferUser> = userList
    var onInfoClickListener: ((ReferUser) -> Unit)? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListAdapter.MyViewHolder {
        val binding = LayoutUserListBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserListAdapter.MyViewHolder, position: Int) {
        val referUser : ReferUser = userList.get(position)
        holder.tvEmail.text = referUser.phone!!
        holder.btnInfo.setOnClickListener {
            onInfoClickListener?.invoke(referUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class MyViewHolder(itemView: LayoutUserListBinding) : RecyclerView.ViewHolder(itemView.root) {
        val tvName = itemView.tvName
        val tvEmail = itemView.tvEmail
        val btnInfo = itemView.btInfo

    }

    interface OnInfoClickListener{
        fun onClick(user:ReferUser)
    }
}

