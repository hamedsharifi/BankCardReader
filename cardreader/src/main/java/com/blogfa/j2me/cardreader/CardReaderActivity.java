package com.blogfa.j2me.cardreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSobelThresholdFilter;

public class CardReaderActivity extends AppCompatActivity {

    static final String TESSBASE_PATH = Environment.getExternalStorageDirectory().toString();
    static final String DEFAULT_LANGUAGE = "eng";
    CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);
        getSupportActionBar().hide();
        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setLifecycleOwner(this);
        final Processor processor = new Processor();

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    cameraView.addFrameProcessor(processor);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }

        }).check();
    }

    private void detectText(final Bitmap bitmap, int processedFrames) {
        try {
            final TessBaseAPI baseApi = new TessBaseAPI();
            boolean success = baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);

            baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!?@#$%&*()<>_-+=.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "/0123456789");
            baseApi.setVariable("classify_bln_numeric_mode", "1");

            GPUImageFilterGroup group = new GPUImageFilterGroup();
            group.addFilter(new GPUImageSharpenFilter());
            group.addFilter(new GPUImageSharpenFilter());
            group.addFilter(new GPUImageSharpenFilter());
            group.addFilter(new GPUImageMonochromeFilter());
            /*if (processedFrames < 8)
                group.addFilter(new GPUImageMonochromeFilter());
            else if (processedFrames % 3 == 0)
                group.addFilter(new GPUImageMonochromeFilter());*/
//            if (processedFrames % 2 == 0)
//                group.addFilter(new GPUImageSobelThresholdFilter());
            GPUImage gpuImage = new GPUImage(CardReaderActivity.this);
            gpuImage.setImage(bitmap);
            gpuImage.setFilter(group);
            Bitmap result = gpuImage.getBitmapWithFilterApplied();
            baseApi.setImage(result);
            String recognizedText = baseApi.getUTF8Text();
            String card = "";
            String expDate = "";
            if (recognizedText.length() > 16) {
                String strings[] = recognizedText.split("\n");
                for (int i = 0; i < strings.length; i++) {
                    if (card.equals(""))
                        card = detectBankCard(strings[i].trim().replaceAll(" ", ""));
                    if (expDate.equals(""))
                        expDate = detectExpireDate(strings[i]);
                }
                if (!card.equals("")) {
                    final String finalCard = card;
                    final String finalExpDate = expDate;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*Intent intent = new Intent();
                            intent.putExtra("card", finalCard + ";" + finalExpDate);
                            setResult(1000, intent);
                            finish();*/
                            Toast.makeText(CardReaderActivity.this, finalCard + ";" + finalExpDate, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                return;
            }
            baseApi.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fetchCardFromString(String raw) {
        if (raw.length() == 16) {
            return raw;
        } else if (raw.length() > 16) {
            return raw.substring(0, 16);
        }
        return "";
    }

    class Processor implements FrameProcessor {
        private long lastTime = System.currentTimeMillis();
        private int processedCount = 0;

        @Override
        public void process(@NonNull Frame frame) {
            /*long newTime = frame.getTime();
            long delay = newTime - lastTime;
            if (delay < 200)
                if (processedCount != 0) {
                    return;
                } else {
                    processedCount += 1;
                }
            processedCount += 1;
            lastTime = newTime;*/
            if (frame.getFormat() == ImageFormat.NV21 && frame.getDataClass() == byte[].class) {
                byte[] data = frame.getData();
                YuvImage yuvImage = new YuvImage(data, frame.getFormat(), frame.getSize().getWidth(), frame.getSize().getHeight(), null);
                ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0,
                        frame.getSize().getWidth(),
                        frame.getSize().getHeight()), 100, jpegStream);
                byte[] jpegByteArray = jpegStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpegByteArray,
                        0, jpegByteArray.length);
                detectText(bitmap, processedCount);
            }
        }
    }

    private int countGivenCharacter(String st, Character character) {
        int length = 0;
        for (int i = 0; i < st.length(); i++) {
            if (st.charAt(i) == character)
                length += 1;
        }
        return length;
    }

    private String detectBankCard(String cardNumber) {
        int index = 0;
        List<String> list = new ArrayList<>();
        list.add("627412");
        list.add("207177");
        list.add("627381");
        list.add("502229");
        list.add("505785");
        list.add("502806");
        list.add("622106");
        list.add("502908");
        list.add("639194");
        list.add("502910");
        list.add("627884");
        list.add("502938");
        list.add("639347");
        list.add("505416");
        list.add("505801");
        list.add("627353");
        list.add("589210");
        list.add("589463");
        list.add("603769");
        list.add("603770");
        list.add("636949");
        list.add("603799");
        list.add("610433");
        list.add("621986");
        list.add("639607");
        list.add("639370");
        list.add("581874");
        list.add("628023");
        list.add("606373");
        list.add("627760");
        list.add("627961");
        list.add("639346");
        list.add("639348");

        for (int i = 0; i < list.size(); i++) {
            if (cardNumber.contains(list.get(i))) {
                index = cardNumber.indexOf(list.get(i));
                String card = cardNumber.trim().replaceAll(" ", "");
                try {
                    return card.substring(index, index + 17);
                } catch (Exception e) {
                    if (card.substring(index).length() == 16) {
                        return card.substring(index);
                    } else {
                        return "";
                    }
                }
            }
        }
        return "";
    }

    private String detectExpireDate(String st) {
        String result;
        int index = st.lastIndexOf("/");
        if (index >= 2) {
            try {
                result = st.trim();
                result = result.substring(index - 2, index) + result.substring(index + 1, index + 3).replaceAll("\\D", "");
                if (result.length() == 4) {
                    if (Integer.parseInt(result.substring(2)) > 0 && Integer.parseInt(result.substring(2)) < 13) {
                        return result;
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}
