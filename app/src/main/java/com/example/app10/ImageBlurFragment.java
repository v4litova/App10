package com.example.app10;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.app10.R;

import java.util.concurrent.ExecutionException;

public class ImageBlurFragment extends Fragment {

    private static final String KEY_IMAGE_URI = "image_uri";
    private static final String KEY_BLURRED_IMAGE_URI = "blurred_image_uri";

    private ImageView imageView;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_blur, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        Button blurButton = rootView.findViewById(R.id.blurButton);

        Drawable drawable = getResources().getDrawable(R.drawable.books);

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        imageUri = Uri.parse(MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(), bitmap, "Title", null));
        imageView.setImageURI(imageUri);

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyBlur();
            }
        });

        WorkManager.getInstance(requireContext()).getWorkInfosByTagLiveData("BlurWork").observe(getViewLifecycleOwner(), new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String blurredImageUriString = workInfo.getOutputData().getString(KEY_BLURRED_IMAGE_URI);
                        if (blurredImageUriString != null) {
                            Uri blurredImageUri = Uri.parse(blurredImageUriString);
                            imageView.setImageURI(blurredImageUri);
                        } else {
                            Toast.makeText(requireContext(), "Failed to load blurred image", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });

        return rootView;
    }

    private void applyBlur() {
        Data inputData = createInputDataForUri();

        OneTimeWorkRequest blurRequest =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .setInputData(inputData)
                        .build();
        WorkManager.getInstance(requireContext()).enqueue(blurRequest);

        Toast.makeText(requireContext(), "Image blurring process initiated", Toast.LENGTH_SHORT).show();
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (imageUri != null) {
            builder.putString(KEY_IMAGE_URI, imageUri.toString());
        } else {
            Log.e("TAG", "ImageUri is null");
        }
        return builder.build();
    }
}

