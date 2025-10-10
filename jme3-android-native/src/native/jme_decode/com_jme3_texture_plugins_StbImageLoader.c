#include "../headers/com_jme3_texture_plugins_StbImageLoader.h"
#include <assert.h>

#ifndef NDEBUG
#include <android/log.h>
#define LOGI(fmt, ...) __android_log_print(ANDROID_LOG_INFO, \
                       "StbImageLoader", fmt, ##__VA_ARGS__);
#else
#define LOGI(fmt, ...)
#endif

#define STB_IMAGE_IMPLEMENTATION
#define STBI_NO_STDIO
#define STBI_NO_THREAD_LOCALS

#include "STBI/stb_image.h"
#include <stdint.h>
#include <string.h>


/**
 * @author Riccardo Balbo
 */

 
static void throwRuntime(JNIEnv* env, const char* msg){
    jclass ex = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (ex) (*env)->ThrowNew(env, ex, msg ? msg : "native error");
}

JNIEXPORT void JNICALL
Java_com_jme3_texture_plugins_StbImageLoader_info
  (JNIEnv* env, jclass clazz,
   jobject inBuf, jobject wBuf, jobject hBuf, jobject compBuf, jobject formatBuf)
{
    if (!inBuf || !wBuf || !hBuf || !compBuf || !formatBuf) {
        throwRuntime(env, "info(): null argument");
        return;
    }

    const stbi_uc* data = (const stbi_uc*)(*env)->GetDirectBufferAddress(env, inBuf);
    jlong cap = (*env)->GetDirectBufferCapacity(env, inBuf);
    if (!data || cap <= 0){
        throwRuntime(env, "info(): invalid input buffer");
        return;
    }

    int w=0,h=0,comp=0;
    if (!stbi_info_from_memory(data, (int)cap, &w, &h, &comp)) {
        throwRuntime(env, stbi_failure_reason());
        return;
    }

    int fmtFlag = (stbi_is_hdr_from_memory(data, (int)cap) ||
                   stbi_is_16_bit_from_memory(data, (int)cap)) ? 16 : 8;

    int* wPtr    = (int*)(*env)->GetDirectBufferAddress(env, wBuf);
    int* hPtr    = (int*)(*env)->GetDirectBufferAddress(env, hBuf);
    int* compPtr = (int*)(*env)->GetDirectBufferAddress(env, compBuf);
    int* fmtPtr  = (int*)(*env)->GetDirectBufferAddress(env, formatBuf);
    if (!wPtr || !hPtr || !compPtr || !fmtPtr){
        throwRuntime(env, "info(): invalid out buffers");
        return;
    }

    wPtr[0] = w;
    hPtr[0] = h;
    compPtr[0] = comp;
    fmtPtr[0]  = fmtFlag;
}



static void setEndianess(JNIEnv* env, jobject bb) {
    if (!bb) return;
    jclass bbCls = (*env)->GetObjectClass(env, bb);
    jclass boCls = (*env)->FindClass(env, "java/nio/ByteOrder");
    if (!bbCls || !boCls) return;

    jfieldID leField = (*env)->GetStaticFieldID(env, boCls, "LITTLE_ENDIAN", "Ljava/nio/ByteOrder;");
    if (!leField) return;
    jobject le = (*env)->GetStaticObjectField(env, boCls, leField);
    if (!le) return;

    jmethodID order = (*env)->GetMethodID(env, bbCls, "order", "(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;");
    if (!order) return;

    (void)(*env)->CallObjectMethod(env, bb, order, le);
}

JNIEXPORT jobject JNICALL
Java_com_jme3_texture_plugins_StbImageLoader_load
  (JNIEnv* env, jclass clazz,
   jobject inBuf, jboolean flipY, jobject wBuf, jobject hBuf, jobject compBuf)
{
    if (!inBuf || !wBuf || !hBuf || !compBuf) return NULL;

    const stbi_uc* data = (const stbi_uc*)(*env)->GetDirectBufferAddress(env, inBuf);
    jlong cap = (*env)->GetDirectBufferCapacity(env, inBuf);
    if (!data || cap <= 0){
        throwRuntime(env, "load(): invalid input buffer");
        return NULL;
    }

    stbi_set_flip_vertically_on_load(flipY ? 1 : 0);

    int w=0,h=0,comp=0;
    stbi_uc* pixels = stbi_load_from_memory(data, (int)cap, &w, &h, &comp, 0);
    if (!pixels){
        throwRuntime(env, stbi_failure_reason());
        return NULL;
    }

    int* wPtr    = (int*)(*env)->GetDirectBufferAddress(env, wBuf);
    int* hPtr    = (int*)(*env)->GetDirectBufferAddress(env, hBuf);
    int* compPtr = (int*)(*env)->GetDirectBufferAddress(env, compBuf);
    if (!wPtr || !hPtr || !compPtr){
        stbi_image_free(pixels);
        throwRuntime(env, "load(): invalid out buffers");
        return NULL;
    }
    wPtr[0] = w; hPtr[0] = h; compPtr[0] = comp;

    jlong byteSize = (jlong)w * (jlong)h * (jlong)comp; // bytes
    jobject bbx = (*env)->NewDirectByteBuffer(env, pixels, byteSize);
    setEndianess(env, bbx);
    return bbx;
}

JNIEXPORT jobject JNICALL
Java_com_jme3_texture_plugins_StbImageLoader_loadf
  (JNIEnv* env, jclass clazz,
   jobject inBuf, jboolean flipY, jobject wBuf, jobject hBuf, jobject compBuf)
{
    if (!inBuf || !wBuf || !hBuf || !compBuf) return NULL;

    const stbi_uc* data = (const stbi_uc*)(*env)->GetDirectBufferAddress(env, inBuf);
    jlong cap = (*env)->GetDirectBufferCapacity(env, inBuf);
    if (!data || cap <= 0){
        throwRuntime(env, "loadf(): invalid input buffer");
        return NULL;
    }

    stbi_set_flip_vertically_on_load(flipY ? 1 : 0);

    int w=0,h=0,comp=0;
    float* pixels = stbi_loadf_from_memory(data, (int)cap, &w, &h, &comp, 0);
    if (!pixels){
        throwRuntime(env, stbi_failure_reason());
        return NULL;
    }

    int* wPtr    = (int*)(*env)->GetDirectBufferAddress(env, wBuf);
    int* hPtr    = (int*)(*env)->GetDirectBufferAddress(env, hBuf);
    int* compPtr = (int*)(*env)->GetDirectBufferAddress(env, compBuf);
    if (!wPtr || !hPtr || !compPtr){
        stbi_image_free(pixels);
        throwRuntime(env, "loadf(): invalid out buffers");
        return NULL;
    }
    wPtr[0] = w; hPtr[0] = h; compPtr[0] = comp;

    jlong byteSize = (jlong)w * (jlong)h * (jlong)comp * (jlong)sizeof(float);
    jobject bb = (*env)->NewDirectByteBuffer(env, pixels, byteSize);
    if (!bb){
        stbi_image_free(pixels);
        throwRuntime(env, "loadf(): failed to create direct buffer");
        return NULL;
    }

    setEndianess(env, bb);
    return bb;
}

JNIEXPORT void JNICALL
Java_com_jme3_texture_plugins_StbImageLoader_free
  (JNIEnv* env, jclass clazz, jobject buf)
{
    if (!buf) return;
    void* ptr = (*env)->GetDirectBufferAddress(env, buf);
    if (ptr) stbi_image_free(ptr);
}