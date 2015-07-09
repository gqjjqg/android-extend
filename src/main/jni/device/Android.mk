LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := device
LOCAL_SRC_FILES := device.c
LOCAL_INC_FILES := device.h
#LOCAL_SHARED_LIBRARIES := libarcsoft_handsigns libmpbase
LOCAL_STATIC_LIBRARIES := loger

LOCAL_C_INCLUDES := $(LOCAL_PATH)	   
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_LDLIBS:= -llog 

include $(BUILD_STATIC_LIBRARY)