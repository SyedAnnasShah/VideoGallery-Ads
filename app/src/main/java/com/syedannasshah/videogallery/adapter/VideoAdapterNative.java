package com.syedannasshah.videogallery.adapter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.NativeAdViewHolder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.syedannasshah.videogallery.R;
import com.syedannasshah.videogallery.activities.MainActivity;
import com.syedannasshah.videogallery.data.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoAdapterNative extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_NATIVE_AD = 1;
    private static final int ITEM_VIEW_TYPE_ITEM = 0;
    private VideoAdapterNative.OnItemClickListener listener;
    private final Context context;
    private final List<Video> videoList;

    public VideoAdapterNative(Context context, List<Video> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 7 == 0) ? ITEM_VIEW_TYPE_NATIVE_AD : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_NATIVE_AD) {
            View adView = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(adView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_view_item, parent, false);
            return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == ITEM_VIEW_TYPE_NATIVE_AD) {
            // Populate native ad
            populateNativeAdView((NativeAdViewHolder) holder);
        } else {
            // Bind your regular item data
            // For example:
            // Item item = (Item) videoList.get(position);
            // ((ItemViewHolder) holder).bind(item);
        }
    }

    private void populateNativeAdView(NativeAdViewHolder nativeAdViewHolder) {
        AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd(nativeAd -> {
                    // Show the NativeAd
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.native_ad_layout, null);
                    populateNativeAd(nativeAd, adView);
                    nativeAdViewHolder.setNativeAd(nativeAd);
                })
//                .withAdListener(new AdLoader.AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(int errorCode) {
//                        // Handle ad load failure
//                    }
//                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAd(NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

        adView.setNativeAd(nativeAd);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvDuration);
        }
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.options_popup_menu); // Inflate your menu resource here

        if (position == 0) {
            return;
        }
        if (position % 7 == 0) return;

        int myCalculatedPosition = position - (position / 7 + 1);


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.action_share) {
                    if (listener != null) {
                        listener.onShareClick(myCalculatedPosition);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    requestDeleteR(myCalculatedPosition);
                    return true;
                } else if (itemId == R.id.action_open) {
                    if (listener != null) {
                        listener.onOpenClick(myCalculatedPosition);
                    }
                    return true;
                }
                return false;


            }
        });

        popupMenu.show();
    }

    public void updateList(ArrayList<Video> searchList) {
        videoList.clear();
        videoList.addAll(searchList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onShareClick(int position);


        void onOpenClick(int position);
    }

    private void updateDeleteUI(int position) {
        MainActivity.videoList.remove(position);
        notifyItemRemoved(position);
    }

    private void requestDeleteR(int position) {
        // List of videos to delete
        List<Uri> uriList = Collections.singletonList(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoList.get(position).getId()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Requesting delete permission for Android 11 and above
            PendingIntent deleteRequest = MediaStore.createDeleteRequest(context.getContentResolver(), uriList);
            try {
                ((Activity) context).startIntentSenderForResult(deleteRequest.getIntentSender(), 123, null, 0, 0, 0, null);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // For devices below Android 11
            File file = new File(videoList.get(position).getPath());
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Delete Video?")
                    .setMessage(videoList.get(position).getTitle())
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (file.exists() && file.delete()) {
                            MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
                            updateDeleteUI(position);
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog delDialog = builder.create();
            delDialog.show();

        }
    }

    public void setOnItemClickListener(VideoAdapterNative.OnItemClickListener listener) {
        this.listener = listener;
    }





    static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        NativeAdViewHolder(View itemView) {
            super(itemView);
        }

        void setNativeAd(NativeAd nativeAd) {
            if (itemView instanceof NativeAdView) {
                NativeAdView adView = (NativeAdView) itemView;
                // Populate the adView with the native ad details.
            }
        }
    }

}
