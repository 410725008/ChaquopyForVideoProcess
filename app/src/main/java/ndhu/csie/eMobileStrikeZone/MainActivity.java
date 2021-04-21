package ndhu.csie.eMobileStrikeZone;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.Metrics;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.VideoView;
import android.view.View;
import android.media.MediaExtractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "PythonOnAndroid";
    private static final int DEFAULT_TIMEOUT_US = 10000;


    ImageView iv;
    TextView centerText;
    VideoView videoView;
    BitmapDrawable drawable;
    Bitmap bitmap;
    Uri VideoUri;
    Context c;
    List<byte[]> YUVS = new ArrayList<>();
    List<byte[]> YUVS2 = new ArrayList<>();
    int Video_width;
    int Video_height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getApplicationContext();
        setContentView(R.layout.activity_main);
        iv = (ImageView)findViewById(R.id.img_view);
        centerText = (TextView)findViewById(R.id.CenterTextView);
        initPython();
        //VideoPath = "/storage/emulated/0/Python/A3.mp4";
        //callPythonCode();

    }

    void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
    void callPythonCode() {
        Python py = Python.getInstance();

        PyObject obj1 = py.getModule("hello").callAttr("greet", "Android");
        // 将Python返回值换为Java中的Integer类型
        Float sum = obj1.toJava(Float.class);
        String sum2 = sum.toString();
        centerText.setText(sum2);
    }
    public void displayToast(View v){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,MainActivity.class);
        startActivity(intent);
    }
    public void choose_File_Btn(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);

    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            VideoUri = data.getData();
            centerText.setText(VideoUri.getPath());
            System.out.println(VideoUri);
        }
        else {
            centerText.setText("取消選擇檔案!!");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void start_process_Btn(View v) throws IOException {

        class Thread2 implements Runnable{
            VideoDecoder mVideoDecoder;
            Python py;
            public void setDecoder(VideoDecoder mV){
                mVideoDecoder = mV;
                py = Python.getInstance();
            }
            public void run() {
                Log.d(TAG, "Use Thread2");
                mVideoDecoder.decode(c,VideoUri, new VideoDecoder.DecodeCallback() {
                    @Override
                    public void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs) {
                        Log.d(TAG, "frameCount: " + frameCount + ", presentationTimeUs: " + presentationTimeUs);
                        //PyObject image = py.getModule("new_test").callAttr("test_image", yuv,width,height);

                        // yuv数据操作，例如保存或者再去编码等
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "onFinish");
                    }

                    @Override
                    public void onStop() {
                        Log.d(TAG, "onStop");
                    }
                });
            }
        }

        VideoDecoder mVideoDecoder = new VideoDecoder();
        mVideoDecoder.setOutputFormat(VideoDecoder.COLOR_FORMAT_NV21);
        mVideoDecoder.decode(c,VideoUri, new VideoDecoder.DecodeCallback() {
            @Override
            public void onDecode(byte[] yuv, int width, int height, int frameCount, long presentationTimeUs) {
                Log.d(TAG, "frameCount: " + frameCount + ", presentationTimeUs: " + presentationTimeUs);
                YUVS.add(yuv);
                Video_height = height;
                Video_width = width;
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish");
            }

            @Override
            public void onStop() {
                Log.d(TAG, "onStop");
            }
        });
        Log.d(TAG, "========================YUVS Count: "+String.valueOf(YUVS.size())+"=======================================================");

        Python py = Python.getInstance();
        YUVS2 = new ArrayList<>();
        for(int i=0;i<YUVS.size();i++){
            Log.d(TAG, "========================YUVS Process "+String.valueOf(i+1)+"========================");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            YuvImage yuvimage = new YuvImage(YUVS.get(i), ImageFormat.NV21,Video_width,Video_height,null);
            yuvimage.compressToJpeg(new Rect(0, 0,Video_width,Video_height), 100, baos);
            
            byte[] jdata = baos.toByteArray();
            Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
            ByteBuffer buf = ByteBuffer.allocate(bmp.getByteCount());
            bmp.copyPixelsToBuffer(buf);
            byte[] byteArray = buf.array();
            PyObject image = py.getModule("imageJAVA2PY").callAttr("test_image", byteArray,Video_width,Video_height);
        }
        YUVS = null;
        YUVS = new ArrayList<>();
        //Thread2 thread = new Thread2();
        //thread.setDecoder(mVideoDecoder);
        //thread.run();
    }





}