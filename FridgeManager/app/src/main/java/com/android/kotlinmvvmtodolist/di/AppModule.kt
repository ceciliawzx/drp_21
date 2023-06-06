package com.android.kotlinmvvmtodolist.di

import android.content.Context
import androidx.room.Room
import com.android.kotlinmvvmtodolist.data.local.ShopItemDao
import com.android.kotlinmvvmtodolist.data.local.TaskDatabase
import com.android.kotlinmvvmtodolist.util.Constants.SHOPPING_TABLE
import com.android.kotlinmvvmtodolist.util.Constants.TASK_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTaskDao(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            TASK_DATABASE
        ).build().taskDao()


    @Singleton
    @Provides
    fun provideShopItemDao(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            SHOPPING_TABLE
        ).build().shopItemDao()

}