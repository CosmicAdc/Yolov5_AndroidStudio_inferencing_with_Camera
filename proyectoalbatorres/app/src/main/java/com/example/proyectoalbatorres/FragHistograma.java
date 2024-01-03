package com.example.proyectoalbatorres;
import static com.example.proyectoalbatorres.R.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import java.util.Arrays;



public class FragHistograma extends Fragment {

    static {
        System.loadLibrary("proyectoalbatorres");
    }
    private android.widget.Button botonAzul;
    private android.widget.Button botonVerde;
    private android.widget.Button botonRojo;
    private android.widget.Button botonGeneral;

    private TextureView mTextureView;
    private ImageView mImageView;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    int canalColor=3;
    public FragHistograma() {
        // Constructor vacío requerido.
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.histograma, container, false);
        AssetManager assetManager = getResources().getAssets();
        //Carga del modelo
        Modelo(assetManager, "yolov5n.onnx");
        //Carga los labels
        loadLabelsCOCO(assetManager);
        System.out.println(loadLabelsCOCO(assetManager)[0]);

        mTextureView = view.findViewById(R.id.textureView);
        //mTextureView.setVisibility(View.GONE);
        botonAzul = view.findViewById(R.id.button1);
        botonVerde = view.findViewById(R.id.button2);
        botonRojo = view.findViewById(R.id.button3);
        botonGeneral = view.findViewById(R.id.button4);
        mImageView = view.findViewById(R.id.imageView);
        botonAzul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canalColor=0;
            }
        });


        botonVerde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canalColor=1;
            }
        });

        botonRojo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canalColor=2;
            }
        });

        botonGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canalColor=3;
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
        // Aquí puedes realizar operaciones en los píxeles, como cambiar colores, filtrar, etc.
        //histograma(bitmap,bitmap,canalColor);

        Deteccion(bitmap,bitmap);
        // Después de procesar, puedes actualizar la vista o realizar otras acciones según tus necesidades.
        updateImageView(bitmap);
    }
    private void updateImageView(Bitmap bitmap) {
       getActivity().runOnUiThread(() -> {
            // Actualiza el ImageView con el nuevo frame
            mImageView.setImageBitmap(bitmap);
        });
    }

    public native void histograma(Bitmap in, Bitmap out, int canal);
    public native void Modelo(AssetManager assetManager, String modelPath);

    public native String[] loadLabelsCOCO(AssetManager assetManager);

    public native void Deteccion(Bitmap in, Bitmap out);
}