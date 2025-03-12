#include <jni.h>
#include "com_quside_QusideQRNG.h"
#include "quside/QRNG.h"
#include "quside/QRNG_admin.h" 
#include <stdlib.h>
#include <string.h>

// Global variables to store the context and device references
static QRNG_context *qContext = NULL;
static QRNG_device *qDevice = NULL;

// Helper function to ensure the context is initialized
static int ensure_context_initialized() {
    if (qContext == NULL) {
        int result = QRNG_init(&qContext);
        return result;
    }
    return 0;  // Context already initialized
}

// Helper function to get a device by index
static QRNG_device* get_device_by_index(int index) {
    if (ensure_context_initialized() < 0) {
        return NULL;
    }
    
    return QRNG_find_device_by_index(qContext, index);
}

// Maintains original Java findBoards signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_findBoards(JNIEnv *env, jobject obj) {
    // Initialize QRNG context if needed
    int result = ensure_context_initialized();
    if (result < 0) {
        return result;  // Return error code
    }
    
    // Return number of devices found
    return (jint)QRNG_get_num_devices(qContext);
}

// Maintains original Java getRandom signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getRandom(JNIEnv *env, jobject obj, jintArray mem_slot, jint Nuint32, jint devInd) {
    jint *c_mem_slot = (*env)->GetIntArrayElements(env, mem_slot, 0);
    ssize_t ret = -1;
    
    // Get device by index
    QRNG_device *dev = get_device_by_index(devInd);
    if (dev == NULL) {
        (*env)->ReleaseIntArrayElements(env, mem_slot, c_mem_slot, 0);
        return -1;  // Device not found
    }
    
    if (Nuint32 < 128) {
        // Build an array of 128 uint32_t to maintain behavior of old implementation
        uint32_t *c_mem_slot_128 = (uint32_t *)malloc(128 * sizeof(uint32_t));
        if (c_mem_slot_128 == NULL) {
            (*env)->ReleaseIntArrayElements(env, mem_slot, c_mem_slot, 0);
            return -1;  // Memory allocation failed
        }
        
        // Get random numbers
        ret = QRNG_getrandom(dev, c_mem_slot_128, 128 * sizeof(uint32_t), QRNG_ENTROPY_PROCESSED);
        
        if (ret > 0) {
            // Copy the first Nuint32 values
            for (int i = 0; i < Nuint32; i++) {
                c_mem_slot[i] = c_mem_slot_128[i];
            }
            ret = 0;  // Success
        }
        
        free(c_mem_slot_128);
    } else {
        // Get random directly into the provided buffer
        ret = QRNG_getrandom(dev, c_mem_slot, Nuint32 * sizeof(uint32_t), QRNG_ENTROPY_PROCESSED);
        if (ret > 0) {
            ret = 0;  // Success
        }
    }
    
    (*env)->ReleaseIntArrayElements(env, mem_slot, c_mem_slot, 0);
    
    // Return success (0) or error code
    return (ret >= 0) ? 0 : (jint)ret;
}

// Maintains original Java qualityQFactor signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_qualityQFactor(JNIEnv *env, jobject obj, jint devInd, jfloatArray qFactor) {
    jfloat *c_qFactor = (*env)->GetFloatArrayElements(env, qFactor, 0);
    int ret = -1;
    
    // Get device by index
    QRNG_device *dev = get_device_by_index(devInd);
    if (dev == NULL) {
        (*env)->ReleaseFloatArrayElements(env, qFactor, c_qFactor, 0);
        return -1;  // Device not found
    }
    
    // Use the admin function to get Q factor
    ret = QRNG_get_qfactor(dev, c_qFactor);
    
    (*env)->ReleaseFloatArrayElements(env, qFactor, c_qFactor, 0);
    return ret;
}

// Maintains original Java getHmin signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getHmin(JNIEnv *env, jobject obj, jint devInd, jfloatArray hmin) {
    jfloat *c_hmin = (*env)->GetFloatArrayElements(env, hmin, 0);
    int ret = -1;
    
    // Get device by index
    QRNG_device *dev = get_device_by_index(devInd);
    if (dev == NULL) {
        (*env)->ReleaseFloatArrayElements(env, hmin, c_hmin, 0);
        return -1;  // Device not found
    }
    
    // Use the admin function to get Hmin
    ret = QRNG_get_hmin(dev, c_hmin);
    
    (*env)->ReleaseFloatArrayElements(env, hmin, c_hmin, 0);
    return ret;
}

// Maintains original Java getCalibrationStatus signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_getCalibrationStatus(JNIEnv *env, jobject obj, jint devInd, jintArray status) {
    jint *c_status = (*env)->GetIntArrayElements(env, status, 0);
    int ret = -1;
    
    // Get device by index
    QRNG_device *dev = get_device_by_index(devInd);
    if (dev == NULL) {
        (*env)->ReleaseIntArrayElements(env, status, c_status, 0);
        return -1;  // Device not found
    }
    
    // The system_state_t enum from QRNG_admin.h is used
    system_state_t system_status;
    ret = QRNG_get_calibration_status(dev, &system_status);
    
    if (ret == 0) {
        // Convert system_state_t to jint
        *c_status = (jint)system_status;
    }
    
    (*env)->ReleaseIntArrayElements(env, status, c_status, 0);
    return ret;
}

// Maintains original Java setCalibration signature but uses new QRNG API
JNIEXPORT jint JNICALL Java_com_quside_QusideQRNG_setCalibration(JNIEnv *env, jobject obj, jint devInd) {
    // Get device by index
    QRNG_device *dev = get_device_by_index(devInd);
    if (dev == NULL) {
        return -1;  // Device not found
    }
    
    // Use the admin function to set calibration
    return QRNG_set_calibration(dev);
}

// Called by JVM when library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    // Standard JNI OnLoad implementation
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    
    // Initialize the QRNG context when the library is loaded
    int result = ensure_context_initialized();
    if (result < 0) {
        // Context initialization failed, but we still return JNI_VERSION_1_6
        // and will handle the error in individual function calls
    }
    
    return JNI_VERSION_1_6;
}

// Called by JVM when library is unloaded
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    // Free the device if it was allocated
    if (qDevice != NULL) {
        QRNG_device_destroy(qDevice);
        qDevice = NULL;
    }
    
    // Free the context if it was allocated
    if (qContext != NULL) {
        QRNG_free(qContext);
        qContext = NULL;
    }
}
