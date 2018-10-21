package com.amitshekhar.tflite;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amitshekhar.tflite.model.BoxPosition;
import com.amitshekhar.tflite.model.Recognition;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = Config.INPUT_SIZE;

    private TensorFlowImageClassifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private Canvas canvas ;
    private Paint paint;
    private Button mAddImage;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.85f;
    private static int SELECT_IMSGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phot_layout);
        initTensorFlowAndLoadModel();
        mAddImage = findViewById(R.id.addButton);
        mAddImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,SELECT_IMSGE);

            }
        });
        imageViewResult = findViewById(R.id.imageView);
        //initTensorFlowAndLoadModel();
    }
    @Override
    protected void onResume() {
        super.onResume();
//        cameraView.start();
    }

    @Override
    protected void onPause() {
//        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets());
//                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_IMSGE && resultCode == RESULT_OK && data !=null){
            Uri selectedImage = data.getData();
//            String [] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn,null,null,null);
            ImageView imageView = findViewById(R.id.imageView);
            Bitmap bmp = null;
            Bitmap bitmap;
            String resultName = "";
            BoxPosition rescaledLocation= null;
            try{
                InputStream is = getContentResolver().openInputStream(selectedImage);
                bmp = BitmapFactory.decodeStream(is);
                bitmap = bmp;
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                final List<Recognition> results = classifier.recognizeImage(bitmap);


                for (final Recognition result : results) {
                    final BoxPosition location = result.getLocation();
                    if (location != null
                            ) {
                         if(result.getTitle().contains("person")) {
                             resultName += result.getTitle() + " ";
                           //  paint.setColor(R.color.colorPrimaryDark);
                             rescaledLocation = location;

                         }
                    }
                }
                RectF personRectangle = null;
                if(rescaledLocation!=null){

                    personRectangle =  rescaledLocation.reCalc(rescaledLocation);

//                    int x = Math.abs((int)rescaledLocation.getLeft()),
//                      y = Math.abs((int)rescaledLocation.getTop()),
//                        width = Math.abs((int) rescaledLocation.getWidth())> bmp.getWidth()?bmp.getWidth():Math.abs((int) rescaledLocation.getWidth()),
//                        height = Math.abs((int)rescaledLocation.getHeight()) > bmp.getHeight()? bmp.getHeight() :Math.abs((int)rescaledLocation.getHeight());
                    int YPadding = Math.abs((int)personRectangle.top )> 150?150:0;;

                    int x = Math.abs((int)personRectangle.left ),
                      y = Math.abs((int)personRectangle.top )-YPadding,
                            width = Math.abs(Math.abs((int)personRectangle.right) - Math.abs(personRectangle.left))> bmp.getWidth()?
                                    bmp.getWidth():
                                    Math.abs(Math.abs((int)personRectangle.right) - Math.abs((int)personRectangle.left)),
                        height = Math.abs(Math.abs((int)personRectangle.bottom) - Math.abs(personRectangle.top) ) > bmp.getHeight()?
                                bmp.getHeight() :
                                Math.abs(Math.abs((int)personRectangle.bottom) - Math.abs((int)personRectangle.top));

//                    Bitmap croppedBitmap = Bitmap.createBitmap(bmp,x-5,y-5,bmp.getWidth()-x,bmp.getHeight()-y);
                    Bitmap croppedBitmap = Bitmap.createBitmap(bmp,x,y,Math.min(bmp.getHeight() - y ,bmp.getWidth()-x),Math.min(bmp.getHeight() - y ,bmp.getWidth()-x) );

                    imageViewResult.setImageBitmap(croppedBitmap);

                }
                else {
                    imageViewResult.setImageBitmap(bmp);
                }


            }catch (FileNotFoundException e ){
                Log.e("MAINACTIVITY",e.getMessage());
            }
            //imageView.setImageBitmap(bmp);

        }
    }

}
