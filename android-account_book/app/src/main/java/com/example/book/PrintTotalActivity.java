package com.example.book;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.Calendar;

public class PrintTotalActivity extends AppCompatActivity {
    //    int[] pieChartValues = {0, };  //각 계열(Series)의 값
    public static final String TYPE = "type";
    //각 계열(Series)의 색상
    private static int[] COLORS = new int[]{Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.BLACK, Color.RED, Color.WHITE};

    //각 계열의 타이틀

    //String[] mSeriesTitle = new String[] {"월급", "용돈", "이월", "기타"};
    private CategorySeries mSeries = new CategorySeries("계열");
    private DefaultRenderer mRenderer = new DefaultRenderer();
    private GraphicalView mChartView;

    String[] inOut = new String[] {"지출", "수입"};
    String[] depCategory = new String[] {"월급", "용돈", "이월", "기타"};
    String[] expCategory = new String[] {"식비", "교통비", "문화생활", "생필품", "의류", "미용", "의료/건강", "교육", "통신비", "회비", "경조사", "저축", "가전", "공과금", "카드대금", "기타"};

    int[] pieChartValues = {0, 0};
    int[] depCategoryValuse = {0, 0, 0, 0};
    int[] expCategoryValuse = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    Button dateSelectButton;
    RadioGroup radioGroup;

    Calendar c = Calendar.getInstance();

    int y=0, m=0, d=0;

    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_graph);
        setTitle("통계");
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.argb(0, 50, 50, 50));
        mRenderer.setChartTitleTextSize(40);
        mRenderer.setLabelsTextSize(30);
        mRenderer.setLegendTextSize(30);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setStartAngle(90);

        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart_pie);
            mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
            mRenderer.setClickEnabled(true);
            mRenderer.setSelectableBuffer(10);
            layout.addView(mChartView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT));
        } else {
            mChartView.invalidate();
            mChartView.repaint();
        }

        helper = new DBHelper(this);
        try{
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }

        dateSelectButton = (Button) findViewById(R.id.dateSelect);
        dateSelectButton.setOnClickListener(btnListener);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == R.id.depRadioButton){
                    setDepCategory(y, m, d);
                }else{
                    setExpCategory(y, m, d);
                }
            }
        });
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View view) {
            Calendar c = Calendar.getInstance();
            int cyear = c.get(Calendar.YEAR);
            int cmonth = c.get(Calendar.MONTH);
            int cday = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                // onDateSet method
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    String date_selected = String.valueOf(year)+
                            " / "+String.valueOf(monthOfYear+1)+" / "+String.valueOf(dayOfMonth);
                    Toast.makeText( PrintTotalActivity.this, date_selected, Toast.LENGTH_SHORT).show();
                    y = year;
                    m = monthOfYear+1;
                    d = dayOfMonth;
                }
            };
            DatePickerDialog alert = new DatePickerDialog(PrintTotalActivity.this, mDateSetListener, cyear, cmonth, cday);
            alert.show();

        }
    };
    public  void setDepCategory(int year, int month, int day) {
        depCategoryValuse[0] = 0;
        depCategoryValuse[1] = 0;
        depCategoryValuse[2] = 0;
        depCategoryValuse[3] = 0;

        Cursor cursor;
        for(int i=0;i<depCategoryValuse.length;i++) {
            if(month < 10 && day >= 10) {
                cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) and category = '" + depCategory[i] + "'" , null);
                while(cursor.moveToNext()) {
                    depCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month >= 10 && day < 10) {
                cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) and category = '" + depCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    depCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month < 10 && day < 10) {
                cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) and category = '" + depCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    depCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month >= 10 && day >= 10){
                cursor = db.rawQuery("SELECT money FROM DEPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) and category = '" + depCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    depCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            }
        }
        fillDepPieChart();
    }
    public  void setExpCategory(int year, int month, int day) {
        for(int i=0;i<expCategoryValuse.length; i++) {
            expCategoryValuse[i] = 0;
        }

        Cursor cursor;
        for(int i=0;i<expCategoryValuse.length;i++) {
            if(month < 10 && day >= 10) {
                cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) and category = '" + expCategory[i] + "'" , null);
                while(cursor.moveToNext()) {
                    expCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month >= 10 && day < 10) {
                cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) and category = '" + expCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    expCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month < 10 && day < 10) {
                cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "0" + month + "01 AND " + year + "0" + month + "" + "31) and category = '" + expCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    expCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            } else if(month >= 10 && day >= 10){
                cursor = db.rawQuery("SELECT money FROM EXPTABLE WHERE (date BETWEEN " + year + "" + month + "01 AND " + year + "" + month + "" + "31) and category = '" + expCategory[i] + "'", null);
                while(cursor.moveToNext()) {
                    expCategoryValuse[i] += Integer.parseInt(cursor.getString(0));
                }
            }
        }
        fillExpPieChart();
    }

    public void fillDepPieChart() {
        mSeries.clear();
        for (int i = 0; i < depCategoryValuse.length; i++) {
            if(depCategoryValuse[i]==0) {
                continue;
            }
            mSeries.add(depCategory[i] + "_" + (String.valueOf(depCategoryValuse[i])), depCategoryValuse[i]);
            SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
            renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
            mRenderer.addSeriesRenderer(renderer);
            if (mChartView != null)
                mChartView.repaint();
        }
    }
    public void fillExpPieChart() {
        mSeries.clear();
        for (int i = 0; i < expCategoryValuse.length; i++) {
            if(expCategoryValuse[i]==0) {
                continue;
            }
            mSeries.add(expCategory[i] + "_" + (String.valueOf(expCategoryValuse[i])), expCategoryValuse[i]);
            SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
            renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
            mRenderer.addSeriesRenderer(renderer);
            if (mChartView != null)
                mChartView.repaint();
        }
    }
}