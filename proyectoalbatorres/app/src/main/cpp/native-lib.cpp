#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/dnn.hpp>
#include <opencv2/video.hpp>
#include "android/bitmap.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <iostream> // Librería estándar para leer y escribir datos en la consola, obtener los errores y loggs
#include <cstdlib> // Librería estándar para generación de números aleatorios, manejo de memoria, etc.
#include <android/log.h>

#include <cstring> // Tiene métodos para el manejo de cadenas de texto
#include <cmath> // Define las funciones matemáticas

#include <random> // Librería para generación de números aleatorios
#include <vector> // Librería para definir arreglos dinámicos
#include <sstream> // Librería para conversión de datos y manejo de flujos
#include <fstream> // Librería para manejar flujos de datos (archivos)

#include <filesystem> // Librería que contiene las funciones para listar archivos y carpetas

// Librerías de OpenCV
#include <opencv2/core/core.hpp> // Contiene las definiciones básicas de las matrices que representan imágenes y otras estructuras
#include <opencv2/highgui/highgui.hpp> // Contiene las definiciones y funciones para crear GUIs
#include <opencv2/imgproc/imgproc.hpp> // Permite realizar procesamiento de imágenes
#include <opencv2/imgcodecs/imgcodecs.hpp> // Permite gestionar los códecs para lectura de formatos gráficos
#include <opencv2/video/video.hpp> // Permite reproducir archivos de vídeo
#include <opencv2/videoio/videoio.hpp> // Permite almacenar vídeos

// DNN Module
#include <opencv2/dnn/dnn.hpp>
//ONNX Module


cv::dnn::Net neuralNetwork;


std::vector<std::string> etiquetas;
const float IMG_WIDTH = 640.0;
const float IMG_HEIGHT = 640.0;
const float CLASS_PROBABILITY = 0.5;
const float NMS_THRESHOLD = 0.5;
const float CONFIDENCE_THRESHOLD = 0.5;
const int NUMBER_OF_OUTPUTS = 85;
cv::Scalar BLACK = cv::Scalar(0,0,0);
cv::Scalar BLUE = cv::Scalar(255, 178, 50);
cv::Scalar YELLOW = cv::Scalar(0, 255, 255);
cv::Scalar RED = cv::Scalar(0,0,255);

// Text parameters.
const float FONT_SCALE = 0.7;
const int FONT_FACE = cv::FONT_HERSHEY_SIMPLEX;
const int THICKNESS = 1;

