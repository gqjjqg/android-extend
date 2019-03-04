LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := GLESRender
LOCAL_SRC_FILES := ..\commen\render\image_render.cpp \
				   ..\commen\render\comm_render.cpp 
LOCAL_C_INCLUDES := $(LOCAL_PATH)\..\commen\render    
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)
LOCAL_STATIC_LIBRARIES := imagebase logger
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := render
LOCAL_SRC_FILES := src\android\render_adapter.cpp \
				   ..\commen\render\Matrix.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)\..\commen\render
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_LDLIBS   := -llog -ljnigraphics -lGLESv2 -lGLESv1_CM -ldl 

LOCAL_STATIC_LIBRARIES := imagebase logger GLESRender

LOCAL_EXPORT_LDLIBS := $(LOCAL_LDLIBS) 

include $(BUILD_SHARED_LIBRARY)