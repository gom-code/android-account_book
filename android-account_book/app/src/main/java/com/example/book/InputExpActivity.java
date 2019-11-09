package com.example.book;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.StringTokenizer;

import static com.example.book.MainActivity.actList;

public class InputExpActivity extends AppCompatActivity {
    TextView money, cateText, paywayText;
    EditText content;
    Button category, payway, button_ok, button_back, button_cancle;
    String Date;
    String Money;
    DBHelper helper;
    SQLiteDatabase db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_exp);
        setTitle("지출 내역");

        actList.add(this);

        money = (TextView)findViewById(R.id.money);
        cateText = (TextView)findViewById(R.id.cateText);
        paywayText = (TextView)findViewById(R.id.paywayText);
        content = (EditText)findViewById(R.id.content);
        category = (Button) findViewById(R.id.category);
        payway = (Button)findViewById(R.id.payway);
        button_ok = (Button)findViewById(R.id.button_ok);
        button_back = (Button)findViewById(R.id.button_back);
        button_cancle = (Button)findViewById(R.id.button_cancle);

        category.setOnClickListener(btnListener);
        payway.setOnClickListener(btnListener);
        button_ok.setOnClickListener(btnListener);
        button_back.setOnClickListener(btnListener);
        button_cancle.setOnClickListener(btnListener);

        Intent intent = getIntent();
        Date = intent.getStringExtra("DATE");
        Money = intent.getStringExtra("MONEY");

        StringTokenizer st = new StringTokenizer(Date,"/");
        Date = ""; // 날짜 초기화
        while(st.hasMoreTokens()) {
            Date += st.nextToken();
        }
        money.setText(Money+"원");

        helper = new DBHelper(this);
        try{
            db = helper.getWritableDatabase();
        }catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

    }

    public void expInsert(View target) {
        db.execSQL("INSERT INTO EXPTABLE VALUES(null, " + Date + ",'" + Money + "','지출','" + content.getText().toString() + "','" + cateText.getText().toString() + "','" + paywayText.getText().toString() + "');");
        Toast.makeText(getApplicationContext(), "지출 내역 추가 완료", Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.category:
                    showDialog(1);
                    break;
                case R.id.payway:
                    showDialog(2);
                    break;
                case R.id.button_ok:
                    if(cateText.getText().toString().equals("") || paywayText.getText().toString().equals("")) {
                        Toast.makeText(InputExpActivity.this,"입력 사항들을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        expInsert(view);
                        Intent intent1 = new Intent(InputExpActivity.this, MainActivity.class);
                        for (int i = 0; i < actList.size(); i++) {
                            actList.get(i).finish();
                        }
                        startActivity(intent1);
                    }
                    break;
                case R.id.button_back:
                    finish();
                    break;
                case R.id.button_cancle:
                    Intent intent2 = new Intent(InputExpActivity.this, MainActivity.class);
                    for(int i = 0; i < actList.size(); i++){
                        actList.get(i).finish();
                    }
                    startActivity(intent2);
                    break;
            }
        }
    };
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(id == 1) {
            final String items[] = {"식비", "교통비", "문화생활", "생필품", "의류", "미용", "의료/건강", "교육", "통신비", "회비", "경조사", "저축", "가전", "공과금", "카드대금", "기타"};
            builder.setTitle("카테고리");

            builder.setSingleChoiceItems(items, 0, new OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    cateText.setText(items[which]);
                    dialog.cancel();
                }
            });
        } else if(id == 2) {
            final String items1[] = {"현금", "체크카드", "신용카드"};
            builder.setTitle("결제수단");

            builder.setSingleChoiceItems(items1, 0, new OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    paywayText.setText(items1[which]);
                    dialog.cancel();
                }
            });
        }
        return builder.create();
    }
}