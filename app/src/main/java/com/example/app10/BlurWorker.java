package com.example.app10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BlurWorker extends Worker {

    private static final String TAG = BlurWorker.class.getSimpleName();

    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();

        try {
            Bitmap picture = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.books);
            Bitmap blurredBitmap = WorkerUtils.blurBitmap(picture, applicationContext);
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, blurredBitmap);

            WorkerUtils.makeStatusNotification("Output is " + outputUri.toString(), applicationContext);
            return Result.success();
        } catch (Throwable throwable) {
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }

}


