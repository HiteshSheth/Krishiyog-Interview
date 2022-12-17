package com.interview.krishiyog.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.interview.krishiyog.R
import com.interview.krishiyog.base.BaseActivity
import com.interview.krishiyog.databinding.ActivityMainBinding
import com.interview.krishiyog.model.TrendingResponseModelItem
import com.interview.krishiyog.model.entity.TrendingResponseModelEntity
import com.interview.krishiyog.network.Resource
import com.interview.krishiyog.utils.CommonUtils.showToast
import com.interview.krishiyog.utils.hideView
import com.interview.krishiyog.utils.showView
import com.interview.krishiyog.view.adapter.TrendingAdapter
import com.interview.krishiyog.viewmodel.MainActViewModel
import kotlin.reflect.KClass

class MainActivity : BaseActivity<MainActViewModel>(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    var PREVIOUS_SELECTED_VALUE: Int = -1

    lateinit var trendingAdapter:TrendingAdapter

    override val modelClass: KClass<MainActViewModel>
        get() = MainActViewModel::class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetryMainAct.setOnClickListener(this)

        binding.pullToRefresh.setOnRefreshListener {

            callTrendingAPI()
            binding.pullToRefresh.isRefreshing = false
        }

        viewModel.getAllTrendingData()
    }

    override fun onClick(p0: View?) {

        when(p0?.id){

            R.id.btnRetry_mainAct -> {
                callTrendingAPI()
            }
        }
    }

    private val trendingRepoObserver = Observer<Any> { response ->
        when (response) {

            is Resource.Error<*> -> {
                noDataFound()
            }
            is Resource.UnAuthorisedRequest<*> -> {
                noDataFound()
            }
            is Resource.Success<*> -> {
                var res = response.data as ArrayList<TrendingResponseModelItem>

                    if(res != null && res.size > 0){

                        var tempTrendId = 0
                        var trendingDBList = ArrayList<TrendingResponseModelEntity>()
                        for(trendingData in res){

                            tempTrendId++
                            var data = TrendingResponseModelEntity(tempTrendId, trendingData.author, trendingData.avatar, trendingData.currentPeriodStars, trendingData.description,
                            trendingData.forks, trendingData.language, trendingData.languageColor, trendingData.name, trendingData.stars, trendingData.url, false)

                            trendingDBList.add(data)
                        }

                        viewModel.addTrendingData(trendingDBList, res)

                    } else {
                        noDataFound()
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
                noDataFound()
            }
            is Resource.ValidationError<*> -> {
                noDataFound()
            }
            is Resource.ValidationCheck<*> -> {
                noDataFound()
            }
        }
    }

    fun noDataFound(){
        binding.noDataMainAct.showView()
        binding.shimmerViewContainer.hideView()
        binding.pullToRefresh.hideView()
    }


    fun setAdapterData(res: ArrayList<TrendingResponseModelItem>){

        trendingAdapter = TrendingAdapter(res){

            if(PREVIOUS_SELECTED_VALUE == -1){
                PREVIOUS_SELECTED_VALUE = it

            } else {
                if(PREVIOUS_SELECTED_VALUE == it && res[it].isItemOpen){
                    res[PREVIOUS_SELECTED_VALUE].isItemOpen = false
                    res[it].isItemOpen = false
                    trendingAdapter.notifyItemChanged(PREVIOUS_SELECTED_VALUE)
                    trendingAdapter.notifyItemChanged(it)
                    return@TrendingAdapter

                } else {
                    res[PREVIOUS_SELECTED_VALUE].isItemOpen = false
                    trendingAdapter.notifyItemChanged(PREVIOUS_SELECTED_VALUE)
                    PREVIOUS_SELECTED_VALUE = it
                }

            }

            res[it].isItemOpen = true
            trendingAdapter.notifyItemChanged(it)
        }

        binding.rvTrendingMainAct.adapter = trendingAdapter
        binding.noDataMainAct.hideView()
        binding.shimmerViewContainer.hideView()
        binding.pullToRefresh.showView()
    }

    private val trendingDBObserver = Observer<Any> { response ->
        when (response) {

            is Resource.Error<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
            is Resource.UnAuthorisedRequest<*> -> {
            }

            is Resource.Success<*> -> {
                var res = response.data as ArrayList<TrendingResponseModelItem>

                setAdapterData(res)
            }

            is Resource.Loading<*> -> {
                response.isLoadingShow.let {

                }
            }
            is Resource.NoInternetError1<*> -> {

            }
            is Resource.ValidationError<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
            is Resource.ValidationCheck<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
        }
    }

    private val trendingListDBObserver = Observer<Any> { response ->
        when (response) {

            is Resource.Error<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
            is Resource.UnAuthorisedRequest<*> -> {
            }

            is Resource.Success<*> -> {

                var res = response.data as ArrayList<TrendingResponseModelItem>

                if(res != null && res.size > 0){

                    setAdapterData(res)
                } else {
                    callTrendingAPI()
                }
            }

            is Resource.Loading<*> -> {
                response.isLoadingShow.let {

                }
            }
            is Resource.NoInternetError1<*> -> {

            }
            is Resource.ValidationError<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
            is Resource.ValidationCheck<*> -> {
                showToast(resources.getString(R.string.db_transaction_msg))
            }
        }
    }

    private fun callTrendingAPI(){

        binding.shimmerViewContainer.showView()
        binding.noDataMainAct.hideView()
        binding.pullToRefresh.hideView()

        viewModel.getTrendingRepository()
    }

    override fun addObserver() {
        viewModel.trendingRepoStatus.observe(this, trendingRepoObserver)
        viewModel.trendingDBStatus.observe(this, trendingDBObserver)
        viewModel.trendingListDBStatus.observe(this, trendingListDBObserver)
    }

    override fun removeObserver() {
        viewModel.trendingRepoStatus.removeObserver(trendingRepoObserver)
        viewModel.trendingDBStatus.removeObserver(trendingDBObserver)
        viewModel.trendingListDBStatus.removeObserver(trendingListDBObserver)
    }

}