package com.yalantis.ucrop.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.util.Locale;
import java.util.Random;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
public class SampleActivity extends BaseActivity {

    private static final String TAG = "SampleActivity";

    private static final int REQUEST_SELECT_PICTURE = 0x01;

    private RadioGroup mRadioGroupAspectRatio, mRadioGroupCompressionSettings;
    private EditText mEditTextMaxWidth, mEditTextMaxHeight;
    private EditText mEditTextRatioX, mEditTextRatioY;
    private CheckBox mCheckBoxMaxSize;
    private SeekBar mSeekBarQuality;
    private TextView mTextViewQuality;
    private CheckBox mCheckBoxHideBottomControls;
    private CheckBox mCheckBoxFreeStyleCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        setupUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    CropUtil.startCropActivity(SampleActivity.this, data.getData());
                } else {
                    Toast.makeText(SampleActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupUI() {
        findViewById(R.id.button_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
        findViewById(R.id.button_random_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int minSizePixels = 800;
                int maxSizePixels = 2400;
                CropUtil.startCropActivity(SampleActivity.this, Uri.parse(String.format(Locale.getDefault(), "https://unsplash.it/%d/%d/?random",
                        minSizePixels + random.nextInt(maxSizePixels - minSizePixels),
                        minSizePixels + random.nextInt(maxSizePixels - minSizePixels))));
            }
        });

        mRadioGroupAspectRatio = ((RadioGroup) findViewById(R.id.radio_group_aspect_ratio));
        mRadioGroupCompressionSettings = ((RadioGroup) findViewById(R.id.radio_group_compression_settings));
        mCheckBoxMaxSize = ((CheckBox) findViewById(R.id.checkbox_max_size));
        mEditTextRatioX = ((EditText) findViewById(R.id.edit_text_ratio_x));
        mEditTextRatioY = ((EditText) findViewById(R.id.edit_text_ratio_y));
        mEditTextMaxWidth = ((EditText) findViewById(R.id.edit_text_max_width));
        mEditTextMaxHeight = ((EditText) findViewById(R.id.edit_text_max_height));
        mSeekBarQuality = ((SeekBar) findViewById(R.id.seekbar_quality));
        mTextViewQuality = ((TextView) findViewById(R.id.text_view_quality));
        mCheckBoxHideBottomControls = ((CheckBox) findViewById(R.id.checkbox_hide_bottom_controls));
        mCheckBoxFreeStyleCrop = ((CheckBox) findViewById(R.id.checkbox_freestyle_crop));

        mRadioGroupAspectRatio.check(R.id.radio_dynamic);
        mEditTextRatioX.addTextChangedListener(mAspectRatioTextWatcher);
        mEditTextRatioY.addTextChangedListener(mAspectRatioTextWatcher);
        mRadioGroupCompressionSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mSeekBarQuality.setEnabled(checkedId == R.id.radio_jpeg);
            }
        });
        mRadioGroupCompressionSettings.check(R.id.radio_jpeg);
        mSeekBarQuality.setProgress(UCropActivity.DEFAULT_COMPRESS_QUALITY);
        mTextViewQuality.setText(String.format(getString(R.string.format_quality_d), mSeekBarQuality.getProgress()));
        mSeekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewQuality.setText(String.format(getString(R.string.format_quality_d), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private TextWatcher mAspectRatioTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mRadioGroupAspectRatio.clearCheck();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
//            Intent i = new Intent(
//                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(i, REQUEST_SELECT_PICTURE);
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_SELECT_PICTURE);
        }
    }


    private void handleCropResult(@NonNull Intent result) {
        //得到结果的uri
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            //传送到结果界面预览
            ResultActivity.startWithUri(SampleActivity.this, resultUri);
        } else {
            Toast.makeText(SampleActivity.this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(SampleActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(SampleActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

}
