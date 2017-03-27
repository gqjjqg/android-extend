LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := video
LOCAL_SRC_FILES := video.cpp

#LOCAL_SHARED_LIBRARIES := libarcsoft_handsigns libmpbase
LOCAL_STATIC_LIBRARIES := device imagebase loger

LOCAL_LDLIBS:= -llog 

include $(BUILD_SHARED_LIBRARY)