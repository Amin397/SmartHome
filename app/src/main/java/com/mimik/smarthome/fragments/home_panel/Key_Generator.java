package com.mimik.smarthome.fragments.home_panel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mimik.smarthome.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.content.Context.WINDOW_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class Key_Generator extends Fragment {


    private TextInputEditText txt_duration_QR;
    private Button btn_QR , btn_Temp_pass;
    private ImageView img_QR;
    private TextView txt_Temp_pass;
    private DatePicker start_date;
    private TextInputLayout L_duration;
    private String inputValue;

    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;
    private FloatingActionButton fab_share;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.key_generator , container , false);

        initViews(view);

        btn_QR.setOnClickListener(mQrCodeGeneratorClickListener);

        return view;
    }

    private Button.OnClickListener mQrCodeGeneratorClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (txt_duration_QR.getText().toString().isEmpty()){
                YoYo.with(Techniques.Shake)
                        .duration(1000)
                        .repeat(0)
                        .playOn(L_duration);

                L_duration.setErrorEnabled(true);
                L_duration.setError("Duration Field is empty !");
            }else {
                generating_QR();
            }
        }
    };

    private void generating_QR() {
        img_QR.setVisibility(View.VISIBLE);
        btn_QR.setVisibility(View.GONE);
        btn_Temp_pass.setVisibility(View.GONE);
        fab_share.setVisibility(View.VISIBLE);

        inputValue = txt_duration_QR.getText().toString().trim() +"_"+start_date.getDayOfMonth() +"-"+ start_date.getMonth() +"-"+ start_date.getYear();
        if (inputValue.length() > 0) {
            WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                img_QR.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews(View view) {
        txt_duration_QR = (TextInputEditText) view.findViewById(R.id.txt_duration_id);
        btn_QR = (Button) view.findViewById(R.id.btn_qr_code_generate_id);
        btn_Temp_pass = (Button) view.findViewById(R.id.btn_temp_password_generate_id);
        img_QR = (ImageView) view.findViewById(R.id.img_qr_code_id);
        txt_Temp_pass = (TextView) view.findViewById(R.id.txt_temp_password_id);
        start_date = (DatePicker) view.findViewById(R.id.datepicker_qr_id);
        L_duration = (TextInputLayout) view.findViewById(R.id.l_edittext);
        fab_share = (FloatingActionButton) view.findViewById(R.id.fab_share_id);
    }
}
