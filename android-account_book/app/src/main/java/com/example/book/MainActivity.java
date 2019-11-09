package com.example.book;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static com.example.book.R.id.today;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<Activity> actList = new ArrayList<Activity>();

    CalendarView calendar;
    TextView totalDep, balance, totalExp, cash, card;
    int tempTD = 0, tempTE = 0, tempCash = 0, tempCard = 0, tempCFPTD = 0, tempCFPTE = 0;
    int year, month, day;
    int todayYear, todayMonth, todayDay;
    //String ymd, ymdToday;
    String date = "";
    Calendar c = Calendar.getInstance();
    //SimpleDateFormat dfM, dfD;
    ListView list;
    ArrayList<String> str = new ArrayList<String>(); // 카테고리
    ArrayList<String> str1 = new ArrayList<String>(); // 내역
    ArrayList<String> str2 = new ArrayList<String>(); // 돈
    ArrayList<String> str3 = new ArrayList<String>(); // "지출" or "입금"
    ArrayList<String> str4 = new ArrayList<String>(); // id 저장(KEY 값)
    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todayYear = c.get(Calendar.YEAR);
        todayMonth = c.get(Calendar.MONTH)+1;
        todayDay= c.get(Calendar.DATE);

        if(todayDay == 1) { // 여기에 있는 숫자 일이면 이월금 전송
           getCarryForwardPayment();
        }

        //c.set(todayYear, todayMonth - 1, todayDay);
        //dfM = new SimpleDateFormat("MM");

        //todayMonth = Integer.parseInt(dfM.format(c.getTime()));
        //Log.d("MONTH", ""+todayMonth);

        year = todayYear;
        month = todayMonth;
        day = todayDay;

        //c.set(year, month - 1, day);
        //ymd = df.format(c.getTime());

        totalDep = (TextView) findViewById(R.id.totalDep);
        balance = (TextView) findViewById(R.id.balance);
        totalExp = (TextView) findViewById(R.id.totalExp);
        cash = (TextView) findViewById(R.id.cash);
        card = (TextView) findViewById(R.id.card);

        calendar = (CalendarView) findViewById(R.id.calendar);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int y, int m, int d) {
                year = y;
                month = m+1;
                day = d;
                //c.set(year, month - 1, day);
                //ymd = df.format(c.getTime());
                //Toast.makeText(MainActivity.this,""+year+"/"+(month)+"/"+day, Toast.LENGTH_SHORT).show();
                getList();
                getTotalDep(); // 월 총 입금(수입) 금액 띄우기
                getTotalExp(); // 월 총 지출 금액 띄우기
                getBalance();
                getCash();
                getCard();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra("YEAR", year);
                intent.putExtra("MONTH", month);
                intent.putExtra("DAY", day);
                startActivity(intent);
            }
        });

        helper = new DBHelper(this);

        try{
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

        getTotalDep(); // 월 총 입금(수입) 금액 띄우기
        getTotalExp(); // 월 총 지출 금액 띄우기
        getBalance();
        getCash();
        getCard();
        todayList(); // 오늘 날짜의 리스트를 얻어서 ArrayList에 저장(수정해야함) -> 자꾸 메인으로 오면 오늘 날짜로오기때문
        getCustomList(); // 리스트를 띄어줌
        getCarryForwardPayment();

    }
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    @Override
    public  void onBackPressed(){	// 뒤로 가기
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        }else{
            backPressedTime = tempTime;
            Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }
    // 전달의 이월금을 얻어서 DB에 저장하는 메소드
    public void getCarryForwardPayment() {
        Cursor cursor, cursor1, cursor2;

        if(month == 1) { // 달이 1월달이라면 12월 입금과 지출 총액을 가져온다.
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEM " + (year - 1) + "1201 AND " + (year -1) + "1231);",null);
            while(cursor.moveToNext()) {
                tempCFPTD += Integer.parseInt(cursor.getString(0));
            }

            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + (year - 1) + "1201 AND " + (year -1) + "1231);",null);
            while(cursor1.moveToNext()) {
                tempCFPTE += Integer.parseInt(cursor1.getString(0));
            }

            cursor2 = db.rawQuery("SELECT id FROM DEPTABLE WHERE category = '이월금' AND date = " + year + "0101;", null);
            if(cursor2.moveToNext() == true) { // 이미 이월금이 존재한다면
                String num = cursor2.getString(0);
                db.execSQL("UPDATE DEPTABLE SET money = '" + (tempCFPTD - tempCFPTE) + "' WHERE id = " + num + ";");
            } else if(cursor2.moveToNext() == false) {
                db.execSQL("INSERT INTO DEPTABLE VALUES(null, " + year + "0101, '" + (tempCFPTD - tempCFPTE) + "','입금','이전 달 이월금','이월금');");
            }

        } else if(month >= 2 && month < 10) { // 달이 2월이거나 9월이라면
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + (month-1) + "01 AND " + year + "0" + (month-1) + "" + "31);", null);
            while(cursor.moveToNext()) {
                tempCFPTD += Integer.parseInt(cursor.getString(0));
            }
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + (month-1) + "01 AND " + year + "0" + (month-1) + "" + "31);", null);
            while(cursor1.moveToNext()) {
                tempCFPTE += Integer.parseInt(cursor1.getString(0));
            }
            cursor2 = db.rawQuery("SELECT id FROM DEPTABLE WHERE category = '이월금' AND date = " + year + "0" + month + "01;", null);
            if(cursor2.moveToNext() == true) {
                String num = cursor2.getString(0);
                db.execSQL("UPDATE DEPTABLE SET money = '" + (tempCFPTD - tempCFPTE) + "' WHERE id = " + num + ";");
            } else if(cursor2.moveToNext() == false) {
                db.execSQL("INSERT INTO DEPTABLE VALUES(null, " + year + "0" + month + "01, '" + (tempCFPTD - tempCFPTE) + "','입금','이전 달 이월금','이월금');");
            }


        }else if( month == 10) { // 10월일 시
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + (month-1) + "01 AND " + year + "0" + (month-1) + "" + "31);", null);
            while(cursor.moveToNext()) {
                tempCFPTD += Integer.parseInt(cursor.getString(0));
            }
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + (month-1) + "01 AND " + year + "0" + (month-1) + "" + "31);", null);
            while(cursor1.moveToNext()) {
                tempCFPTE += Integer.parseInt(cursor1.getString(0));
            }

            cursor2 = db.rawQuery("SELECT id FROM DEPTABLE WHERE category = '이월금' AND date = " + year + "" + month + "01;", null);
            if(cursor2.moveToNext() == true) {
                String num = cursor2.getString(0);
                db.execSQL("UPDATE DEPTABLE SET money = '" + (tempCFPTD - tempCFPTE) + "' WHERE id = " + num + ";");
            } else if(cursor2.moveToNext()==false) {
                db.execSQL("INSERT INTO DEPTABLE VALUES(null, " + year + "" + month + "01, '" + (tempCFPTD - tempCFPTE) + "','입금','이전 달 이월금','이월금');");
            }

        } else if( month > 10) { // 11월 or 12월 일시
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31);", null);
            while(cursor.moveToNext()) {
                tempCFPTD += Integer.parseInt(cursor.getString(0));
            }
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31);", null);
            while(cursor1.moveToNext()) {
                tempCFPTE += Integer.parseInt(cursor1.getString(0));
            }
            cursor2 = db.rawQuery("SELECT id FROM DEPTABLE WHERE category = '이월금' AND date = " + year + "" + month + "01;", null);
            if(cursor2.moveToNext() == true) {
                String num = cursor2.getString(0);
                db.execSQL("UPDATE DEPTABLE SET money = '" + (tempCFPTD - tempCFPTE) + "' WHERE id = " + num + ";");
            } else if(cursor2.moveToNext()==false) {
                db.execSQL("INSERT INTO DEPTABLE VALUES(null, " + year + "" + month + "01, '" + (tempCFPTD - tempCFPTE) + "','입금','이전 달 이월금','이월금');");
            }

        }
    }

    // 월별 총 입금(수입) 금액 띄우기
    public void getTotalDep() {
        tempTD = 0;
        Cursor cursor;
        if(month < 10 && day >= 10) {  // month만 있어도 가능함.
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTD += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day < 10) { // 마찬가지로 month만 있어도 가능함.
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTD += Integer.parseInt(cursor.getString(0));
            }
        } else if(month < 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTD += Integer.parseInt(cursor.getString(0));
            }

        } else if(month >= 10 && day >= 10){
            cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTD += Integer.parseInt(cursor.getString(0));
            }
        }

        totalDep.setText(""+tempTD+"원");
        totalDep.setTextColor(Color.BLUE);
    }

    public void getTotalExp() {
        tempTE = 0;
        Cursor cursor;
        if(month < 10 && day >= 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTE += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTE += Integer.parseInt(cursor.getString(0));
            }
        } else if(month < 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTE += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day >= 10){
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31)", null);
            while(cursor.moveToNext()) {
                tempTE += Integer.parseInt(cursor.getString(0));
            }
        }

        totalExp.setText(""+tempTE+"원");
        totalExp.setTextColor(Color.RED);
    }

    public void getBalance() {
        balance.setText("" + (tempTD-tempTE)+"원");
        balance.setTextColor(Color.BLACK);
    }

    public void getCash() {
        tempCash = 0;
        Cursor cursor;
        if(month < 10 && day >= 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '현금'", null);
            while(cursor.moveToNext()) {
                tempCash += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '현금'", null);
            while(cursor.moveToNext()) {
                tempCash += Integer.parseInt(cursor.getString(0));
            }
        } else if(month < 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '현금'", null);
            while(cursor.moveToNext()) {
                tempCash += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day >= 10){
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '현금'", null);
            while(cursor.moveToNext()) {
                tempCash += Integer.parseInt(cursor.getString(0));
            }
        }


        Cursor cursor1;
        if(month < 10 && day >= 10) {
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '체크카드'", null);
            while(cursor1.moveToNext()) {
                tempCash += Integer.parseInt(cursor1.getString(0));
            }
        } else if(month >= 10 && day < 10) {
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '체크카드'", null);
            while(cursor1.moveToNext()) {
                tempCash += Integer.parseInt(cursor1.getString(0));
            }
        } else if(month < 10 && day < 10) {
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '체크카드'", null);
            while(cursor1.moveToNext()) {
                tempCash += Integer.parseInt(cursor1.getString(0));
            }
        } else if(month >= 10 && day >= 10){
            cursor1 = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '체크카드'", null);
            while(cursor1.moveToNext()) {
                tempCash += Integer.parseInt(cursor1.getString(0));
            }
        }
        cash.setText(""+tempCash+"원");
    }

    public void getCard() {
        tempCard = 0;
        Cursor cursor;
        if(month < 10 && day >= 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '신용카드'", null);
            while(cursor.moveToNext()) {
                tempCard += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '신용카드'", null);
            while(cursor.moveToNext()) {
                tempCard += Integer.parseInt(cursor.getString(0));
            }
        } else if(month < 10 && day < 10) {
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) AND payway = '신용카드'", null);
            while(cursor.moveToNext()) {
                tempCard += Integer.parseInt(cursor.getString(0));
            }
        } else if(month >= 10 && day >= 10){
            cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) AND payway = '신용카드'", null);
            while(cursor.moveToNext()) {
                tempCard += Integer.parseInt(cursor.getString(0));
            }
        }
        card.setText(""+tempCard+"원");
    }

    public void getCustomList() {
        CustomList adapter = new CustomList(MainActivity.this);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(str3.get(position).equals("지출")){
                    Intent intent = new Intent(MainActivity.this, PrintExpActivity.class);
                    intent.putExtra("KEY",str4.get(position)); // 키값 넘겨줌
                    startActivity(intent);
                } else if(str3.get(position).equals("입금")){
                    Intent intent = new Intent(MainActivity.this, PrintDepActivity.class);
                    intent.putExtra("KEY", str4.get(position)); // 키값 넘겨줌
                    startActivity(intent);
                }
            }
        });
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

    public void todayList() {

        toIntDate();
        Log.d("todayList", date);

        Cursor cursor1 = db.rawQuery("SELECT category, content, money, dep, id FROM DEPTABLE WHERE date = " + date + ";", null);
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

    }

    // 선택한 날짜의 리스트 얻어오기.
    public void getList() {
        toIntDate();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   // 툴바 메뉴 선택시

        int id = item.getItemId();

        if(id == R.id.statistics) {
            Intent intent = new Intent(getApplicationContext(), PrintTotalActivity.class);
            startActivity(intent);
        } else if(id == R.id.search) {
            Intent intent = new Intent(MainActivity.this, PrintSearchActivity.class);
            startActivity(intent);
        }  else if(id == R.id.monthly) {

        } else if(id == R.id.weekly){
            Intent intent = new Intent(MainActivity.this, PrintWeeklyActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            startActivity(intent);
        } else if(id == R.id.daily) {
            Intent intent = new Intent(MainActivity.this, PrintDailyActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            startActivity(intent);
        } else if(id == today) {
            Intent intent = new Intent(MainActivity.this, PrintTodayActivity.class);
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);
            intent.putExtra("TODAYYEAR", todayYear);
            intent.putExtra("TODAYMONTH", todayMonth);
            intent.putExtra("TODAYDAY", todayDay);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
