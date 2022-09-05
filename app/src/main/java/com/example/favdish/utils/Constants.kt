package com.example.favdish.utils

object Constants {

    const val DISH_TYPE: String = "DishType"
    const val DISH_CATEGORY: String = "DishCategory"
    const val DISH_COOKING_TIME: String = "DishCookingTime"

    const val DISH_IMAGE_SOURCE_LOCAL: String = "Local"
    const val DISH_IMAGE_SOURCE_ONLINE: String = "Online"

    fun dishTypes(): ArrayList<String>{
        val list = ArrayList<String>()
        with(list){
            add("breakfast")
            add("lunch")
            add("snacks")
            add("dinner")
            add("salad")
            add("other")
        }
        return list
    }

    fun dishCategories(): ArrayList<String>{
        val list = ArrayList<String>()
        with(list){
            add("Pizza")
            add("BBQ")
            add("Bakery")
            add("Cafe")
            add("Chicken")
            add("Drinks")
            add("Tea & Coffee")
            add("Wraps")
            add("Other")
        }
        return list
    }

    fun dishCookTime(): ArrayList<String>{
        val list = ArrayList<String>()
        with(list){
            add("10")
            add("15")
            add("20")
            add("25")
            add("30")
            add("35")
        }
        return list
    }
}