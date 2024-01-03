package com.example.proyectoalbatorres;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FragPixelReemplazo extends Fragment {

    static {
        System.loadLibrary("proyectoalbatorres");
    }

    private TextureView mTextureView;
    private ImageView mImageView;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private SeekBar seekBarRed;
    private SeekBar seekBarGreen;
    private SeekBar seekBarBlue;
    private SeekBar seekUmbral;
    private android.widget.Button botonColor;
    private android.widget.Button botonGris;
    private android.widget.Button botonVideo;

    int canalColor=3;
    int redSelect = -1;
    int greenSelect = -1;
    int blueSelect = -1;
    int redNew = 0;
    int greenNew = 0;
    int blueNew = 0;
    int umbral = 0;
    int colorSiNo = 0;


    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String videoFilePath;
    // Llamar a este método al iniciar la aplicación para solicitar permisos


    // Manejar el resultado de la solicitud de permisos
    private void startRecording() {
        try {
            videoFilePath = getVideoFilePath(); // Método para obtener la ruta del archivo de video
            //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoSize(720, 480); // Ajusta el tamaño según tus necesidades
            mediaRecorder.setOutputFile(videoFilePath);
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(720, 480); // Adjust the size as needed
            Surface previewSurface = new Surface(surfaceTexture);
            mediaRecorder.setPreviewDisplay(previewSurface);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;

            // Detener la grabación después de 30 segundos (30000 milisegundos)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecording();
                }
            }, 30000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            isRecording = false;
            Toast.makeText(getActivity(), "Video grabado y guardado en: " + videoFilePath, Toast.LENGTH_LONG).show();
        }
    }

    private String getVideoFilePath() {
        File file = new File(getActivity().getExternalFilesDir(null), "video.mp4");
        return file.getAbsolutePath();
    }
    public FragPixelReemplazo() {
        // Constructor vacío requerido.
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pixel_reemplazo, container, false);
        mTextureView = view.findViewById(R.id.textureView);
        //mTextureView.setVisibility(View.GONE);
        mImageView = view.findViewById(R.id.imageView);
        seekBarRed = view.findViewById(R.id.seekBarRed);
        seekBarGreen = view.findViewById(R.id.seekBarGreen);
        seekBarBlue = view.findViewById(R.id.seekBarBlue);
        seekUmbral= view.findViewById(R.id.seekUmbral);
        botonColor = view.findViewById(R.id.boton1);
        botonGris = view.findViewById(R.id.boton2);
        botonVideo = view.findViewById(R.id.boton3);
        botonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorSiNo=0;
            }
        });
        botonGris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorSiNo=1;
            }
        });
        botonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (!isRecording) {
                //    startRecording();
                //} else {
                //    stopRecording();
                //}
            }
        });
        mediaRecorder = new MediaRecorder();
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                Bitmap bitmap = mTextureView.getBitmap();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixelSelect = bitmap.getPixel(x, y);
                    redSelect = Color.red(pixelSelect);
                    greenSelect = Color.green(pixelSelect);
                    blueSelect = Color.blue(pixelSelect);

                    // Aquí puedes utilizar el color del píxel seleccionado
                    System.out.println("PixelColor>  "+ "Red: " + redSelect + ", Green: " + greenSelect + ", Blue: " + blueSelect);
                }

                return true;
            }
        });
        // Set OnSeekBarChangeListener for each SeekBar
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update red channel value based on SeekBar progress
                redNew = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch start event
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch stop event
            }
        });

        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update green channel value based on SeekBar progress
                // Update the image view with the new color values
                greenNew = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch start event
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch stop event
            }
        });

        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update blue channel value based on SeekBar progress
                blueNew= progress;// Update the image view with the new color values
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch start event
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch stop event
            }
        });
        // Set OnSeekBarChangeListener for each SeekBar
        seekUmbral.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update red channel value based on SeekBar progress
                umbral = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch start event
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle SeekBar touch stop event
            }
        });
        return view;
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
        return new Size(720, 480); // Default size, you may need to change this.
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

        // Aquí puedes realizar operaciones en los píxeles, como cambiar colores, filtrar, etc.
        reemplazo(bitmap, bitmap, redSelect, greenSelect, blueSelect, redNew, greenNew, blueNew,umbral,colorSiNo);
        // Después de procesar, puedes actualizar la vista o realizar otras acciones según tus necesidades.

        updateImageView(bitmap);
    }
    private void updateImageView(Bitmap bitmap) {
        getActivity().runOnUiThread(() -> {
            // Actualiza el ImageView con el nuevo frame

            mImageView.setImageBitmap(bitmap);

        });
    }

    public native void reemplazo(Bitmap bitmapIn,Bitmap bitmapOut,int redSel, int greenSel, int blueSel, int redNue, int greenNue, int blueNue, int umbral, int colorSiNo);
}
