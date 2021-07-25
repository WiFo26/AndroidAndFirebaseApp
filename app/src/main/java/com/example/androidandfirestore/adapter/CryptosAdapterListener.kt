package com.example.androidandfirestore.adapter

import com.example.androidandfirestore.model.Crypto

interface CryptosAdapterListener {

    fun onBuyCryptoClicked(crypto: Crypto)

}