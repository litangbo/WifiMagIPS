package com.jc.wifimagips;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jc.test.DoubleBean;
import com.jc.test.ListBean;

import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

public class ListTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_test);

        SQLiteDatabase db = Connector.getDatabase();
        this.findViewById(R.id.save_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListBean listBean = new ListBean();
                        listBean.setName("abc");
                        List<String> strList = new ArrayList<>();
                        strList.add("3qr");
                        strList.add("dsg");
                        listBean.setStrList(strList);
                        // listBean.setToDefault("numList");
                        List<DoubleBean> numList = new ArrayList<>();
                        numList.add(new DoubleBean(4.5));
                        numList.add(new DoubleBean(3.54));
                        numList.add(new DoubleBean(314.5));
                        listBean.setNumList(numList);

                        if(listBean.save()){
                            Toast.makeText(ListTestActivity.this,"save success",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ListTestActivity.this,"save failure",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
