package com.saadahmedev.videoplayer.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<T: Any, VB: ViewDataBinding> : RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder<VB>>() {

    private var items = mutableListOf<T>()

    @get: LayoutRes
    abstract val layoutRes: Int

    abstract fun onBind(binding: VB, item: T, position: Int)

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<T>) {
        this.items = items as MutableList<T>
        notifyDataSetChanged()
    }

    fun addItemsAfter(items: List<T>) {
        val startPosition = this.items.size
        for (item in items) this.items.add(item)
        notifyItemRangeInserted(startPosition, items.size)
    }

    fun addItem(item: T) {
        this.items.add(item)
        notifyItemInserted(this.items.size - 1)
    }

    fun addItem(position: Int, item: T) {
        this.items.add(position, item)
        notifyItemInserted(position)
    }

    fun removeItem(item: T) {
        val index = this.items.indexOf(item)
        this.items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeItem(position: Int) {
        this.items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseViewHolder<VB>(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutRes,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        onBind(holder.binding, items[position], position)
    }

    override fun getItemCount() = items.size

    class BaseViewHolder<VB : ViewDataBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}