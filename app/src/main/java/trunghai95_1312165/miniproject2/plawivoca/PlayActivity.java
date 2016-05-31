package trunghai95_1312165.miniproject2.plawivoca;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends Activity {

    private SQLiteDatabase _appDB;
    private Cursor _cursor;
    private String _word;
    private String _lastWord;
    private EditText _editText;
    private TextView _tvResult;
    private TextView _tvNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);

        _appDB = SQLiteDatabase.openDatabase(
                Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.db_file_name),
                null, SQLiteDatabase.OPEN_READWRITE);

        _word = nextWord();
        _lastWord = "";
        _editText = (EditText) findViewById(R.id.etGuess);
        _tvResult = (TextView) findViewById(R.id.tvResult);
        _tvNote = (TextView) findViewById(R.id.tvNote);

        Button btn = (Button) findViewById(R.id.btnSubmit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guess = _editText.getText().toString().toLowerCase();
                String tmp = _word.toLowerCase();
                if (guess.equals(tmp)) {
                    _lastWord = _word;
                    _word = nextWord();
                    showResult();
                } else {
                    showWarning(getString(R.string.str_incorrect));
                }

                _editText.setText("");
            }
        });

        btn = (Button) findViewById(R.id.btnListen);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent = new Intent(getApplicationContext(), SpeechService.class);
                speechIntent.putExtra("word", _word);
                startService(speechIntent);
            }
        });

        btn = (Button) findViewById(R.id.btnSkip);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _lastWord = _word;
                _word = nextWord();
                _editText.setText("");
                showWarning(getString(R.string.str_skipped));
            }
        });

        btn = (Button) findViewById(R.id.btnViewRes);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _lastWord = _word;
                _word = nextWord();
                showResult();
                _editText.setText("");
            }
        });
    }

    protected void showResult() {
        _tvResult.setText(_lastWord);
        _tvResult.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color3));
        _tvNote.setVisibility(View.VISIBLE);

        _tvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WordView.class);
                intent.putExtra("word", _lastWord);
                startActivity(intent);
            }
        });
    }

    protected void showWarning(String w) {
        _tvResult.setText(w);
        _tvResult.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));
        _tvNote.setVisibility(View.INVISIBLE);
        _tvResult.setClickable(false);
    }

    protected String nextWord() {
        if (_cursor == null || !_cursor.moveToNext()) {
            _cursor = _appDB.rawQuery("select Word from WORD order by random() limit 500", null);
            if (!_cursor.moveToFirst()) {
                Log.e("error", "Database error. Cannot move cursor.");
                Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return _cursor.getString(_cursor.getColumnIndex("Word"));
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
