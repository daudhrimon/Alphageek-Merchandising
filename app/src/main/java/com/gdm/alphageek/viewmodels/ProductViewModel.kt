package com.gdm.alphageek.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.down_sync.*
import com.gdm.alphageek.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val brandList   = MutableLiveData<List<Brand>>()
    val productList   = MutableLiveData<List<Product>>()
    val productListPosm   = MutableLiveData<List<Posm>>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun getProductByBrand(brandID:Int) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getProductByBrand(brandID)
            withContext(Dispatchers.Main) {
                productList.postValue(data)
            }
        }
    }
    fun getProductPosmByBrand(brandID:Int) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getProductPosmByBrand(brandID)
            withContext(Dispatchers.Main) {
                productListPosm.postValue(data)
            }
        }
    }

    fun getBrandList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllBrand()
            withContext(Dispatchers.Main) {
                brandList.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}