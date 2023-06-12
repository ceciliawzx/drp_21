package com.android.kotlinmvvmtodolist.ui.contacts

import com.android.kotlinmvvmtodolist.util.Constants.USER_DATABASE_REFERENCE
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object FirebaseModule {

    @Provides
    fun provideFirebaseDatabase(): DatabaseReference {
        return USER_DATABASE_REFERENCE
    }

}