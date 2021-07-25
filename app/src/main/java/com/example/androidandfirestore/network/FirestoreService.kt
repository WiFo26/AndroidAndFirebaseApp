package com.example.androidandfirestore.network

import com.example.androidandfirestore.model.Crypto
import com.example.androidandfirestore.model.User
import com.google.firebase.firestore.FirebaseFirestore

const val CRYPTO_COLLECTION_NAME = "cryptos"
const val USER_COLLECTION_NAME = "users"
class FirestoreService (val firebaseFirestore: FirebaseFirestore){

    fun setDocument(data: Any,collectionName: String,id:String,callback: Callback<Void>){
        firebaseFirestore.collection(collectionName)
            .document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener{ e -> callback.onFailed(e)}
    }

    fun updateUser(user:User,callback:Callback<User>?){
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.username)
            .update("cryptosList",user.cryptosList)
            .addOnSuccessListener { result ->
                if (callback != null)
                    callback.onSuccess(user)
            }
            .addOnFailureListener{ e ->
                callback!!.onFailed(e)
            }
    }

    fun  updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available",crypto.available)
    }

    fun  getCryptos(callback: Callback<List<Crypto>>?){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val cryptoList = result.toObjects(Crypto::class.java)
                    callback!!.onSuccess(cryptoList)
                    break
                }
            }
            .addOnFailureListener{ e ->
                callback!!.onFailed(e)
            }
    }

    fun findUserById(id: String, callback: Callback<User>){
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(id)
            .get()
            .addOnSuccessListener { result ->
                if (result.data != null){
                    callback.onSuccess(result.toObject(User::class.java))
                }else{
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener{ e ->
                callback.onFailed(e)
            }
    }

    fun listenForUpdates(cryptos : List<Crypto>, listener: RealtimeDataListener<Crypto>){
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for (crypto in cryptos){
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener{ snapshot, e->
                if (e != null){
                    listener.onError(e)
                }
                if (snapshot != null && snapshot.exists()){
                    listener.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                }
            }
        }
    }

    fun listenForUpdates(user: User,listener: RealtimeDataListener<User>){
        val userReference = firebaseFirestore.collection(USER_COLLECTION_NAME)
        userReference.document(user.username).addSnapshotListener{snapshot, e->
            if (e != null){
                listener.onError(e)
            }
            if (snapshot != null && snapshot.exists()){
                listener.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }
}