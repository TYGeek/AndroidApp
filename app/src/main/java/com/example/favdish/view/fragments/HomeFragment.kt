package com.example.favdish.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.GridLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentHomeBinding
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var mBinding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mBinding!!

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory( (requireActivity().application as FavDishApplication).repository )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
        val favDishAdapter = FavDishAdapter(this@HomeFragment)

        binding.rvDishesList.adapter = favDishAdapter

        // observe any changes and let it data to Log
        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner){ dishes->
            dishes.let {
                if (it.isNotEmpty()){
                    binding.rvDishesList.visibility = View.VISIBLE
                    binding.tvNoDishesAddedYet.visibility = View.GONE

                    favDishAdapter.dishesList(it)
                }else{
                    binding.rvDishesList.visibility = View.GONE
                    binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                }

                for (item in it){
                    Log.i("Dish Title", "${item.id} :: ${item.title}")
                }
            }
        }

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_home, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_add_dish -> {
                        startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}