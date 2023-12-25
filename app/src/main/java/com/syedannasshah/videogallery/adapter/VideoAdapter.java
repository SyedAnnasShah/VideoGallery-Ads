package com.syedannasshah.videogallery.adapter;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.syedannasshah.videogallery.R;
import com.syedannasshah.videogallery.activities.MainActivity;
import com.syedannasshah.videogallery.activities.PlayVideoActivity;
import com.syedannasshah.videogallery.data.Video;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {

    private final Context context;
    private ArrayList<Video> videoList;
    private boolean isFolder;
    private int newPosition;

    private int typeAds = 0;
    private int typePost = 1;
    private int empty = 2;

    private OnItemClickListener listener;

    public VideoAdapter(Context context, ArrayList<Video> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.isFolder = isFolder;
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        TextView duration;
        TextView size;
        ImageView image, ivMenu, ivPlay;
        View root;

        public MyHolder(View itemView) {
            super(itemView);
            duration = itemView.findViewById(R.id.tvDuration);
            size = itemView.findViewById(R.id.tvSize);
            image = itemView.findViewById(R.id.ivThumbnail);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            root = itemView;
        }
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == typePost) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_view_item, parent, false);
        } else if (viewType == typeAds) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_medium_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_native_layout, parent, false);
        }

        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyHolder holder, @SuppressLint("RecyclerView") int position) {


//        if (position == 0) {
//            return;
//        }
        if (position % 7 == 0) return;

        int myCalculatedPosition = position - (position / 7 + 1);

        holder.duration.setText(DateUtils.formatElapsedTime(videoList.get(myCalculatedPosition).getDuration() / 1000));
        holder.size.setText(Formatter.formatShortFileSize(context, videoList.get(myCalculatedPosition).getSize()));
        Glide.with(context)
                .asBitmap()
                .load(videoList.get(myCalculatedPosition).getArtUri())
                .apply(new RequestOptions().placeholder(R.drawable.ic_thumbnail).centerCrop())
                .into(holder.image);

//        holder.root.setOnClickListener(view -> {});

//
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                int myCalculatedPos = pos - (pos / 7 + 1);
                showPopupMenu(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size() + (videoList.size() / 7 + 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return empty;
        } else {
            if (position % 7 == 0) {
                return typeAds;
            } else {
                return typePost;
            }
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
