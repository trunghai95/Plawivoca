package trunghai95_1312165.miniproject2.plawivoca;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class Launcher extends Activity implements TextToSpeech.OnInitListener {

    private final int TTS_CHECK_CODE = 12345;
    private ProgressDialog _progressDialog;
    private String _dbPath;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launcher);

        _dbPath = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.db_file_name);

        // Check if TTS data is available
        // TODO: Check if Google Text-To-Speech is already installed
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK_CODE);

        // Check if DB already existed
        File file = new File(_dbPath);
        if (file.exists()) {
            Log.d("success", "DB exists");

            downloadDone();
        } else {
            Log.d("error", "DB not existed yet. Downloading...");
            String params[] = {getString(R.string.db_url), _dbPath};
            new DownloadTask(this).execute(params);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success
                // try to initialize
                tts = new TextToSpeech(this, this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    protected void downloadDone() {
        // Start Speech Service for the first time
        Intent speechIntent = new Intent(this, SpeechService.class);
        startService(speechIntent);

        // Start app
        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("success", "TextToSpeech init successfully");
            int res = tts.setLanguage(Locale.US);
            if (res == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "Language not supported");
                Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_LONG).show();
                finish();
            } else if (res == TextToSpeech.LANG_MISSING_DATA) {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        } else {
            Log.e("error", "TextToSpeech cannot init");
            Toast.makeText(getApplicationContext(), "Cannot initialize TextToSpeech", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
//                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(params[1]);

                byte data[] = new byte[4096];
//                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return "Download cancelled";
                    }
//                    total += count;
                    // publishing the progress....
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _progressDialog = ProgressDialog.show(context, "Please wait", "Downloading application database");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            _progressDialog.setIndeterminate(false);
            _progressDialog.setMax(100);
            _progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            _progressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                Log.e("error", result);
                finish();
            }
            else {
                Toast.makeText(context, "Database downloaded", Toast.LENGTH_SHORT).show();
                Log.d("done", "DB download done");
                downloadDone();
            }
        }
    }
}
