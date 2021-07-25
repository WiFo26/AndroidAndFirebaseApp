package com.example.androidandfirestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidandfirestore.R
import com.example.androidandfirestore.model.Crypto
import com.squareup.picasso.Picasso

class CryptosAdapter(val cryptosAdapterListener: CryptosAdapterListener):
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {

    var cryptoList : List<Crypto> = ArrayList()

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var image = itemView.findViewById<ImageView>(R.id.imgCrypto)
        var name = itemView.findViewById<TextView>(R.id.nameCrypto)
        var available = itemView.findViewById<TextView>(R.id.countCrypto)
        var buyButton = itemView.findViewById<TextView>(R.id.buyCrypto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptosAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cripto_row,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptosAdapter.ViewHolder, position: Int) {
        val crypto = cryptoList[position]

        Picasso.get().load(crypto.imageUrl).into(holder.image)
        holder.name.text = crypto.name
        holder.available.text = crypto.available.toString()
        holder.buyButton.setOnClickListener{
            cryptosAdapterListener.onBuyCryptoClicked(crypto)
        }
    }

    override fun getItemCount() = cryptoList.size


}