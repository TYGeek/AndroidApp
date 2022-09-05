package com.example.favdish.application

import android.app.Application
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.database.FavDishRoomDatabase

class FavDishApplication : Application() {

    // lazy mean -- init when it needed not just when application run
    private val database by lazy { FavDishRoomDatabase.getDatabase(this@FavDishApplication) }

    val repository by lazy { FavDishRepository(database.favDishDao()) }


}