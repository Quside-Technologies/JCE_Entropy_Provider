#include <jni.h>
#include "com_quside_QusideQRNG.h"
#include "quside_QRNG_minimal.h"
#include <stdlib.h>

// #define QRNG_DEBUG

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_findBoards(JNIEnv *env, jobject obj) {
    return find_boards();
}

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getRandom(JNIEnv *env, jobject obj, jintArray mem_slot, jint Nuint32, jint devInd) {
    jint *c_mem_slot = (*env)->GetIntArrayElements(env, mem_slot, 0);

    int ret;

    if(Nuint32 < 128){
        // Build an array of 128 uint32_t
        uint32_t *c_mem_slot_128 = (uint32_t *)malloc(128 * sizeof(uint32_t));
        // Get random numbers
        ret = get_random(c_mem_slot_128, 128 * 4, (uint16_t)devInd);
        // Copy the first Nuint32 values
        for(int i = 0; i < Nuint32; i++){
            c_mem_slot[i] = c_mem_slot_128[i];
        }
        free(c_mem_slot_128);
    }
    else {
        ret = get_random(c_mem_slot, (size_t)Nuint32 * 4, (uint16_t)devInd);
    }

    (*env)->ReleaseIntArrayElements(env, mem_slot, c_mem_slot, 0);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_qualityQFactor(JNIEnv *env, jobject obj, jint devInd, jfloatArray qFactor) {
    jfloat *c_qFactor = (*env)->GetFloatArrayElements(env, qFactor, 0);
    int ret = quality_Qfactor((uint16_t)devInd, c_qFactor);
    (*env)->ReleaseFloatArrayElements(env, qFactor, c_qFactor, 0);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getHmin(JNIEnv *env, jobject obj, jint devInd, jfloatArray hmin) {
    jfloat *c_hmin = (*env)->GetFloatArrayElements(env, hmin, 0);
    int ret = get_hmin((uint16_t)devInd, c_hmin);
    (*env)->ReleaseFloatArrayElements(env, hmin, c_hmin, 0);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getCalibrationStatus(JNIEnv *env, jobject obj, jint devInd, jintArray status) {
    jint *c_status = (*env)->GetIntArrayElements(env, status, 0);
    int ret = get_calibration_status((uint16_t)devInd, (int *)c_status);
    (*env)->ReleaseIntArrayElements(env, status, c_status, 0);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_setCalibration(JNIEnv *env, jobject obj, jint devInd) {
    return set_calibration((uint16_t)devInd);
}

