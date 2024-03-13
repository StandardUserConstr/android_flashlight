package com.example.test000;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Camera cam_old;
    private Camera.Parameters parameters_old;
    private boolean old_cam_supports_torch;

    private CameraManager cam_new;
    private String cam_new_id;

    private ImageView image_light;
    private ImageView image_view_anim;

    private Animation animation_buttom_up;
    private Animation animation_up_buttom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        image_light = (ImageView)findViewById(R.id.imageView4);
        image_view_anim = (ImageView)findViewById(R.id.imageView3);
        image_light.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            cam_old = Camera.open();
            parameters_old = cam_old.getParameters();
            List<String> modes = parameters_old.getSupportedFlashModes();
            if (modes.contains(Camera.Parameters.FLASH_MODE_TORCH)) old_cam_supports_torch = true;
            else if (modes.contains(Camera.Parameters.FLASH_MODE_ON)) old_cam_supports_torch = false;
            else
            {
                showNoFlashError();
            }
        }
        else
        {
            boolean isFlashAvailable = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

            if (!isFlashAvailable) showNoFlashError();

            cam_new = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try
            {
                cam_new_id = cam_new.getCameraIdList()[0];
            }
            catch (CameraAccessException e)
            {
                e.printStackTrace();
            }
        }

        animation_buttom_up = AnimationUtils.loadAnimation(this, R.anim.from_button_to_up_anim);
        animation_up_buttom = AnimationUtils.loadAnimation(this, R.anim.from_up_to_button_anim);

        image_view_anim.setOnClickListener(v -> {
            if(image_light.getVisibility()==View.INVISIBLE)
            {
                v.startAnimation(animation_buttom_up);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                {
                    image_light.setVisibility(View.VISIBLE);
                    if(old_cam_supports_torch==true) parameters_old.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    else parameters_old.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    Objects.requireNonNull(cam_old).setParameters(parameters_old);
                    Objects.requireNonNull(cam_old).startPreview();
                }
                else
                {
                    image_light.setVisibility(View.VISIBLE);
                    try
                    {
                        cam_new.setTorchMode(cam_new_id, true);
                    }
                    catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
            else
            {
                v.startAnimation(animation_up_buttom);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                {
                    image_light.setVisibility(View.INVISIBLE);
                    parameters_old.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    Objects.requireNonNull(cam_old).setParameters(parameters_old);
                    Objects.requireNonNull(cam_old).startPreview();
                }
                else
                {
                    image_light.setVisibility(View.INVISIBLE);
                    try
                    {
                        cam_new.setTorchMode(cam_new_id, false);
                    }
                    catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }

            }

        });


    }

    public void showNoFlashError()
    {
        AlertDialog alert = new AlertDialog.Builder(this)
                .create();
        alert.setTitle("Oops!");
        alert.setMessage("FlashLight is not available in this device");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    @Override
    public void onDestroy()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            Objects.requireNonNull(cam_old).stopPreview();
            Objects.requireNonNull(cam_old).release();
        }
        super.onDestroy();
        finish();
    }
}
