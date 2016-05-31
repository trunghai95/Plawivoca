package trunghai95_1312165.miniproject2.plawivoca;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewList extends Activity {

    private SQLiteDatabase _appDB;
    private String word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_list);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        _appDB = SQLiteDatabase.openDatabase(
                Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.db_file_name),
                null, SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor = _appDB.rawQuery("select Word from WORD where Favorite > 0", null);
        if (!cursor.moveToFirst())
            return;
        do {
            word = cursor.getString(cursor.getColumnIndex("Word"));
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
                    getApplicationContext().getResources().getDisplayMetrics());
            lp.setMargins(0, 0, 0, px);
            tv.setLayoutParams(lp);
            tv.setText(word);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color3));

            tv.setOnClickListener(new MyOnClickListener(word));

            linearLayout.addView(tv);
        } while (cursor.moveToNext());
        cursor.close();
    }

    protected class MyOnClickListener implements View.OnClickListener {

        private String _word;

        MyOnClickListener(String w) {
            _word = w;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), WordView.class);
            intent.putExtra("word", _word);
            startActivity(intent);
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
}
