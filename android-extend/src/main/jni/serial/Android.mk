LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := serial
LOCAL_SRC_FILES := serial.cpp

#LOCAL_SHARED_LIBRARIES := libarcsoft_handsigns libmpbase

LOCAL_STATIC_LIBRARIES := device loger

LOCAL_LDLIBS:= -llog 

include $(BUILD_SHARED_LIBRARY)