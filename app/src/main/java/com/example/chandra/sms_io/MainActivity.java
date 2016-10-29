package com.example.chandra.sms_io;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemLongClickListener {
    private static final int PERMISSION_SEND_SMS = 123;
    ListView lvMsg;
    SimpleCursorAdapter adapter;
    Context context;
    String myPackageName;
    SmsParser parseObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_homescreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = MainActivity.this;
        myPackageName = getPackageName();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        txtView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        navigationView.setNavigationItemSelectedListener(this);

        lvMsg = (ListView) findViewById(R.id.lvMsg);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                refreshSmsInbox();
                if(isComposeMsgAllowed()){
                    Intent i = new Intent(getApplicationContext(),ComposeMsg.class);
                    startActivity(i);
                } else {
                    requestSendSmsPermission();
                    Intent i = new Intent(getApplicationContext(),ComposeMsg.class);
                    startActivity(i);
                }

            }
        });

        if(isReadSmsAllowed()) {
            fetchInbox();
        } else {
            requestReadSmsPermission();
            fetchInbox();
        }
        refreshSmsInbox();

    }
    public void refreshSmsInbox() {
        ArrayList<String> stark = new ArrayList<String>();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        do {
            String temp = smsInboxCursor.getString(indexAddress)+":"+smsInboxCursor.getString(indexBody);
            stark.add(temp);
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
        } while (smsInboxCursor.moveToNext());
        parseObj = new SmsParser(stark);
        parseObj.init();
//        Set<String> keys = parseObj.smstable.keySet();
//        for(String key: keys){
//            System.out.println(key);
//        }
    }

    public void fetchInbox() {
        ArrayList<String> stark = new ArrayList<String>();
        Uri inboxURI = Uri.parse("content://sms/inbox");
        String[] reqCols = new String[]{"_id", "address", "body","date"};Uri.parse("content://sms/inbox");

        ContentResolver cr = getContentResolver();
        final Cursor c = cr.query(inboxURI, reqCols, null, null, null);

        adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                new String[]{"body", "address","date"}, new int[]{
                R.id.lblMsg, R.id.lblNumber,R.id.lblDate}, 0);


        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.lblDate) {
                    int getIndex = cursor.getColumnIndex("date");
                    String date = cursor.getString(getIndex);
                    TextView dateTextView = (TextView) view;
                    dateTextView.setText(convertMilliToDate(date));
                    return true;
                }
                return false;
            }
        });

        lvMsg.setAdapter(adapter);
        lvMsg.setOnItemLongClickListener(this);

    }

    public void showMsgOfUserClick(String key) {
        
    }

    public String convertMilliToDate(String milli) {
        Date a = new Date(Long.parseLong(milli));
        SimpleDateFormat obj = new SimpleDateFormat("dd-MM-yy HH:mm:SS");
        String result = obj.format(a);
        return result;

    }

    private boolean isReadSmsAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void requestReadSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_SMS)){
        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_SMS},PERMISSION_SEND_SMS);
    }

    //Requesting permission
    private void requestSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},PERMISSION_SEND_SMS);
    }

    private boolean isComposeMsgAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == PERMISSION_SEND_SMS){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_SendSms) {
            Intent i = new Intent(this,ComposeMsg.class);
            startActivity(i);
            // Handle the camera action
//            Intent intent_nav_camera = new Intent(this,cameraActivity.class);
//            startActivity(intent_nav_camera);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c = (Cursor) adapter.getItem(position);
        String number = c.getString(c.getColumnIndex("address"));
        showMsgOfUserClick(number);
        return true;
    }
//    @Override
//    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//        final Cursor c = (Cursor) adapter.getItem(position);
//        final long cid = c.getLong(c.getColumnIndex("_id"));
//        String message = c.getString(c.getColumnIndex("body"));
//        String number = c.getString(c.getColumnIndex("address"));
//        Log.e("id", cid + "");
//        Log.e("message label", number + "");
//        Log.e("message body", message + "");
//
//
//        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//        alert.setMessage(" Are you sure you want to delete this message?");
//        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
////                deleteSMS(MainActivity.this, cid);
//                fetchInbox();
//
//            }
//
//
//        });
//        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                dialog.dismiss();
//            }
//        });
//        alert.create();
//        alert.show();
//
//        return false;
//    }
}
