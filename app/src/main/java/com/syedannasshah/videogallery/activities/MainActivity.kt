package com.syedannasshah.videogallery.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.syedannasshah.videogallery.adapter.VideoAdapter
import com.syedannasshah.videogallery.data.Video
import com.syedannasshah.videogallery.data.getAllVideos
import com.syedannasshah.videogallery.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), VideoAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding

    lateinit var adapter: VideoAdapter

    private var mInterstitialAd: InterstitialAd? = null


    private val STORAGE_PERMISSION_CODE = 101
    private val PREFS_NAME = "MyPrefs"
    private val PERMISSION_ASKED_KEY = "permissionAsked"

    companion object {
        lateinit var videoList: ArrayList<Video>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoList = ArrayList()

        loadAd()


        requestRuntimePermissionForDialog()




    }

    private fun requestRuntimePermission(): Boolean {
        //android 13 permission request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),
                    13
                )
                return false
            }
            return true
        }

        //requesting storage permission for only devices less than api 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )

                return false
            }
        } else {
            //read external storage permission for devices higher than android 10 i.e. api 29
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    14
                )
                return false
            }
        }
        return true
    }



    private fun requestRuntimePermissionForDialog() {
        //android 13 permission request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                if (requestRuntimePermission()) {
                    // perm granted
                    getVideosAndShow()
                }
            }
        }

        //requesting storage permission for only devices less than api 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                showWhyPermissionsNeededDialog()
            } else {
                if (requestRuntimePermission()) {
                    // perm granted
                    getVideosAndShow()
                }
            }
        }
        else {
            //read external storage permission for devices higher than android 10 i.e. api 29
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {


                showWhyPermissionsNeededDialog()
            } else {
                if (requestRuntimePermission()) {
                    // perm granted
                    getVideosAndShow()
                }
            }
        }
    }


    private fun getVideosAndShow() {
        markPermissionAsAsked()

        videoList = getAllVideos(this)

        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.setItemViewCacheSize(10)
        adapter = VideoAdapter(this, videoList)
        var gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    0 -> 3
                    1 -> 1
                    2 -> 3
                    else -> 1
                }
            }
        }
        binding.VideoRV.setHasFixedSize(true)
        binding.VideoRV.layoutManager = gridLayoutManager
        adapter.setOnItemClickListener(this)
        binding.VideoRV.adapter = adapter
        binding.totalVideos.text = "Total Videos: ${videoList.size}"

        //for refreshing layout
        binding.root.setOnRefreshListener {
            videoList = getAllVideos(this)
            adapter.updateList(videoList)
            binding.totalVideos.text = "Total Videos: ${videoList.size}"
            binding.totalVideos.visibility = GONE

            binding.root.isRefreshing = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            try {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    getVideosAndShow()
                } else Snackbar.make(binding.root, "Storage Permission Needed!!", 5000)
                    .setAction("OK") {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            13
                        )
                    }
                    .show()
            } catch (e: Exception) {

            }
        }

        //for read external storage permission
        if (requestCode == 14) {
            try {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    getVideosAndShow()
                } else Snackbar.make(binding.root, "Storage Permission Needed!!", 5000)
                    .setAction("OK") {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            14
                        )
                    }
                    .show()
            } catch (e: Exception) {

            }
        }
    }

    // Check if permission has been asked before
    private fun hasAskedForPermission(): Boolean {
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return preferences.getBoolean(PERMISSION_ASKED_KEY, false)
    }

    // Mark permission as asked
    private fun markPermissionAsAsked() {
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(PERMISSION_ASKED_KEY, true)
        editor.apply()
    }

    private fun showWhyPermissionsNeededDialog() {

        if (!hasAskedForPermission()){

            // Explain to the user why the permission is needed
            val dialog = AlertDialog.Builder(this)
                .setTitle("Storage Permission Needed")
                .setMessage("This app needs access to your device's storage to display videos.")
                .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                    // Request the permission when the user clicks "OK"
                    dialogInterface.dismiss()
                    if (requestRuntimePermission()) {
                        // perm granted
                        getVideosAndShow()
                    }

                }
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                    // Dismiss the dialog if the user clicks "Cancel"
                    dialogInterface.dismiss()
                    // Optionally handle denial of permission
                }
                .create()
            dialog.show()
        }else{


            if (requestRuntimePermission()) {
                // perm granted
                getVideosAndShow()
            }
        }
    }

    override fun onShareClick(position: Int) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoList[position].path))
        ContextCompat.startActivity(
            this,
            Intent.createChooser(shareIntent, "Sharing Video File!!"),
            null
        )
    }

    override fun onOpenClick(position: Int) {

        showInterstitial(position)


    }


    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("@@@", adError.toString() ?:"")
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.e("@@@", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
//                    showInterstitial()
                }
            })
    }

    private fun showInterstitial(position: Int) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                        loadAd()

                        playVideoInPlayerActivity(position)



                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null

                        playVideoInPlayerActivity(position)

                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when ad is dismissed.
                    }
                }
            mInterstitialAd?.show(this)
        } else {
            playVideoInPlayerActivity(position)


//            loadAd()
//            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playVideoInPlayerActivity(position: Int) {
        val intent = Intent(this, PlayVideoActivity::class.java)
        intent.putExtra("videoPath", videoList[position].path)
        intent.putExtra("videoIndex", position)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}