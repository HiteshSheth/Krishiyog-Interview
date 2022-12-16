package com.interview.krishiyog.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.interview.krishiyog.base.BaseActivity
import com.interview.krishiyog.databinding.ActivityMainBinding
import com.interview.krishiyog.model.TrendingResponseModelItem
import com.interview.krishiyog.network.Resource
import com.interview.krishiyog.utils.hideView
import com.interview.krishiyog.utils.showView
import com.interview.krishiyog.view.adapter.TrendingAdapter
import com.interview.krishiyog.viewmodel.MainActViewModel
import kotlin.reflect.KClass

class MainActivity : BaseActivity<MainActViewModel>(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override val modelClass: KClass<MainActViewModel>
        get() = MainActViewModel::class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetryMainAct.setOnClickListener(this)

        binding.shimmerViewContainer.showView()
        binding.noDataMainAct.hideView()
        binding.rvTrendingMainAct.hideView()

        viewModel.getTrendingRepository()

        binding.pullToRefresh.setOnRefreshListener {

            binding.shimmerViewContainer.showView()
            binding.noDataMainAct.hideView()
            binding.rvTrendingMainAct.hideView()

            viewModel.getTrendingRepository()
            binding.pullToRefresh.isRefreshing = false
        }
    }

    override fun onClick(p0: View?) {

        when(p0){

            binding.btnRetryMainAct -> {

                viewModel.getTrendingRepository()
            }
        }
    }

    private val trendingRepoObserver = Observer<Any> { response ->
        when (response) {

            is Resource.Error<*> -> {
                binding.noDataMainAct.showView()
                binding.shimmerViewContainer.hideView()
                binding.rvTrendingMainAct.hideView()
            }
            is Resource.UnAuthorisedRequest<*> -> {
                binding.noDataMainAct.showView()
                binding.shimmerViewContainer.hideView()
                binding.rvTrendingMainAct.hideView()
            }
            is Resource.Success<*> -> {
                var res = response.data as ArrayList<TrendingResponseModelItem>


                    if(res != null && res.size > 0){

                        val adapter = TrendingAdapter(this, res)
                        binding.rvTrendingMainAct.adapter = adapter

                        binding.noDataMainAct.hideView()
                        binding.shimmerViewContainer.hideView()
                        binding.rvTrendingMainAct.showView()

                    } else {
                        binding.noDataMainAct.showView()
                        binding.shimmerViewContainer.hideView()
                        binding.rvTrendingMainAct.hideView()
                    }


            }
            is Resource.Loading<*> -> {
                response.isLoadingShow.let {
                    if (it as Boolean) {
                        binding.shimmerViewContainer.startShimmerAnimation()
                    } else {
                        binding.shimmerViewContainer.stopShimmerAnimation()
                    }
                }
            }
            is Resource.NoInternetError1<*> -> {
                binding.noDataMainAct.showView()
                binding.shimmerViewContainer.hideView()
                binding.rvTrendingMainAct.hideView()
            }
            is Resource.ValidationError<*> -> {
                binding.noDataMainAct.showView()
                binding.shimmerViewContainer.hideView()
                binding.rvTrendingMainAct.hideView()
            }
            is Resource.ValidationCheck<*> -> {
                binding.noDataMainAct.showView()
                binding.shimmerViewContainer.hideView()
                binding.rvTrendingMainAct.hideView()
            }
        }
    }

    override fun addObserver() {
        viewModel.trendingRepoStatus.observe(this, trendingRepoObserver)
    }

    override fun removeObserver() {
        viewModel.trendingRepoStatus.removeObserver(trendingRepoObserver)
    }

}