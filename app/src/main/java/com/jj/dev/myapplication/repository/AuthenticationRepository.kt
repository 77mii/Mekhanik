package com.jj.dev.myapplication.repository

import android.util.Log

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.jj.dev.myapplication.Model.User
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AuthenticationRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    // Registers user with Firebase Auth and saves a basic user object (id, email, name, role)
    fun register(email: String, password: String, name: String, role: String): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .continueWithTask { authTask ->
                if (!authTask.isSuccessful) {
                    return@continueWithTask authTask // Return failed auth task
                }
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser == null) {
                    return@continueWithTask Tasks.forException(Exception("User authentication failed post-creation."))
                }
                val uid = firebaseUser.uid
                val basicUser = User(userId = uid, email = email, name = name, role = role)

                // Save only the basic user object to /users/{uid}
                return@continueWithTask database.getReference("users").child(uid).setValue(basicUser)
                    .continueWithTask { dbSaveTask ->
                        if (dbSaveTask.isSuccessful) {
                            Log.d(TAG, "Basic user object saved for $uid with role $role")
                            authTask // Return original successful auth task
                        } else {
                            Log.e(TAG, "Failed to save basic user object for $uid. Deleting auth user.", dbSaveTask.exception)
                            // Attempt to delete the auth user if DB save fails to prevent orphaned auth entries
                            firebaseUser.delete().addOnCompleteListener { deleteTask ->
                                if (!deleteTask.isSuccessful) {
                                    Log.w(TAG, "Failed to delete auth user after DB save failure.", deleteTask.exception)
                                }
                            }
                            Tasks.forException(dbSaveTask.exception ?: Exception("DB save of basic user failed and auth user cleanup attempted."))
                        }
                    }
            }
    }

    fun login(email: String, password: String): Task<AuthResult> =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // Fetches the basic user document (id, email, name, role)
    fun fetchBasicUserDocument(uid: String): Task<DataSnapshot> {
        return database.getReference("users").child(uid).get()
    }

    suspend fun logout(): Boolean {
        return try {
            firebaseAuth.signOut()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Logout failed", e)
            false
        }
    }
}
