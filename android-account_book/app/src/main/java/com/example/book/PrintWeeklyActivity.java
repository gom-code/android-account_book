package com.example.book;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class PrintWeeklyActivity extends AppCompatActivity {
    int year, month, day;
    int todayYear, todayMonth, todayDay;
    String date = "";

    TextView totalDep, totalExp, cash, card;
    int tempTD = 0, tempTE = 0, tempCash = 0, tempCard = 0;
    Calendar c = Calendar.getInstance();
    int count = 0;

    TextView t1, category1, content1, money1;
    TextView t2, category2, content2, money2;
    TextView t3, category3, content3, money3;
    TextView t4, category4, content4, money4;
    TextView t5, category5, content5, money5;
    TextView t6, category6, content6, money6;
    TextView t7, category7, content7, money7;

    ListView list;
    ArrayList<String> str = new ArrayList<String>(); // 카테고리
    ArrayList<String> str1 = new ArrayList<String>(); // 내역
    ArrayList<String> str2 = new ArrayList<String>(); // 돈
    ArrayList<String> str3 = new ArrayList<String>(); // "지출" or "입금"
    ArrayList<String> str4 = new ArrayList<String>(); // id 저장(KEY 값)
    ArrayList<String> str5 = new ArrayList<String>(); // 날짜 저장
    ArrayList<String> dateList = new ArrayList<String>(); // 날짜 저장

    DBHelper helper;
    SQLiteDatabase db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_weekly);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        setTitle("주간 내역");

        Intent intent = getIntent();
        year = intent.getIntExtra("YEAR", 0);
        month = intent.getIntExtra("MONTH", 0);
        day = intent.getIntExtra("DAY", 0);
        todayYear = intent.getIntExtra("TODAYYEAR", 0);
        todayMonth = intent.getIntExtra("TODAYMONTH", 0);
        todayDay = intent.getIntExtra("TODAYDAY", 0);

        totalDep = (TextView) findViewById(R.id.totalDep);
        totalExp = (TextView) findViewById(R.id.totalExp);
        cash = (TextView) findViewById(R.id.cash);
        card = (TextView) findViewById(R.id.card);

        t1 = (TextView)findViewById(R.id.t1);
        category1 = (TextView)findViewById(R.id.category1);
        content1 = (TextView)findViewById(R.id.content1);
        money1 = (TextView)findViewById(R.id.money1);

        t2 = (TextView)findViewById(R.id.t2);
        category2 = (TextView)findViewById(R.id.category2);
        content2 = (TextView)findViewById(R.id.content2);
        money2 = (TextView)findViewById(R.id.money2);

        t3 = (TextView)findViewById(R.id.t3);
        category3 = (TextView)findViewById(R.id.category3);
        content3 = (TextView)findViewById(R.id.content3);
        money3 = (TextView)findViewById(R.id.money3);

        t4 = (TextView)findViewById(R.id.t4);
        category4 = (TextView)findViewById(R.id.category4);
        content4 = (TextView)findViewById(R.id.content4);
        money4 = (TextView)findViewById(R.id.money4);

        t5 = (TextView)findViewById(R.id.t5);
        category5 = (TextView)findViewById(R.id.category5);
        content5 = (TextView)findViewById(R.id.content5);
        money5 = (TextView)findViewById(R.id.money5);

        t6 = (TextView)findViewById(R.id.t6);
        category6 = (TextView)findViewById(R.id.category6);
        content6 = (TextView)findViewById(R.id.content6);
        money6 = (TextView)findViewById(R.id.money6);

        t7 = (TextView)findViewById(R.id.t7);
        category7 = (TextView)findViewById(R.id.category7);
        content7 = (TextView)findViewById(R.id.content7);
        money7 = (TextView)findViewById(R.id.money7);


        helper = new DBHelper(this);

        try{
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        c.set(year, month - 1, day);
        Calendar first = (Calendar)c.clone();
        first.add(Calendar.DAY_OF_MONTH, first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));

        dateList.add(df.format(first.getTime())); // 처음 날짜를 dateList에 추가

        for(int i = 1; i < 7; i++) {
            Calendar last = (Calendar) first.clone();
            last.add(Calendar.DAY_OF_MONTH, i);
            dateList.add(df.format(last.getTime())); // 이후 날짜(주간) dateList에 추가
        }

        toIntDate();
        getTotalDep(); // 월 총 입금(수입) 금액 띄우기
        getTotalExp(); // 월 총 지출 금액 띄우기
        getCash();
        getCard();
        getList();
        setText1();
        setText2();
        setText3();
        setText4();
        setText5();
        setText6();
        setText7();

    }

    public void toIntDate() {
        date = "";
        if(month < 10 && day >= 10) {
            date = ("" + year + "0" + month + "" + day);
        } else if(month >= 10 && day < 10) {
            date = ("" + year + "" + month + "0" + day);
        } else if(month < 10 && day < 10) {
            date = ("" + year + "0" + month + "0" + day);
        } else if(month >= 10 && day >= 10){
            date = ("" + year + "" + month + "" + day);
        }
    }

    // 월별 총 입금(수입) 금액 띄우기
    public void getTotalDep() {
        tempTD = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ");", null);
        while(cursor.moveToNext()) {
            tempTD += Integer.parseInt(cursor.getString(0));
        }
        startManagingCursor(cursor);
        totalDep.setText(""+tempTD+"원");
        totalDep.setTextColor(Color.BLUE);
    }

    public void getTotalExp() {
        tempTE = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ");", null);
        while(cursor.moveToNext()) {
            tempTE += Integer.parseInt(cursor.getString(0));
        }
        startManagingCursor(cursor);
        totalExp.setText(""+tempTE+"원");
        totalExp.setTextColor(Color.RED);
    }

    public void getCash() {
        tempCash = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ") AND payway = '현금';", null);
        while(cursor.moveToNext()) {
            tempCash += Integer.parseInt(cursor.getString(0));
        }
        startManagingCursor(cursor);

        Cursor cursor1;
        cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ") AND payway = '체크카드';", null);
        while(cursor1.moveToNext()) {
            tempCash += Integer.parseInt(cursor1.getString(0));
        }
        startManagingCursor(cursor1);
        cash.setText(""+tempCash+"원");
    }

    public void getCard() {
        tempCard = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ") AND payway = '신용카드';", null);
        while(cursor.moveToNext()) {
            tempCard += Integer.parseInt(cursor.getString(0));
        }
        startManagingCursor(cursor);
        card.setText(""+tempCard+"원");
    }

    public void getList() {
        str.clear();
        str1.clear();
        str2.clear();
        str3.clear();
        str4.clear();
        str5.clear();
        Cursor cursor1;
        cursor1 = db.rawQuery("SELECT category, content, money, dep, id, date FROM DEPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ");", null);
        while(cursor1.moveToNext() == true) {
            str.add(cursor1.getString(0));
            str1.add(cursor1.getString(1));
            str2.add(cursor1.getString(2));
            str3.add(cursor1.getString(3));
            str4.add(cursor1.getString(4));
            str5.add(cursor1.getString(5));
        }
        startManagingCursor(cursor1);

        Cursor cursor;
        cursor = db.rawQuery("SELECT category, content, money, exp, id, date FROM EXPTABLE WHERE (date BETWEEN " + dateList.get(0) + " AND " + dateList.get(6) + ");", null);
        while(cursor.moveToNext() == true) {
            str.add(cursor.getString(0));
            str1.add(cursor.getString(1));
            str2.add(cursor.getString(2));
            str3.add(cursor.getString(3));
            str4.add(cursor.getString(4));
            str5.add(cursor.getString(5));
        }
        startManagingCursor(cursor);
        //getCustomList();
    }

    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();
        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    public void setText1() {
        String temp = "", temp1 = "", temp2 = "";
        t1.setText(dateList.get(0) + " (일)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(0))) {
                temp = str.get(i) + "\n";
                category1.append(temp);
                temp1 = str1.get(i) + "\n";
                content1.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money1, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money1, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText2() {
        String temp = "", temp1 = "", temp2 = "";
        t2.setText(dateList.get(1) + " (월)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(1))) {
                temp = str.get(i) + "\n";
                category2.append(temp);
                temp1 = str1.get(i) + "\n";
                content2.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money2, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money2, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText3() {
        String temp = "", temp1 = "", temp2 = "";
        t3.setText(dateList.get(2) + " (화)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(2))) {
                temp = str.get(i) + "\n";
                category3.append(temp);
                temp1 = str1.get(i) + "\n";
                content3.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money3, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money3, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText4() {
        String temp = "", temp1 = "", temp2 = "";
        t4.setText(dateList.get(3) + " (수)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(3))) {
                temp = str.get(i) + "\n";
                category4.append(temp);
                temp1 = str1.get(i) + "\n";
                content4.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money4, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money4, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText5() {
        String temp = "", temp1 = "", temp2 = "";
        t5.setText(dateList.get(4) + " (목)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(4))) {
                temp = str.get(i) + "\n";
                category5.append(temp);
                temp1 = str1.get(i) + "\n";
                content5.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money5, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money5, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText6() {
        String temp = "", temp1 = "", temp2 = "";
        t6.setText(dateList.get(5) + " (금)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(5))) {
                temp = str.get(i) + "\n";
                category6.append(temp);
                temp1 = str1.get(i) + "\n";
                content6.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money6, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money6, temp2, Color.BLUE);
                }
            }
        }
    }

    public void setText7() {
        String temp = "", temp1 = "", temp2 = "";
        t7.setText(dateList.get(6) + " (토)");
        for(int i = 0; i < str5.size(); i++) {
            if(str5.get(i).equals(dateList.get(6))) {
                temp = str.get(i) + "\n";
                category7.append(temp);
                temp1 = str1.get(i) + "\n";
                content7.append(temp1);
                temp2 = str2.get(i) + "\n";
                if(str3.get(i).equals("지출")){
                    appendColoredText(money7, temp2, Color.RED);
                } else if(str3.get(i).equals("입금")) {
                    appendColoredText(money7, temp2, Color.BLUE);
                }
            }
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.statistics) {
            Intent intent = new Intent(getApplicationContext(), PrintTotalActivity.class);
            startActivity(intent);
        } else if(id == R.id.search) {

        } else if(id == R.id.monthly) {
            finish();
        } else if(id == R.id.weekly){

        } else if(id == R.id.daily) {
            Intent intent = new Intent(this, PrintDailyActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            finish();
            startActivity(intent);
        } else if(id == R.id.today) {
            Intent intent = new Intent(PrintWeeklyActivity.this, PrintTodayActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            finish();
            startActivity(intent);
        } else if(id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
