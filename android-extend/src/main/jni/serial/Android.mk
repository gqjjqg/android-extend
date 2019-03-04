LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := serial
LOCAL_SRC_FILES := src\android\serial_adapter.cpp

LOCAL_STATIC_LIBRARIES := device logger

LOCAL_LDLIBS:= -llog 

include $(BUILD_SHARED_LIBRARY)