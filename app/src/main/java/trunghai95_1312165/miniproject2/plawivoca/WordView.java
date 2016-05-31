package trunghai95_1312165.miniproject2.plawivoca;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class WordView extends Activity {

    private String _word;
    private boolean _inList = false;
    private Button _btnListen;
    private Button _btnAddRemoveList;
    private WebView _webView;
    private String _url;
    private SQLiteDatabase _appDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_word_view);

        Intent intent = getIntent();
        _word = intent.getStringExtra("word");

        _appDB = SQLiteDatabase.openDatabase(
                Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.db_file_name),
                null, SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor = _appDB.rawQuery("select Favorite from WORD where Word = \'" + _word + "\'", null);
        if (cursor.moveToFirst())
            _inList = (cursor.getInt(cursor.getColumnIndex("Favorite")) != 0);
        cursor.close();

        _url = getString(R.string.dict_url) + _word;

        _btnListen = (Button) findViewById(R.id.btnListen);
        _btnAddRemoveList = (Button) findViewById(R.id.btnAddRemoveList);

        if (_inList) {
            _btnAddRemoveList.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));
            _btnAddRemoveList.setText(getString(R.string.btn_removelist));
        }

        // Speak the word when user clicks listen
        _btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent = new Intent(getApplicationContext(), SpeechService.class);
                speechIntent.putExtra("word", _word);
                startService(speechIntent);
            }
        });

        _btnAddRemoveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddRemoveList_onClick();
            }
        });

        _webView = (WebView) findViewById(R.id.webView);
        _webView.setWebViewClient(new MyBrowser());
        _webView.loadUrl(_url);
    }

    protected void btnAddRemoveList_onClick() {
        if (_inList) {
            // If already in list, remove it
            _btnAddRemoveList.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color3));
            _btnAddRemoveList.setText(getString(R.string.btn_addlist));
            _appDB.execSQL("update WORD set Favorite = 0 where Word = \'" + _word + "\'");
            _inList = false;
        } else {
            _btnAddRemoveList.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));
            _btnAddRemoveList.setText(getString(R.string.btn_removelist));
            _appDB.execSQL("update WORD set Favorite = 1 where Word = \'" + _word + "\'");
            _inList = true;
        }
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals(_url))
                view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onPause() {
        if (_appDB != null) {
            _appDB.close();
            _appDB = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (_appDB == null) {
            _appDB = SQLiteDatabase.openDatabase(
                    Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.db_file_name),
                    null, SQLiteDatabase.OPEN_READWRITE);
        }
        super.onResume();
    }
}
