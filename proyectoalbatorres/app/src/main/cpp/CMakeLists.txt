cmake_minimum_required(VERSION 3.22.1)

project("proyectoalbatorres")

set(OpenCV_STATIC ON)
set(OpenCV_DIR /home/lubuntu/Descargas/OpenCV-android-sdk/sdk/native/jni/)
find_package(OpenCV REQUIRED)
include_directories(/home/lubuntu/Descargas/OpenCV-android-sdk/sdk/native/jni/include/)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)


# Enlace de bibliotecas
target_link_libraries(${CMAKE_PROJECT_NAME}
        ${OpenCV_LIBS}
        jnigraphics
        android
        log
        )
