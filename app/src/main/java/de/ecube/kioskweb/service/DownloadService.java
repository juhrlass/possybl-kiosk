package de.ecube.kioskweb.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


import de.ecube.kioskweb.BuildConfig;
import de.ecube.kioskweb.R;
import de.ecube.kioskweb.ServiceLocator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public final class DownloadService {

    private static final String TAG = "DownloadService";

    ProgressDialog progress;

    /**
     * This method will start downloading the file
     */
    public void startDownload(Context context, String downloadUrl, String targetFilename, boolean showProgress) {

        if (showProgress) {
            progress = new ProgressDialog(context);
            progress.setMessage(context.getString(R.string.download_in_progress));
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }
        Log.d(TAG, "Download started");
        //setup the dirs
        File targetFile = new File(context.getFilesDir() + "/" + targetFilename);

        //setup the request
        final Request request = new Request.Builder().url(downloadUrl).build();

        //setup the progressListner
        final ProgressListener progressListener = new ProgressListener() {
            boolean firstUpdate = true;

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    Log.d(TAG, "Download Complete");
                    ServiceLocator.getInstance().getInstallApkService().installPackage(context, BuildConfig.APPLICATION_ID, targetFile);
                } else {

                    if (firstUpdate) {
                        firstUpdate = false;
                        if (contentLength == -1) {
                            Log.d(TAG, "content-length: unknown");
                        } else {
                            Log.d(TAG, "content-length: " + contentLength);
                        }
                    }
                    if (contentLength != -1) {
                        Log.d(TAG, "" + (100 * bytesRead) / contentLength);
                        if (showProgress) {
                            progress.setProgress((int) ((100 * bytesRead) / contentLength));
                        }
                    }
                }
            }
        };

        //init okHttp client
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                })
                .build();

        //send the request and write the file
        Log.d(TAG, "Download Starting");

        new Thread(() -> {
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                //download Success now
                Log.d(TAG, "Download Completed");
                if (response.body() != null) {
                    FileUtils.copyInputStreamToFile(response.body().byteStream(), targetFile);
                }
                response.close(); //close reponse to avoid memory leak
                if (showProgress) {
                    progress.dismiss();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (showProgress) {
                    progress.dismiss();
                }
            }

        }).start();
    }

    /**
     * custom response body for progress tracking
     */
    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @NonNull
        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    /**
     * Listner
     */
    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

}
