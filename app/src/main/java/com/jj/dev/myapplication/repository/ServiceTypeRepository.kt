package com.jj.dev.myapplication.repository

import com.google.firebase.database.FirebaseDatabase
import com.jj.dev.myapplication.Model.ServiceType
import javax.inject.Inject

class ServiceTypeRepository @Inject constructor(
    private val db: FirebaseDatabase
) {
    private val ref = db.reference.child("serviceTypes")

    fun fetchAll(callback: (List<ServiceType>) -> Unit, error: (Exception) -> Unit) {
        ref.get()
            .addOnSuccessListener { snap ->
                val list = snap.children.mapNotNull { child ->
                    child.getValue(ServiceType::class.java)?.copy(serviceId = child.key ?: "")
                }
                callback(list)
            }
            .addOnFailureListener(error)
    }
}
