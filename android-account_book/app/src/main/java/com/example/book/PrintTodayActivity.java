package com.example.book;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class PrintTodayActivity extends AppCompatActivity {

    int year, month, day;
    int todayYear, todayMonth, todayDay;
    String date = "";

    ListView list;
    ArrayList<String> str = new ArrayList<String>(); // 카테고리
    ArrayList<String> str1 = new ArrayList<String>(); // 내역
    ArrayList<String> str2 = new ArrayList<String>(); // 돈
    ArrayList<String> str3 = new ArrayList<String>(); // "지출" or "입금"
    ArrayList<String> str4 = new ArrayList<String>(); // id 저장(KEY 값)

    TextView totalDep, totalExp, cash, card, t;
    int tempTD = 0, tempTE = 0, tempCash = 0, tempCard = 0;
    Calendar c = Calendar.getInstance();

    DBHelper helper;
    SQLiteDatabase db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_daily);
        setTitle("오늘 내역");

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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
        t = (TextView)findViewById(R.id.t1);

        helper = new DBHelper(this);

        try{
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

        toIntDate(); // 날짜를 DB형식에 맞추어주는 메소드
        t.setText(date); // 오늘의 날짜를 띄어줌
        getTotalDep(); // 월 총 입금(수입) 금액 띄우기
        getTotalExp(); // 월 총 지출 금액 띄우기
        getCash();
        getCard();
        getList();
    }

    public void toIntDate() {
        date = "";
        if(todayMonth < 10 && todayDay >= 10) {
            date = ("" + todayYear + "0" + todayMonth + "" + todayDay);
        } else if(todayMonth >= 10 && todayDay < 10) {
            date = ("" + todayYear + "" + todayMonth + "0" + todayDay);
        } else if(todayMonth < 10 && todayDay < 10) {
            date = ("" + todayYear + "0" + todayMonth + "0" + todayDay);
        } else if(todayMonth >= 10 && todayDay >= 10){
            date = ("" + todayYear + "" + todayMonth + "" + todayDay);
        }
    }


    public void getTotalDep() {
        tempTD = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE date = " + date + ";", null);
        while(cursor.moveToNext()) {
            tempTD += Integer.parseInt(cursor.getString(0));
        }
        totalDep.setText(""+tempTD+"원");
        totalDep.setTextColor(Color.BLUE);
    }

    public void getTotalExp() {
        tempTE = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE date = " + date + ";", null);
        while(cursor.moveToNext()) {
            tempTE += Integer.parseInt(cursor.getString(0));
        }
        totalExp.setText(""+tempTE+"원");
        totalExp.setTextColor(Color.RED);
    }

    public void getCash() {
        tempCash = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE date = "  + date + " AND payway = '현금';", null);
        while(cursor.moveToNext()) {
            tempCash += Integer.parseInt(cursor.getString(0));
        }

        Cursor cursor1;
        cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE date = "  + date + " AND payway = '체크카드';", null);
        while(cursor1.moveToNext()) {
            tempCash += Integer.parseInt(cursor1.getString(0));
        }
        cash.setText(""+tempCash+"원");
    }

    public void getCard() {
        tempCard = 0;
        Cursor cursor;
        cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE date = " + date + " AND payway = '신용카드';", null);
        while(cursor.moveToNext()) {
            tempCard += Integer.parseInt(cursor.getString(0));
        }
        card.setText(""+tempCard+"원");
    }

    public void getList() {
        str.clear();
        str1.clear();
        str2.clear();
        str3.clear();
        str4.clear();
        Cursor cursor1;
        cursor1 = db.rawQuery("SELECT category, content, money, dep, id FROM DEPTABLE WHERE date = " + date + ";", null);
        while(cursor1.moveToNext() == true) {
            str.add(cursor1.getString(0));
            str1.add(cursor1.getString(1));
            str2.add(cursor1.getString(2));
            str3.add(cursor1.getString(3));
            str4.add(cursor1.getString(4));
        }

        Cursor cursor;
        cursor = db.rawQuery("SELECT category, content, money, exp, id FROM EXPTABLE WHERE date = " + date + ";", null);
        while(cursor.moveToNext() == true) {
            str.add(cursor.getString(0));
            str1.add(cursor.getString(1));
            str2.add(cursor.getString(2));
            str3.add(cursor.getString(3));
            str4.add(cursor.getString(4));
        }
        getCustomList();
    }

    public void getCustomList() {
        CustomList adapter = new CustomList(PrintTodayActivity.this);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(str3.get(position).equals("지출")){
                    Intent intent = new Intent(PrintTodayActivity.this, PrintExpActivity.class);
                    intent.putExtra("KEY",str4.get(position)); // 키값 넘겨줌
                    startActivity(intent);
                } else if(str3.get(position).equals("입금")){
                    Intent intent = new Intent(PrintTodayActivity.this, PrintDepActivity.class);
                    intent.putExtra("KEY", str4.get(position)); // 키값 넘겨줌
                    startActivity(intent);
                }
            }
        });
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;
        public CustomList(Activity context) {
            super(context, R.layout.listitem, str);
            this.context = context;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitem, null, true);
            TextView category = (TextView)rowView.findViewById(R.id.category);
            TextView content = (TextView)rowView.findViewById(R.id.content);
            TextView money = (TextView)rowView.findViewById(R.id.money);
            category.setText(str.get(position));
            content.setText(str1.get(position));
            money.setText(str2.get(position));
            if(str3.get(position).equals("지출")){
                money.setTextColor(Color.RED);
            } else if(str3.get(position).equals("입금")){
                money.setTextColor(Color.BLUE);
            }
            return rowView;
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
            Intent intent = new Intent(this, PrintWeeklyActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            finish();
            startActivity(intent);
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

        } else if(id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
