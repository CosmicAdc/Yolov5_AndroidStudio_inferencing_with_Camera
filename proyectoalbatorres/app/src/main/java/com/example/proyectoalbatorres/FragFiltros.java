package com.example.proyectoalbatorres;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Arrays;

public class FragFiltros extends Fragment {
    public FragFiltros() {
        // Constructor vacío requerido.
    }
    static {
        System.loadLibrary("proyectoalbatorres");
    }
    private android.widget.Spinner opciones;

    private android.widget.ImageView  imagenFiltros;

    private SeekBar SMediana,SsigmaX,SsigmaY,SGausX,SGausY,SBlurX,SBlurY;

    public int valorMediana = 1, valorSigmaX = 1, valorSigmaY =1, valorGausX = 1, valorGausY = 1, valorBlurX = 1, valorBlurY = 1;

    private TextView ValMediana,ValSigmaX,ValSigmaY,ValGausX,ValGausY,ValBlurX,ValBlurY;

    private TextView TXValMediana,TXValSigmaX,TXValSigmaY,TXValGausX,TXValGausY,TXValBlurX,TXValBlurY;

    private TextureView mTextureView;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CameraCaptureSession mCameraCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    int f=0;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        View rootView = inflater.inflate(R.layout.filtros, container, false);
        opciones=rootView.findViewById(R.id.spinner);

        String[] filtros = {"Mediana", "Blur", "Gaussiano","SobelX","SobelY","Gradiente"};
        FragmentActivity actividad=requireActivity();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(actividad, android.R.layout.simple_spinner_item,filtros);
        opciones.setAdapter(adapter);
        imagenFiltros=rootView.findViewById(R.id.imageView5);
        SMediana=rootView.findViewById(R.id.seekBar);
        SsigmaX=rootView.findViewById(R.id.seekBar2);
        SsigmaY=rootView.findViewById(R.id.seekBar3);
        SGausX=rootView.findViewById(R.id.seekBar4);
        SGausY=rootView.findViewById(R.id.seekBar5);
        SBlurX=rootView.findViewById(R.id.seekBar6);
        SBlurY=rootView.findViewById(R.id.seekBar7);
        ValMediana=rootView.findViewById(R.id.valorMediana);
        ValGausX=rootView.findViewById(R.id.ValorGausX);
        ValGausY=rootView.findViewById(R.id.ValorGausY);
        ValBlurX=rootView.findViewById(R.id.ValorBlurX);
        ValBlurY=rootView.findViewById(R.id.BlurYValor);
        ValSigmaX=rootView.findViewById(R.id.SigmaXValor);
        ValSigmaY=rootView.findViewById(R.id.SigmaYValor);

        TXValBlurY=rootView.findViewById(R.id.BlurYTexto);
        TXValBlurX=rootView.findViewById(R.id.BlurXTexto);
        TXValGausX=rootView.findViewById(R.id.GausXTexto);
        TXValGausY=rootView.findViewById(R.id.GausYTexto);
        TXValSigmaX=rootView.findViewById(R.id.SigmaXTexto);
        TXValSigmaY=rootView.findViewById(R.id.SigmaYTexto);
        TXValMediana=rootView.findViewById(R.id.MedianaTexto);
        mTextureView = rootView.findViewById(R.id.textureView10);

        setSliderVisibility(true, false, false, false, false, false,false);

        opciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String opcion= opciones.getSelectedItem().toString();
                if (opcion.equals("Mediana")) {
                    setSliderVisibility(true, false, false, false, false, false,false);
                    f=0;
                } else if (opcion.equals("Blur")) {
                    setSliderVisibility(false, false, false, false, false, true,true);
                    f=1;
                } else if (opcion.equals("Gaussiano")) {
                    setSliderVisibility(false, true, true, true, true, false,false);
                   f=2;
                } else if (opcion.equals("SobelX")) {
                    setSliderVisibility(false, false, false, false, false, false,false);
                    f=3;
                }else if (opcion.equals("SobelY")) {
                    setSliderVisibility(false, false, false, false, false, false,false);
                    f=4;
                }else if (opcion.equals("Gradiente")) {
                    setSliderVisibility(false, false, false, false, false, false,false);
                    f=5;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Bitmap bIn =
                        BitmapFactory.decodeResource(getResources(),R.drawable.ima1);
                Bitmap bOut = bIn.copy(bIn.getConfig(), true);

                AplicaFiltros(bIn, bOut,0,valorMediana,valorSigmaX,valorSigmaY,valorBlurX,valorBlurY,valorGausX,valorGausY);

            }
        });

        SMediana.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress % 2 == 0) {
                    SMediana.setProgress(progress + 1);
                }
                valorMediana = SMediana.getProgress();
                ValMediana.setText(String.valueOf(SMediana.getProgress()));
                f=0;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SGausX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress % 2 == 0) {
                    SGausX.setProgress(progress + 1);
                }

                valorGausX = SGausX.getProgress();
                ValGausX.setText(String.valueOf(SGausX.getProgress()));
                f=2;

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        SGausY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress % 2 == 0) {
                    SGausY.setProgress(progress + 1);
                }
                valorGausY = SGausY.getProgress();
                ValGausY.setText(String.valueOf(SGausY.getProgress()));
                f=2;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SBlurX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress % 2 == 0) {
                    SBlurX.setProgress(progress + 1);
                }
                valorBlurX = SBlurX.getProgress();
                ValBlurX.setText(String.valueOf(SBlurX.getProgress()));
                f=1;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        SBlurY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress % 2 == 0) {
                    SBlurY.setProgress(progress + 1);
                }
                valorBlurY = SBlurY.getProgress();
                ValBlurY.setText(String.valueOf(SBlurY.getProgress()));
                f=1;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SsigmaX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valorSigmaX = SsigmaX.getProgress();
                ValSigmaX.setText(String.valueOf(SsigmaX.getProgress()));
                f=2;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SsigmaY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valorSigmaY = SsigmaY.getProgress();
                ValSigmaY.setText(String.valueOf(SsigmaY.getProgress()));
                f=2;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return rootView;
    }
    public native void AplicaFiltros(Bitmap in, Bitmap out, int opcion, int vMed, int vSX, int vSY, int vBX, int vBY,int vGX, int vGY);
    private void setSliderVisibility(boolean mediana ,boolean sigmaX, boolean sigmaY, boolean gausX,
                                     boolean gausY, boolean BlurX, boolean BlurY) {
        SMediana.setVisibility(mediana ? View.VISIBLE : View.GONE);
        TXValMediana.setVisibility(mediana ? View.VISIBLE : View.GONE);
        ValMediana.setVisibility(mediana ? View.VISIBLE : View.GONE);

        SsigmaX.setVisibility(sigmaX ? View.VISIBLE : View.GONE);
        TXValSigmaX.setVisibility(sigmaX ? View.VISIBLE : View.GONE);
        ValSigmaX.setVisibility(sigmaX ? View.VISIBLE : View.GONE);

        SsigmaY.setVisibility(sigmaY ? View.VISIBLE : View.GONE);
        TXValSigmaY.setVisibility(sigmaY ? View.VISIBLE : View.GONE);
        ValSigmaY.setVisibility(sigmaY ? View.VISIBLE : View.GONE);

        SGausX.setVisibility(gausX ? View.VISIBLE : View.GONE);
        TXValGausX.setVisibility(gausX ? View.VISIBLE : View.GONE);
        ValGausX.setVisibility(gausX ? View.VISIBLE : View.GONE);

        SGausY.setVisibility(gausY ? View.VISIBLE : View.GONE);
        TXValGausY.setVisibility(gausY ? View.VISIBLE : View.GONE);
        ValGausY.setVisibility(gausY ? View.VISIBLE : View.GONE);

        SBlurX.setVisibility(BlurX ? View.VISIBLE : View.GONE);
        TXValBlurX.setVisibility(BlurX ? View.VISIBLE : View.GONE);
        ValBlurX.setVisibility(BlurX ? View.VISIBLE : View.GONE);

        SBlurY.setVisibility(BlurY ? View.VISIBLE : View.GONE);
        TXValBlurY.setVisibility(BlurY ? View.VISIBLE : View.GONE);
        ValBlurY.setVisibility(BlurY ? View.VISIBLE : View.GONE);
        // Configura la visibilidad de los demás sliders
    }
    private void AplicoF(int f){
        Bitmap bIn =
                BitmapFactory.decodeResource(getResources(),R.drawable.ima1);
        Bitmap bOut = bIn.copy(bIn.getConfig(), true);

        Log.d(TAG, "VM"+valorMediana+"VM"+valorSigmaX+"F"+f);

        AplicaFiltros(bIn, bOut,f,valorMediana,valorSigmaX,valorSigmaY,valorBlurX,valorBlurY,valorGausX,valorGausY);

        imagenFiltros.setImageBitmap(bOut);
    }
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            Size previewSize = chooseOptimalSize();
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (mCameraDevice == null) {
                                return;
                            }

                            mCameraCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size chooseOptimalSize() {
        // Implement logic to choose optimal preview size based on available sizes.
        // You can get available sizes using CameraCharacteristics.
        return new Size(1920, 1080); // Default size, you may need to change this.
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Bitmap bitmap = mTextureView.getBitmap();
            processFrame(bitmap);
        }
    };
    private void processFrame(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        AplicaFiltros(bitmap, bitmap,f,valorMediana,valorSigmaX,valorSigmaY,valorBlurX,valorBlurY,valorGausX,valorGausY);
        Bitmap bOut = bitmap.copy(bitmap.getConfig(), true);
        imagenFiltros.setImageBitmap(bOut);
    }



}