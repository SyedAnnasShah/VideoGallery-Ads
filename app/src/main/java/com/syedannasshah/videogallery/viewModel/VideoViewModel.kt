package com.syedannasshah.videogallery.viewModel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syedannasshah.videogallery.data.Video
import com.syedannasshah.videogallery.data.getAllVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    // Use application context here using the `getApplication()` method
    @SuppressLint("StaticFieldLeak")
    val appContext = getApplication<Application>().applicationContext

    private val _videos = MutableLiveData<ArrayList<Video>>()
    val videos: LiveData<ArrayList<Video>> = _videos

    fun getVideos() {
        // Use coroutines to fetch videos asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            val fetchedVideos = getAllVideos(appContext)
            _videos.postValue(fetchedVideos) // Update the LiveData with fetched data
        }
    }
}
