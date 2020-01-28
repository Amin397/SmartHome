package com.mimik.smarthome.userinterface.common;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mimik.smarthome.R;


//CHECKED
public class ConfirmationActivity extends Activity {
    private TextView txtTitle;
    private TextView lblMessage;
    private Button btnOk;
    private Button btnCancel;

    private String _message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_confirmation);

        this.setFinishOnTouchOutside(false);

        _message = getIntent().getStringExtra("Message");

        initializeComponent();
    }

    private void initializeComponent() {
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        lblMessage = (TextView)findViewById(R.id.lblMessage);
        btnOk = (Button)findViewById(R.id.btnOk);
        btnCancel = (Button)findViewById(R.id.btnCancel);

        lblMessage.setText(_message);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(1);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(0);
                finish();
            }
        });

        setCustomFonts();

    }

    private void setCustomFonts() {
        String fontPath = "fonts/vazir_bold.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), fontPath);

        txtTitle.setTypeface(typeface);
        lblMessage.setTypeface(typeface);
        btnOk.setTypeface(typeface);
        btnCancel.setTypeface(typeface);
    }
}
