package com.example.inomtest.recyclerview

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.inomtest.R
import com.example.inomtest.dataClass.ProductResult
import com.example.inomtest.databinding.ItemViewBinding
import com.example.inomtest.network.App

class SearchResultAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var productList = ArrayList<ProductResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchResultViewHolder)
            holder.bind(this.productList[position])
    }

    override fun getItemCount(): Int {
        return this.productList.size
    }

    class SearchResultViewHolder(private val binding: ItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rArray : ProductResult) {
            with(binding) {
                itemTextViewPrice.text = rArray.price.toString()
                itemTextViewTitle.text = rArray.title
                //이미지설정
                Glide.with(App.instance)
                    .load(rArray.thumbnail)
                    .placeholder(R.drawable.ic_baseline_insert_photo_24)
                    .into(itemImageView)
            }
        }
    }

    fun submitList(productList1: ArrayList<ProductResult>){
        this.productList = productList1
        //Log.d(TAG,"잘넘어왔는지 봐봐 $productList")
    }
}