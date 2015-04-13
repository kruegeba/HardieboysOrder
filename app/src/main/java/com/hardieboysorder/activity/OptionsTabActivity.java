package com.hardieboysorder.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hardieboysorder.R;
import com.hardieboysorder.db.HardieboysOrderDB;
import com.hardieboysorder.model.OutputRow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class OptionsTabActivity extends Activity {

    HardieboysOrderDB db;
    RadioButton todayRadioButton, rangeRadioButton;
    Button startButton, endButton, emailButton;
    static TextView startTextView, endTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.options_tab_activity);

        db = new HardieboysOrderDB(this);
        initializeViews();
        checkRadioButtons();

       /* Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream fos;
                BufferedReader br;
                try {
                    File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+File.separator+"MyFile2.txt");
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
                    bufferedWriter.write(createHeaderRow());
                    ArrayList<OutputRow> outputRows = db.getOutputRows();

                    for (int i = 0; i < outputRows.size(); i++) {
                        outputRows.get(i).setNameCode(getContactNickname(outputRows.get(i).getContactID()));
                        bufferedWriter.write(outputRows.get(i).toString());
                        bufferedWriter.write("\n");
                    }
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    sendEmailWithAttachment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

    }

    private void initializeViews(){
        todayRadioButton = (RadioButton)findViewById(R.id.todayRadioButton);
        todayRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRadioButtons();
            }
        });
        rangeRadioButton = (RadioButton)findViewById(R.id.rangeRadioButton);
        rangeRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRadioButtons();
            }
        });
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePickerDialog(v);
            }
        });
        endButton = (Button)findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickerDialog(v);
            }
        });
        emailButton = (Button)findViewById(R.id.emailButton);
        startTextView = (TextView)findViewById(R.id.startTextView);
        endTextView = (TextView)findViewById(R.id.endTextView);
    }

    private String createHeaderRow() {
        String[] headerRowTitles = {"OurRef", "TransDate", "Type", "NameCode", "Gross", "Detail.Net", "Detail.StockCode", "Detial.StockQty", "Detail.Discount"};
        String headerRow = "";

        for (int i = 0; i < headerRowTitles.length; i++) {
            headerRow += headerRowTitles[i];

            if (i != headerRowTitles.length - 1) {
                headerRow += "\t";
            }
        }

        headerRow += "\n";

        return headerRow;
    }

    private String getContactNickname(int contactId) {
        String nickname;

        if(contactId <= 0){
            return "unknown";
        }

        try {
            Cursor cur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.DATA1}, ContactsContract.Data.CONTACT_ID + " = " + contactId, null, null);

            int nicknameIndex = cur.getColumnIndex(ContactsContract.Data.DATA1);

            if (cur.moveToFirst()) {
                nickname = cur.getString(nicknameIndex);
            } else {
                return "unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }

        return nickname;
    }

    private void sendEmailWithAttachment(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"ben.adams.krueger@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");

        String root = Environment.DIRECTORY_DOCUMENTS;
        File root2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String pathToMyAttachedFile = "MyFile2.txt";
        File file = new File(root2, pathToMyAttachedFile);
        try {
            if (!file.exists() || !file.canRead()) {
                return;
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String read;
            StringBuilder builder = new StringBuilder("");

            while ((read = bufferedReader.readLine()) != null) {
                builder.append(read);
            }
            bufferedReader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        Uri uri = Uri.fromFile(file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

    private void showStartDatePickerDialog(View v){
        DialogFragment newFragment = new StartDatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void showEndDatePickerDialog(View v){
        DialogFragment newFragment = new EndDatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void checkRadioButtons(){
        if(todayRadioButton.isChecked()){
            startButton.setEnabled(false);
            endButton.setEnabled(false);
            startTextView.setEnabled(false);
            endTextView.setEnabled(false);
        }else{
            startButton.setEnabled(true);
            endButton.setEnabled(true);
            startTextView.setEnabled(true);
            endTextView.setEnabled(true);
        }
    }

    public static class StartDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstance){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            startTextView.setText(day + "-" + month + "-" + year);
        }
    }

    public static class EndDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstance){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            endTextView.setText(day + "-" + month + "-" + year);
        }
    }
}
