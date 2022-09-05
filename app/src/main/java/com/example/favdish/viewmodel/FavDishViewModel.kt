package com.example.favdish.viewmodel

import androidx.lifecycle.*
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.entities.FavDish
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FavDishViewModel(private val repository: FavDishRepository): ViewModel() {

    fun insert(dish: FavDish) = viewModelScope.launch {
        repository.insertFavDishData(dish)
    }

    // we put an observer on data
    val allDishesList: LiveData<List<FavDish>> = repository.allDishesList.asLiveData()

}

class FavDishViewModelFactory(private val repository: FavDishRepository) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if( modelClass.isAssignableFrom(FavDishViewModel::class.java) ){
            @Suppress("UNCHECKED_CAST")
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}