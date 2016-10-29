package com.example.chandra.sms_io;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeMsg extends AppCompatActivity implements View.OnClickListener {
    Button buttonSend;
    EditText textPhoneNo;
    EditText textSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_msg);

        buttonSend = (Button) findViewById(R.id.send);
        textPhoneNo = (EditText) findViewById(R.id.phoneNum);
        textSMS = (EditText) findViewById(R.id.ComposeMsg);
        buttonSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(R.id.send == v.getId()) {
            String phoneNo = textPhoneNo.getText().toString();
            String sms = textSMS.getText().toString();

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent!",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again later!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