void bitmapToMat(JNIEnv * env, jobject bitmap, cv::Mat &dst, jboolean needUnPremultiplyAlpha){
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
// info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
//jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
//if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv * env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width ==
                                                                         (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
// info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
//jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
//if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

std::vector<std::string> loadLabelsCOCO(AAssetManager *assetManager, const char *filename, char sep = '\n') {
    std::vector<std::string> names;

    // Abre el archivo desde la carpeta "assets"
    AAsset *asset = AAssetManager_open(assetManager, filename, AASSET_MODE_BUFFER);
    if (asset == nullptr) {
        // Maneja el error si el archivo no se puede abrir
        std::cerr << "Error al abrir el archivo " << filename << " desde assets." << std::endl;
        return names;
    }

    // Obtiene un puntero al búfer de datos del archivo
    const void *buffer = AAsset_getBuffer(asset);

    // Crea un flujo de entrada a partir del búfer de datos
    std::istringstream bufferStream(static_cast<const char*>(buffer));

    std::string token;
    while (getline(bufferStream, token, sep)) {
        if (token.size() > 1) {
            names.push_back(token);
        }
    }
    // Cierra el archivo
    AAsset_close(asset);
    etiquetas=names;
    return names;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_proyectoalbatorres_FragHistograma_loadLabelsCOCO(JNIEnv *env, jobject /* this */,
                                                                  jobject assetManager) {
    const char *filename = "coco.names";

    // Obtiene el puntero al AssetManager desde el objeto Java
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    // Llama a la función loadLabelsCOCO para cargar las etiquetas desde assets
    std::vector<std::string> labels = loadLabelsCOCO(mgr, filename);

    // Convierte el vector de cadenas C++ a un array de cadenas Java
    jobjectArray result = env->NewObjectArray(labels.size(), env->FindClass("java/lang/String"), nullptr);
    for (size_t i = 0; i < labels.size(); ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(labels[i].c_str()));
    }

    return result;
}



void draw_label(cv::Mat& input_image, std::string label, int left, int top){
    // Display the label at the top of the bounding box.
    int baseLine;
    cv::Size label_size = cv::getTextSize(label, FONT_FACE, FONT_SCALE, THICKNESS, &baseLine);
    top = std::max(top, label_size.height);
    // Top left corner.
    cv::Point tlc = cv::Point(left, top);
    // Bottom right corner.
    cv::Point brc = cv::Point(left + label_size.width, top + label_size.height + baseLine);
    // Draw white rectangle.
    rectangle(input_image, tlc, brc, BLACK, cv::FILLED);
    // Put the label on the black rectangle.
    putText(input_image, label, cv::Point(left, top + label_size.height), FONT_FACE, FONT_SCALE, YELLOW, THICKNESS);
}

std::vector<cv::Mat> forwardNET(cv::Mat inputImage, cv::dnn::Net net){
    // Create a blob from the input image
    cv::Mat blob;
    cv::dnn::blobFromImage(inputImage, blob, 1./255., cv::Size(IMG_WIDTH, IMG_HEIGHT), cv::Scalar(), true, false);

    net.setInput(blob);

    // Forward pass.
    std::vector<cv::Mat> outputs;

    net.forward(outputs, net.getUnconnectedOutLayersNames());

    return outputs;
}

cv::Mat filterDetections(cv::Mat inputImg, std::vector<cv::Mat> detections, const std::vector<std::string> classNames){
    // Initialize vectors to hold respective outputs while unwrapping detections.
    cv::Mat inputImage=inputImg.clone();
    std::vector<int> classIDs;
    std::vector<float> confidences;
    std::vector<cv::Rect> boxes;

    // Resizing factor.
    float x_factor = inputImage.cols / IMG_WIDTH;
    float y_factor = inputImage.rows / IMG_HEIGHT;
    float *pData = new float[NUMBER_OF_OUTPUTS]; // = (float *)detections[0].data;
    float confidence = 0.0;
    float *probValues;
    cv::Point classId;
    double maxClassProb = 0.0;

    //Mat scores(1, classNames.size(), CV_32FC1, classes_scores);
    cv::Mat probabilityClasses = cv::Mat::zeros(1, classNames.size(), CV_32FC1);

    int totalDetections = detections[0].total() / NUMBER_OF_OUTPUTS;

    for (int i = 0; i < totalDetections; ++i){
        std::memcpy(pData, (float *) detections[0].data+(i*NUMBER_OF_OUTPUTS), NUMBER_OF_OUTPUTS*sizeof(float));
        confidence = pData[4];
        // Discard bad detections and continue.
        if (confidence >= CONFIDENCE_THRESHOLD){
            probValues = (pData + 5);
            // Create a 1x85 Mat and store class scores of 80 classes.
            probabilityClasses = cv::Mat::zeros(1, classNames.size(), CV_32FC1);
            std::memcpy(probabilityClasses.data, probValues, classNames.size()*sizeof(float));

            // Perform minMaxLoc and acquire the index of best class  score.
            minMaxLoc(probabilityClasses, 0, &maxClassProb, 0, &classId);
            // Continue if the class score is above the threshold.
            if (maxClassProb > CLASS_PROBABILITY){
                // Store class ID and confidence in the pre-defined respective vectors.
                confidences.push_back(confidence);
                classIDs.push_back(classId.x);
                boxes.push_back(cv::Rect(int((pData[0]-0.5*pData[2])*x_factor),int((pData[1]-0.5*pData[3])*y_factor),
                                     int(pData[2]*x_factor), int(pData[3]*y_factor)));

            }
        }
    }
    // Perform Non-Maximum Suppression and draw predictions.
    std::vector<int> indices;
    std::string label = "";
    cv::dnn::NMSBoxes(boxes, confidences, CLASS_PROBABILITY, NMS_THRESHOLD, indices);
    for (int i = 0; i < indices.size(); i++){
        // Draw the bounding box arround detected object
        rectangle(inputImage, boxes[indices[i]], BLUE, 3*THICKNESS);
        // Get the label for the class name and its confidence.
        label = cv::format("%.2f", confidences[indices[i]]);
        label = classNames[classIDs[indices[i]]] + ":" + label;
        // Draw class labels.
        draw_label(inputImage, label, boxes[indices[i]].x, boxes[indices[i]].y);
    }
    return inputImage;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragHistograma_Modelo(JNIEnv *env, jobject /* this */,
                                                          jobject assetManager,
                                                          jstring modelPath) {
    AAssetManager *mgr = AAssetManager_fromJava(env,assetManager);
    const char *modelPathStr = env->GetStringUTFChars(modelPath, nullptr);
    AAsset *modelAsset = AAssetManager_open(mgr, modelPathStr, AASSET_MODE_BUFFER);
    env->ReleaseStringUTFChars(modelPath, modelPathStr);
    if (modelAsset != nullptr) {
        off_t modelSize = AAsset_getLength(modelAsset);
        const void *modelData = AAsset_getBuffer(modelAsset);
        const uchar* modelDataUChar = static_cast<const uchar*>(modelData);
        std::vector<uchar> modelBuffer(modelDataUChar, modelDataUChar + modelSize);
        AAsset_close(modelAsset);
        cv::Mat modelMat(modelBuffer);
        neuralNetwork = cv::dnn::readNetFromONNX(modelMat);
        if (!neuralNetwork.empty()) {
            // El modelo se cargó correctamente, puedes imprimir un mensaje
            __android_log_print(ANDROID_LOG_INFO, "ProyectoAlbaTorres", "Cargo el modelo");
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "ProyectoAlbaTorres", "NO Cargo el modelo if 2");
        }
    }  else {
        __android_log_print(ANDROID_LOG_ERROR, "ProyectoAlbaTorres", "NO Cargo el modelo if 1");
        }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragHistograma_Deteccion(JNIEnv *env, jobject /* this */,
                                                             jobject bitmapIn,
                                                             jobject bitmapOut

                                                            ) {

    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::cvtColor(src, src, cv::COLOR_RGBA2BGR);

    std::vector<std::string> nombresCapas = neuralNetwork.getLayerNames();

    nombresCapas = neuralNetwork.getUnconnectedOutLayersNames();

    std::vector<cv::Mat> detections;
    detections = forwardNET(src, neuralNetwork);

    cv::Mat img = filterDetections(src, detections, etiquetas);

    matToBitmap(env, img, bitmapOut, false);


}


extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragHistograma_histograma(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut,
        jint canal){

    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);


    std::vector<cv::Mat> bgrChannels;
    split(src, bgrChannels);

   // std::vector<std::string> classNames = loadLabelsCOCO();


    cv::Mat histograma;
    int histSize = src.cols;
    float range[] = {0, 256};
    const float* histRange = {range};

    if (canal == 3) {
        cv::Mat histInput;
        cv::hconcat(bgrChannels, histInput);

        // Calcula el histograma general
        calcHist(&histInput, 1, 0, cv::Mat(), histograma, 1, &histSize, &histRange, true, false);
    } else {
        // Calcula el histograma del canal seleccionado
        calcHist(&bgrChannels[canal], 1, 0, cv::Mat(), histograma, 1, &histSize, &histRange, true, false);
    }
    int ancho =src.cols;
    int alto = src.rows;
    int bin_w = cvRound((double) ancho / histSize);

    cv::Mat histImage(alto, ancho, CV_8UC3, cv::Scalar(0, 0, 0));

    normalize(histograma, histograma, 0, histImage.rows, cv::NORM_MINMAX, -1, cv::Mat());
    //AZUL
    if (canal==0){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(0, 0, 255), -1, 8, 0);
        }
    }
    //VERDE
    if (canal==1){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(0, 255, 0), -1, 8, 0);
        }
    }
    //ROJO
    if (canal==2){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(255, 0, 0), -1, 8, 0);
        }
    }
    if (canal==3){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(255, 255, 255), -1, 8, 0);
        }
    }
    matToBitmap(env, histImage, bitmapOut, false);
}
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragPixelReemplazo_histograma(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut,
        jint canal){

    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

//cv::flip(src, src, 0);
    std::vector<cv::Mat> bgrChannels;
    split(src, bgrChannels);

    cv::Mat histograma;
    int histSize = src.cols;
    float range[] = {0, 256};
    const float* histRange = {range};

    if (canal == 3) {
        cv::Mat histInput;
        cv::hconcat(bgrChannels, histInput);

        // Calcula el histograma general
        calcHist(&histInput, 1, 0, cv::Mat(), histograma, 1, &histSize, &histRange, true, false);
    } else {
        // Calcula el histograma del canal seleccionado
        calcHist(&bgrChannels[canal], 1, 0, cv::Mat(), histograma, 1, &histSize, &histRange, true, false);
    }
    int ancho =src.cols;
    int alto = src.rows;
    int bin_w = cvRound((double) ancho / histSize);

    cv::Mat histImage(alto, ancho, CV_8UC3, cv::Scalar(0, 0, 0));

    normalize(histograma, histograma, 0, histImage.rows, cv::NORM_MINMAX, -1, cv::Mat());
    //AZUL
    if (canal==0){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(0, 0, 255), -1, 8, 0);
        }
    }
    //VERDE
    if (canal==1){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(0, 255, 0), -1, 8, 0);
        }
    }
    //ROJO
    if (canal==2){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(255, 0, 0), -1, 8, 0);
        }
    }
    if (canal==3){
        for (int i = 1; i < histSize; i++) {
            rectangle(histImage, cv::Point(bin_w * (i - 1), alto),
                      cv::Point(bin_w * (i), alto - cvRound(histograma.at<float>(i))),
                      cv::Scalar(255, 255, 255), -1, 8, 0);
        }
    }
    matToBitmap(env, histImage, bitmapOut, false);
}
extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragPixelReemplazo_procesarFrame(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray frameData,
        jint width,
        jint height,
        jobject bitmapOut) {

    // Convierte jbyteArray a cv::Mat
    jbyte* frameDataPtr = env->GetByteArrayElements(frameData, nullptr);
    cv::Mat frameMat(height, width, CV_8UC4, (uchar*)frameDataPtr);

    // Realiza el procesamiento en la matriz
    // (Aquí puedes hacer cualquier manipulación que desees en el frame)

    // Convierte la matriz de salida a bitmap y envíala de vuelta a Java
    // (Debes implementar la función matToBitmap según tu código original)
    matToBitmap(env, frameMat, bitmapOut, false);

    // Libera la memoria
    env->ReleaseByteArrayElements(frameData, frameDataPtr, 0);
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragFiltros_AplicaFiltros(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut,
        jint filtro,
        jint vMed,
        jint vSX,
        jint vSY,
        jint vBX,
        jint vBY,
        jint vGX,
        jint vGY){

    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    int valorMediana = static_cast<int>(vMed);
    int sigmaX = static_cast<int>(vSX);
    int sigmaY = static_cast<int>(vSY);
    int valorGaussX = static_cast<int>(vGX);
    int valorGaussY = static_cast<int>(vGY);
    int valorBlurX = static_cast<int>(vBX);
    int valorBlurY = static_cast<int>(vBY);


    cv::Mat salida;

    if (filtro == 0){
        medianBlur(src, salida, valorMediana);
    }
    if (filtro == 1){
        blur(src, salida, cv::Size(valorBlurX,valorBlurY));
    }
    if (filtro == 2){
        GaussianBlur(src, salida, cv::Size(valorGaussX,valorGaussY),sigmaX,sigmaY);
    }
    if (filtro == 3){
        cv::Mat imagenGris = src.clone();
        cvtColor(imagenGris, imagenGris, cv::COLOR_BGR2GRAY);
        Sobel(imagenGris, salida, CV_16S, 1, 0, 3);
        convertScaleAbs(salida, salida);
    }
    if (filtro == 4){
        cv::Mat imagenGris = src.clone();
        cvtColor(imagenGris, imagenGris, cv::COLOR_BGR2GRAY);
        Sobel(imagenGris, salida, CV_16S, 0, 1, 3);
        convertScaleAbs(salida, salida);
    }
    if (filtro == 5){
        cv::Mat imagenGris = src.clone();
        cv::Mat salidaX, salidaY;
        cvtColor(imagenGris, imagenGris, cv::COLOR_BGR2GRAY);
        Sobel(imagenGris, salidaX, CV_16S, 1, 0, 3);
        Sobel(imagenGris, salidaY, CV_16S, 0, 1, 3);
        convertScaleAbs(salidaX, salidaX);
        convertScaleAbs(salidaY, salidaY);
        addWeighted(salidaX, 0.5, salidaY, 0.5, 0, salida);
    }

    matToBitmap(env, salida, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragEfecto_EfectoCartoon(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut,
        jint valor
       ){

    double valorN = static_cast<double>(valor) / 10;
    cv::Mat src;
    cv::Mat salida;
    bitmapToMat(env, bitmapIn, src, false);
    cv::Mat caricatura;
    cv::Mat imagenGris;
    cv::Mat blurred;
    cv::Mat edges;
    cv::Mat thresholded;
    cv::Mat dilated;
    cv::Mat dilatedColor;
    cv::Mat salidaX, salidaY;

    cv::cvtColor(src, imagenGris, cv::COLOR_BGR2GRAY);
    Sobel(imagenGris, salidaX, CV_16S, 1, 0, 3);
    Sobel(imagenGris, salidaY, CV_16S, 0, 1, 3);
    convertScaleAbs(salidaX, salidaX);
    convertScaleAbs(salidaY, salidaY);
    addWeighted(salidaX, 0.5, salidaY, 0.5, 0, edges);

    bitwise_not(edges, edges);

    threshold(edges, thresholded, 180, 200, cv::THRESH_BINARY_INV);

    cv::Mat kern = getStructuringElement(cv::MORPH_RECT, cv::Size(3, 3));  // Puedes ajustar el tamaño del kernel aquí
    dilate(thresholded, dilated, kern, cv::Point(-1, -1), 1);

    bitwise_not(dilated, dilated);

    cvtColor(dilated, dilatedColor, cv::COLOR_GRAY2RGBA);
    addWeighted(src, 1, dilatedColor, valorN, 0, caricatura);

    matToBitmap(env, caricatura, bitmapOut, false);

}
#include <android/log.h>
extern "C" JNIEXPORT void JNICALL
Java_com_example_proyectoalbatorres_FragPixelReemplazo_reemplazo(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut,
        jint redSeleccionado,
        jint greenSeleccionado,
        jint blueSeleccionado,
        jint redNuevo,
        jint greenNuevo,
        jint blueNuevo,
        jint umbral,
        jint colorSiNo){
    cv::Mat src;
    int redUmBajo=0;
    int grenUmBajo=0;
    int blueUmBajo=0;
    int redUmAlto=0;
    int grenUmAlto=0;
    int blueUmAlto=0;
    bitmapToMat(env, bitmapIn, src, false);

    if(redSeleccionado!=-1){
        redUmBajo=redSeleccionado-umbral;
        grenUmBajo=greenSeleccionado-umbral;
        blueUmBajo=blueSeleccionado-umbral;
        redUmAlto=redSeleccionado+umbral;
        grenUmAlto=greenSeleccionado+umbral;
        blueUmAlto=blueSeleccionado+umbral;
        cv::Mat mask;
        cv::Mat src1;
        //cv::inRange(src, cv::Scalar (0,0,0), cv::Scalar(0,0,0), mask);
        //src.setTo(cv::Vec3b(0,255,0),mask);
        int type = src.type();
        if(type != CV_8UC3){
            cv::cvtColor(src,src1, cv::COLOR_BGR2GRAY);
        }
        cv::inRange(src1, cv::Scalar (redUmBajo,grenUmBajo,blueUmBajo), cv::Scalar(redUmAlto,grenUmAlto,blueUmAlto), mask);
        cv::cvtColor(src,src1, cv::COLOR_BGR2GRAY);
        if(colorSiNo==0){
            src.setTo(cv::Scalar(redNuevo,greenNuevo,blueNuevo),mask);
            matToBitmap(env, src, bitmapOut, false);
        }else if(colorSiNo==1){
            src.setTo(cv::Scalar(redNuevo,greenNuevo,blueNuevo),mask);
            cv::cvtColor(src,src, cv::COLOR_BGR2GRAY);
            matToBitmap(env, src, bitmapOut, false);
        }

        //cv::cvtColor(src,src, cv::COLOR_GRAY2RGB);

    }else{
        matToBitmap(env, src, bitmapOut, false);
    }

}


