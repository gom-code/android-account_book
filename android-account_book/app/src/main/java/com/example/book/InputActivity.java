package com.example.book;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.book.MainActivity.actList;

public class InputActivity extends AppCompatActivity {
    EditText money;
    TextView date;
    int year, month, day;
    Button button_dep, button_exp, button_cancle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input);
        setTitle("입력 화면");

        actList.add(this);

        money = (EditText) findViewById(R.id.money);
        date = (TextView) findViewById(R.id.date);

        Intent intent = getIntent();
        year = intent.getIntExtra("YEAR", 0);
        month = intent.getIntExtra("MONTH", 0);
        day = intent.getIntExtra("DAY", 0);

        if(month < 10 && day >= 10) {
            date.setText("" + year + "/0" + month + "/" + day);
        } else if(month >= 10 && day < 10) {
            date.setText("" + year + "/" + month + "/0" + day);
        } else if(month < 10 && day < 10) {
            date.setText("" + year + "/0" + month + "/0" + day);
        } else if(month >= 10 && day >= 10){
            date.setText("" + year + "/" + month + "/" + day);
        }

        button_dep = (Button) findViewById(R.id.button_dep);
        button_exp = (Button) findViewById(R.id.button_exp);
        button_cancle = (Button) findViewById(R.id.button_cancle);

        button_dep.setOnClickListener(btnListener);
        button_exp.setOnClickListener(btnListener);
        button_cancle.setOnClickListener(btnListener);
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.button_dep:
                    if(money.getText().toString().equals("") ) {
                        Toast.makeText(InputActivity.this,"금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent1 = new Intent(InputActivity.this, InputDepActivity.class);
                        intent1.putExtra("DATE", date.getText().toString());
                        intent1.putExtra("MONEY", money.getText().toString());
                        startActivity(intent1);
                    }
                    break;
                case R.id.button_exp:
                    if(money.getText().toString().equals("")) {
                        Toast.makeText(InputActivity.this,"금액을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent2 = new Intent(InputActivity.this, InputExpActivity.class);
                        intent2.putExtra("DATE", date.getText().toString());
                        intent2.putExtra("MONEY", money.getText().toString());
                        startActivity(intent2);
                    }
                    break;
                case R.id.button_cancle:
                    finish();
                    break;
            }
        }
    };
}