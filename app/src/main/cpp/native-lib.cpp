#include <jni.h>
#include <string>
#include <vector>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_alone_edgeview_MainActivity_nativeProcessFrame(
        JNIEnv* env, jobject /* this */, jbyteArray nv21Array, jint width, jint height) {

    jbyte* nv21 = env->GetByteArrayElements(nv21Array, nullptr);
    int nv21Len = env->GetArrayLength(nv21Array);

    // Create Mat from NV21 (YUV420sp)
    cv::Mat yuv(height + height/2, width, CV_8UC1, (unsigned char*)nv21);
    cv::Mat bgr;
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    // Convert to gray and Canny
    cv::Mat gray, edges, rgba;
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
    cv::GaussianBlur(gray, gray, cv::Size(5,5), 1.5);
    Canny(gray, edges, 50, 150);

    // Make edges into 4-channel RGBA for display (white on black)
    cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA); // CV_8UC4

    // Prepare jbyteArray to return
    int outLen = rgba.total() * rgba.elemSize();
    jbyteArray outArray = env->NewByteArray(outLen);
    env->SetByteArrayRegion(outArray, 0, outLen, (jbyte*)rgba.data);

    env->ReleaseByteArrayElements(nv21Array, nv21, 0);
    return outArray;
}