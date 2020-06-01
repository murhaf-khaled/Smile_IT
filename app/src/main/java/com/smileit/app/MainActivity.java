package com.smileit.app;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.widget.Toast;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.util.List;

public class MainActivity extends BaseActivity  {

    private LinearLayout llHappy;
    private LinearLayout llSad;
    private LinearLayout llClass;
    private ImageView myImageView;
    private Bitmap myBitmap;
    private Bitmap mutableBitmap;
    private Canvas cnvs;
    private ChipGroup chipGroupHappyFace;
    private ChipGroup chipGroupSadFace;
    private ChipGroup chipGroupLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        llHappy = (LinearLayout) findViewById(R.id.happyLL);
        llSad = (LinearLayout) findViewById(R.id.sadLL);
        llClass = (LinearLayout) findViewById(R.id.labelLL);
        myImageView = findViewById(R.id.imageView);
        chipGroupLabel = (ChipGroup) findViewById(R.id.chipGroupLabel);
        chipGroupHappyFace = (ChipGroup) findViewById(R.id.chipGroupHappyFace);
        chipGroupSadFace = (ChipGroup) findViewById(R.id.chipGroupSadFace);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                case CAMERA:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    llHappy.setVisibility(View.GONE);
                    chipGroupHappyFace.removeAllViews();
                    llSad.setVisibility(View.GONE);
                    chipGroupSadFace.removeAllViews();
                    llClass.setVisibility(View.GONE);
                    chipGroupLabel.removeAllViews();
                    Uri dataUri = data.getData();
                    String path = MyHelper.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = MyHelper.resizePhoto(photoFile, this, dataUri, myImageView);
                    } else {
                        myBitmap = MyHelper.resizePhoto(photoFile, path, myImageView);
                    }
                    if (myBitmap != null) {
                        mutableBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        cnvs=new Canvas(mutableBitmap);
                        cnvs.drawBitmap(mutableBitmap, 0, 0, null);
                        myImageView.setImageBitmap(myBitmap);
                        runFaceDetector(myBitmap);
                        runLandDetector(myBitmap);
                    }
                    break;
                case TAKE_PHOTO:
                    llHappy.setVisibility(View.GONE);
                    chipGroupHappyFace.removeAllViews();
                    llSad.setVisibility(View.GONE);
                    chipGroupSadFace.removeAllViews();
                    llClass.setVisibility(View.GONE);
                    chipGroupLabel.removeAllViews();
                    myBitmap = MyHelper.resizePhoto(photoFile, photoFile.getPath(), myImageView);
                    if (myBitmap != null) {
                        mutableBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        cnvs=new Canvas(mutableBitmap);
                        cnvs.drawBitmap(mutableBitmap, 0, 0, null);
                        myImageView.setImageBitmap(myBitmap);
                        runFaceDetector(myBitmap);
                        runLandDetector(myBitmap);
                    }
                    break;
            }
        }
    }

    private void runLandDetector(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getCloudImageLabeler();

        // Or, to set the minimum confidence required:
        // FirebaseVisionOnDeviceImageLabelerOptions options =
        //     new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
        //         .setConfidenceThreshold(0.7f)
        //         .build();
        // FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
        //     .getOnDeviceImageLabeler(options);

        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {

                        runLandRecog(labels);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,
                                "Exception", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void runFaceDetector(Bitmap bitmap) {

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setMinFaceSize(0.1f)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                runFaceRecog(faces);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(MainActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void runFaceRecog(List<FirebaseVisionFace> faces) {
        //StringBuilder result = new StringBuilder();
        float smilingProbability = 0;

        Paint redPaint=new Paint();
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(10);
        redPaint.setColor(Color.RED);

        Paint greenPaint=new Paint();
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeWidth(10);
        greenPaint.setColor(Color.GREEN);


        for (FirebaseVisionFace face : faces) {
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                smilingProbability = face.getSmilingProbability();
            }

            //result.append("Smile: ");
            if (smilingProbability > 0.5) {
                llHappy.setVisibility(View.VISIBLE);
                cnvs.drawRect(face.getBoundingBox(),greenPaint);
                chipGroupHappyFace.addView(addChipView((int) Math.ceil((smilingProbability) * 100)+" %",cropBitmap(mutableBitmap,face.getBoundingBox())));
            } else {
                llSad.setVisibility(View.VISIBLE);
                cnvs.drawRect(face.getBoundingBox(),redPaint);
                chipGroupSadFace.addView(addChipView((int) Math.ceil((1-smilingProbability) * 100)+" %",cropBitmap(mutableBitmap,face.getBoundingBox())));
            }

        }
        myImageView.setImageBitmap(mutableBitmap);
    }

    private void runLandRecog(List<FirebaseVisionImageLabel> labels) {
        llClass.setVisibility(View.VISIBLE);
        for (FirebaseVisionImageLabel label : labels) {
            String text = label.getText();
            String entityId = label.getEntityId();
            int confidence = (int) Math.ceil((label.getConfidence()) * 100);
            chipGroupLabel.addView(addChipView(text+" ( "+confidence+"% )"));

        }
    }

    private Chip addChipView(String chipText,Bitmap bitmap ) {
        Chip chip = new Chip(this);
        chip.setText(chipText);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        chip.setChipIcon(drawable);
        chip.setCloseIconVisible(false);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

        return chip;
    }

    private Chip addChipView(String chipText) {
        Chip chip = new Chip(this);
        chip.setText(chipText);
        chip.setCloseIconVisible(false);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

        return chip;
    }

    private Bitmap cropBitmap(Bitmap mBit, Rect rect) {
        Bitmap croppedBitmap = Bitmap.createBitmap(mBit, rect.left, rect.top, rect.width(), rect.height());

        return croppedBitmap;
    }


}

