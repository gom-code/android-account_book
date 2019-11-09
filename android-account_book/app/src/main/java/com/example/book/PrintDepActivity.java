package com.example.book;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import static com.example.book.MainActivity.actList;

/**
 * Created by junho on 2017-06-15.
 */

public class PrintDepActivity extends AppCompatActivity {
    TextView date;
    EditText money;
    TextView category;
    EditText content;
    String key; // 키 값 받을 변수
    DBHelper helper;
    SQLiteDatabase db;

    int year, month, day;
    String date1 = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_dep);
        setTitle("입금 내역서");

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        date = (TextView) findViewById(R.id.date);
        money = (EditText)findViewById(R.id.money);
        category = (TextView)findViewById(R.id.category);
        content = (EditText)findViewById(R.id.content);

        date.setOnClickListener(btnListener);
        money.setOnClickListener(btnListener);
        category.setOnClickListener(btnListener);
        content.setOnClickListener(btnListener);

        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day= c.get(Calendar.DATE);

        Intent intent = getIntent();
        key = intent.getStringExtra("KEY"); // 키 값을 받음.

        helper = new DBHelper(this);

        try{
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

        getValue();
    }

    public void toIntDate() {
        date1 = "";
        if(month < 10 && day >= 10) {
            date1 = ("" + year + "0" + month + "" + day);
        } else if(month >= 10 && day < 10) {
            date1 = ("" + year + "" + month + "0" + day);
        } else if(month < 10 && day < 10) {
            date1 = ("" + year + "0" + month + "0" + day);
        } else if(month >= 10 && day >= 10){
            date1 = ("" + year + "" + month + "" + day);
        }
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.date:
                    showDialog(1);
                    break;
                case R.id.money:
                    money.setFocusableInTouchMode(true);
                    break;
                case R.id.category:
                    showDialog(2);
                    break;
                case R.id.content:
                    content.setFocusableInTouchMode(true);
                    break;
            }
        }
    };

    public void getValue() {
        Cursor cursor;
        cursor = db.rawQuery("SELECT date, money, category, content FROM DEPTABLE WHERE id = '" + key + "';", null);
        while(cursor.moveToNext() == true) {
            date.setText(cursor.getString(0));
            money.setText(cursor.getString(1));
            category.setText(cursor.getString(2));
            content.setText(cursor.getString(3));
        }
    }

    public void modify() {
        db.execSQL("UPDATE DEPTABLE SET date = " + date.getText().toString()
                + ", money = '" + money.getText().toString()
                + "', category = '" + category.getText().toString()
                + "', content = '" + content.getText().toString()
                + "' WHERE id = '" + key + "';");
        Intent intent = new Intent(PrintDepActivity.this, MainActivity.class);
        for(int i = 0; i < actList.size(); i++){
            actList.get(i).finish();
        }
        finish();
        startActivity(intent);
    }

    public void delete() {
        db.execSQL("DELETE FROM DEPTABLE WHERE id = '" + key + "';");
        Intent intent = new Intent(PrintDepActivity.this, MainActivity.class);
        for(int i = 0; i < actList.size(); i++){
            actList.get(i).finish();
        }
        finish();
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_print, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /* 셋팅 버튼
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */

        if(id == R.id.modify) {
            showDialog(3);
        } else if(id == R.id.delete) {
            showDialog(4);
        } else if(id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // 새로운 날짜를 클릭하여 텍스트의 값을 변경
    DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth1) {
            year = year1;
            month = month1 + 1; // 사용자에게 표시 할 경우만 +1해줌
            day = dayOfMonth1;
            toIntDate();
            date.setText(date1);
        }
    };
    // Dialog 생성
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(id == 1) {
            DatePickerDialog dialog = new DatePickerDialog(this, callback, year, month, day);
            return dialog;
        } else if(id == 2) {

            final String items[] = {"식비", "교통비", "문화생활", "생필품", "의류", "미용", "의료/건강", "교육", "통신비", "회비", "경조사", "저축", "가전", "공과금", "카드대금", "기타"};
            builder.setTitle("카테고리");

            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    category.setText(items[which]);
                    dialog.cancel();
                }
            });
        } else if(id == 3) {
            builder.setTitle("입금 내역 수정 여부");
            builder.setMessage("입금(수입) 내역을 수정하시겠습니까?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    modify(); // 수정
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Dialog 창 없앰.
                }
            });
        } else if(id == 4) {
            builder.setTitle("입금 내역 삭제 여부");
            builder.setMessage("입금(수입) 내역을 삭제하시겠습니까?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    delete(); // 삭제
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Dialog창 없앰
                }
            });
        }
        return builder.create();
    }
}