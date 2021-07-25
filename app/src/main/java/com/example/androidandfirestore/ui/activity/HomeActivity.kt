package com.example.androidandfirestore.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidandfirestore.R
import com.example.androidandfirestore.adapter.CryptosAdapter
import com.example.androidandfirestore.adapter.CryptosAdapterListener
import com.example.androidandfirestore.databinding.ActivityHomeBinding
import com.example.androidandfirestore.model.Crypto
import com.example.androidandfirestore.model.User
import com.example.androidandfirestore.network.Callback
import com.example.androidandfirestore.network.FirestoreService
import com.example.androidandfirestore.network.RealtimeDataListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.lang.Exception

class HomeActivity : AppCompatActivity(), CryptosAdapterListener {

    private lateinit var binding: ActivityHomeBinding

    lateinit var firestoreService: FirestoreService

    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)

    private var username : String? = null

    private var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        username = intent.extras!![USER_KEY]!!.toString()

        binding.idUser.text = getString(R.string.userInfo,username)

        configureRecyclerView()

        loadCryptos()

        binding.fbaBuy.setOnClickListener{ view ->
            Snackbar.make(view,"generating new cryptos...",
                Snackbar.LENGTH_SHORT)
                .setAction("Info",null)
                .show()
            generateCryptoCurrenciesRandom()
        }
    }

    private fun generateCryptoCurrenciesRandom() {
        for (crypto in cryptosAdapter.cryptoList){
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object : Callback<List<Crypto>>{
            override fun onSuccess(cryptoList : List<Crypto>?) {

                firestoreService.findUserById(username!!,object : Callback<User>{
                    override fun onSuccess(result: User?) {
                        user = result
                        if (user!!.cryptosList == null){
                            val userCryptoList = mutableListOf<Crypto>()

                            for (crypto in cryptoList!!){
                                val cryptoUser = Crypto()
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptoList.add(cryptoUser)
                            }
                            user!!.cryptosList = userCryptoList
                            firestoreService.updateUser(user!!, null)
                        }
                        loadUserCryptos()
                        addRealtimeDatabaseListeners(user!!, cryptoList!!)

                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralServerErrorMessage()
                    }

                })

                this@HomeActivity.runOnUiThread{
                    cryptosAdapter.cryptoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }

            }

            override fun onFailed(exception: Exception) {
                Log.e("HomeActivity","error loading criptos",exception)
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealtimeDatabaseListeners(user: User, cryptoList: List<Crypto>) {
        firestoreService.listenForUpdates(user,object : RealtimeDataListener<User>{
            override fun onDataChange(updatedData: User) {
                this@HomeActivity.user = updatedData
                loadUserCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })
        firestoreService.listenForUpdates(cryptoList, object : RealtimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                var pos = 0
                for (crypto in cryptosAdapter.cryptoList){
                    if (crypto.name.equals(updatedData.name)){
                        crypto.available = updatedData.available
                        cryptosAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun loadUserCryptos() {
        runOnUiThread{
            if (user!=null && user!!.cryptosList != null){
                val infoPanel = binding.llDescrip
                infoPanel.removeAllViews()
                for (crypto in user!!.cryptosList!!){
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, binding.llDescrip,false)
        view.findViewById<TextView>(R.id.countinfoCrypto).text = getString(R.string.coinInfo, crypto.name,crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.imginfoCrypto))
        binding.llDescrip.addView(view)
    }

    private fun showGeneralServerErrorMessage() {
        Snackbar.make(binding.fbaBuy,"Error while connecting to the server",Snackbar.LENGTH_SHORT)
            .setAction("Info",null).show()
    }

    private fun configureRecyclerView() {
        val rv = binding.rvCrypto
        rv.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        rv.layoutManager = layoutManager
        rv.adapter = cryptosAdapter
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        if (crypto.available > 0){
            for (userCrypto in user!!.cryptosList!!){
                if (userCrypto.name == crypto.name){
                    userCrypto.available += 1
                    break
                }
            }
            crypto.available--
            firestoreService.updateUser(user!!,null)
            firestoreService.updateCrypto(crypto)
        }
    }


}