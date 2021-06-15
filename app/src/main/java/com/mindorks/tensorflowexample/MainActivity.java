/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.tensorflowexample;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mindorks.tensorflowexample.view.DrawModel;
import com.mindorks.tensorflowexample.view.DrawView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    TextView textView;
    List<String> pictures;
    private TextView textView2;
    private static final String TAG = "MainActivity";
    Boolean a = true;

    private static final int PIXEL_WIDTH = 500;
    private static final int PIXEL_HEIGHT = 300;

    private TextView mResultText;

    private float mLastX;

    private float mLastY;

    private DrawModel mModel;
    private DrawView mDrawView;

    private View detectButton;

    private PointF mTmpPoint = new PointF();

    private static final int INPUT_SIZE = 28;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/mnist_model_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/graph_label_strings.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mModel = new DrawModel(PIXEL_HEIGHT, PIXEL_WIDTH);

        mDrawView = (DrawView) findViewById(R.id.view_draw);
        mDrawView.setModel(mModel);
        mDrawView.setOnTouchListener(this);

        detectButton = findViewById(R.id.buttonDetect);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetectClicked();
            }
        });

        View clearButton = findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });

        mResultText = (TextView) findViewById(R.id.textResult);

        initTensorFlowAndLoadModel();
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                    Log.d(TAG, "Load Success");
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
                detectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        mDrawView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDrawView.onPause();
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        mDrawView.calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mDrawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        mDrawView.invalidate();
    }

    private void processTouchUp() {
        mModel.endLine();
    }

    private void onDetectClicked() {
        float pixels[] = mDrawView.getPixelData();

        final List<Classifier.Recognition> results = classifier.recognizeImage(pixels);

        if (results.size() > 0) {
            String value = " Number is : " +results.get(0).getTitle();
            mResultText.setText(value);
        }

    }

    private void onClearClicked() {
        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();

        mResultText.setText("");
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
    public void onClick(final View v) {
        textView2 = (TextView) findViewById(R.id.textView2);
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                textView2.setText("         "
                        + millisUntilFinished / 1000+"       ");
            }
            public void onFinish() {
                textView2.setText("");
            }
        }
                .start();
        Button btn = (Button)   findViewById(R.id.button);
        v.setVisibility(View.INVISIBLE);
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.VISIBLE);
            }
        }, 10 * 1000);
        textView = (TextView) findViewById(R.id.textVew);
        pictures = new ArrayList<>();
        pictures.add("1");
        pictures.add("2");
        pictures.add("3");
        pictures.add("4");
        pictures.add("5");
        pictures.add("6");
        pictures.add("7");
        pictures.add("8");
        pictures.add("9");
        pictures.add("10");
        pictures.add("11");
        pictures.add("12");
        pictures.add("13");
        pictures.add("14");
        pictures.add("15");
        pictures.add("16");
        pictures.add("17");
        pictures.add("18");
        pictures.add("19");
        pictures.add("20");
        pictures.add("21");
        pictures.add("22");
        pictures.add("23");
        pictures.add("24");
        pictures.add("25");
        pictures.add("26");
        pictures.add("27");
        pictures.add("28");
        pictures.add("29");
        pictures.add("30");


        Collections.shuffle(pictures);
        textView.setVisibility(View.VISIBLE);
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
            }
        }, 10 * 1000);
        if (a) {
            String current = pictures.get(0);
            if (current.equals("1")) {
                textView.setText("  Happy Smile");
            }  if (current.equals("2")) {
                textView.setText("  Sad Smile");
            }  if (current.equals("3")) {
                textView.setText("  Wi - Fi");
            }  if (current.equals("4")) {
                textView.setText("  Smartphone");
            }  if (current.equals("5")) {
                textView.setText("  Hockey stick");
            }  if (current.equals("6")) {
                textView.setText("  Flower");
            }  if (current.equals("7")) {
                textView.setText("  Remote Controller");
            }  if (current.equals("8")) {
                textView.setText("  Ice Cream");
            }  if (current.equals("9")) {
                textView.setText("  Pizza");
            }  if (current.equals("10")) {
                textView.setText("  Lion");
            }  if (current.equals("11")) {
                textView.setText("  Sun");
            }  if (current.equals("12")) {
                textView.setText("  Knife");
            }  if (current.equals("13")) {
                textView.setText("  Cat");
            }  if (current.equals("14")) {
                textView.setText("  Fish");
            }  if (current.equals("15")) {
                textView.setText("  Door");
            }  if (current.equals("16")) {
                textView.setText("  Train");
            }  if (current.equals("17")) {
                textView.setText("  Ping - Pong Racket");
            }  if (current.equals("18")) {
                textView.setText("  Snowman");
            }  if (current.equals("19")) {
                textView.setText("  Palm");
            }  if (current.equals("20")) {
                textView.setText("  Donut");
            } if (current.equals("21")) {
                textView.setText("  Car");
            }if (current.equals("22")) {
                textView.setText("  Ball");
            } if (current.equals("23")) {
                textView.setText("  Ship");
            }  if (current.equals("24")) {
                textView.setText("  Banana");
            } if (current.equals("25")) {
                textView.setText("  TV");
            } if (current.equals("26")) {
                textView.setText("  Plane");
            } if (current.equals("27")) {
                textView.setText("  House");
            } if (current.equals("28")) {
                textView.setText("  Jail");
            } if (current.equals("29")) {
                textView.setText("  Broom");
            } if (current.equals("30")) {
                textView.setText("  Trousers");

                //спользуется рандомное слово в array list, но по нажатию на text view. ужно сделать, что бы каждые 10 сек слово менялось
            }
        }
    }

}

