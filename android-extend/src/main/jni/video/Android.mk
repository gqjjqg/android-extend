LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := video
LOCAL_SRC_FILES := src\android\video_adapter.cpp

LOCAL_STATIC_LIBRARIES := device imagebase logger

LOCAL_LDLIBS:= -llog 

include $(BUILD_SHARED_LIBRARY)