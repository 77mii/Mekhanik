package com.jj.dev.myapplication

import com.google.firebase.appdistribution.gradle.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jj.dev.myapplication.repository.AppointmentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This ensures that the module is installed for the whole app lifecycle.
object AppModule {

    // Provides FirebaseAuth as a Singleton
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Provides FirebaseDatabase as a Singleton
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }
    @Provides
    @Singleton
    @Named("appointmentsRef")
    fun provideAppointmentsRef(
        database: FirebaseDatabase
    ): DatabaseReference =
        database.getReference("appointments")
/*    @Provides @Singleton
    fun provideAppointmentRepository() = AppointmentRepository()*/
}