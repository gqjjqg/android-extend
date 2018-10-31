#include <jni.h>

#include <stdio.h>
#include <string.h>

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "Matrix.h"

#include "image.h"
#include "loger.h"
#include "image_render.h"
#include "shader.h"

typedef struct opengl_t {
    GLuint	mProgram;
    GLuint	mGLBufs[3];
	GLuint	mTextures[3];
    GLfloat mVertices[12];
    GLfloat mCoords[8];
    GLushort mIndexs[6];
    GLuint  mGLIndex[3];
	int	mMirror;
	int	mOrientation;
	int	mPixelFormat;
}OPENGLES, *LPOPENGLES;

static GLuint LoadShader(GLenum shaderType, const char* pSource);

int GLImageRenderInit(int mirror, int ori, int format)
{
	LPOPENGLES engine;
	GLuint	vertexShader;
	GLuint	fragmentShader;
	GLint	linked;

	LOGD("GLImageInit glesInit() <--- format = %d", format);

	engine = (LPOPENGLES)malloc(sizeof(OPENGLES));
	engine->mProgram		    = 0;
	engine->mMirror				= mirror;
	engine->mOrientation	    = ori;
	engine->mPixelFormat 		= format;
    engine->mGLIndex[0]         = 0;
    engine->mGLIndex[1]         = 1;
    engine->mGLIndex[2]         = 2;


	vertexShader = LoadShader(GL_VERTEX_SHADER, pVertexShaderStr);
	if (engine->mPixelFormat == CP_PAF_NV21) {
		fragmentShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentShaderNV21);
	} else if (engine->mPixelFormat == CP_PAF_NV12) {
		fragmentShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentShaderNV12);
	} else if (engine->mPixelFormat == CP_PAF_YUYV) {
		fragmentShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentShaderYUYV);
	} else if (engine->mPixelFormat == CP_PAF_I420) {
        fragmentShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentShaderI420);
    }

	engine->mProgram = glCreateProgram();
	if (0 == engine->mProgram) {
		LOGE("create programObject failed");
		return 0;
	}

	LOGD("glAttachShader");

	glAttachShader(engine->mProgram, vertexShader);
	glAttachShader(engine->mProgram, fragmentShader);

	LOGD("glBindAttribLocation");
	glBindAttribLocation(engine->mProgram,  engine->mGLIndex[0], "a_position");
	glBindAttribLocation(engine->mProgram,  engine->mGLIndex[1], "a_texCoord");

	glLinkProgram ( engine->mProgram );
	LOGD("glLinkProgram");

	glGetProgramiv( engine->mProgram, GL_LINK_STATUS, &linked);
	if (0 == linked) {
		GLint	infoLen = 0;
		LOGE("link failed");
		glGetProgramiv( engine->mProgram, GL_INFO_LOG_LENGTH, &infoLen);

		if (infoLen > 1) {
			char* infoLog = (char*)malloc(sizeof(char) * infoLen);

			glGetProgramInfoLog( engine->mProgram, infoLen, NULL, infoLog);
			LOGE( "Error linking program: %s", infoLog);

			free(infoLog);
			infoLog = NULL;
		}

		glDeleteProgram( engine->mProgram);
        LOGE("link failed -> out");
		return 0;
	}

	//glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	//glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	//glEnable(GL_TEXTURE_2D);

	LOGD("glGenTextures");
	// Textures
	if (engine->mPixelFormat == CP_PAF_NV21 || engine->mPixelFormat == CP_PAF_NV12 ||
	    engine->mPixelFormat == CP_PAF_YUYV) {
	    glGenTextures(2, engine->mTextures);
	    glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	} else if (engine->mPixelFormat == CP_PAF_I420) {
	    glGenTextures(3, engine->mTextures);
	    glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[2]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	LOGD("VBO");
	//VBO
	glGenBuffers(3, engine->mGLBufs);

    memset(engine->mVertices, 0, sizeof(engine->mVertices));
    memset(engine->mCoords, 0, sizeof(engine->mCoords));

    //engine->m_Scale = 1.0;
    engine->mVertices[0] = -1.0; engine->mVertices[1] = 1.0;  //1.0f, // Position 0
    engine->mVertices[3] = -1.0; engine->mVertices[4] = -1.0; //1.0f, // Position 1
    engine->mVertices[6] = 1.0; engine->mVertices[7] = -1.0;  //1.0f, // Position 2
    engine->mVertices[9] = 1.0; engine->mVertices[10] = 1.0;  //1.0f, // Position 3
    //degree 0
    engine->mCoords[0] = 0.0; engine->mCoords[1] = 0.0;
    engine->mCoords[2] = 0.0; engine->mCoords[3] = 1.0;
    engine->mCoords[4] = 1.0; engine->mCoords[5] = 1.0;
    engine->mCoords[6] = 1.0; engine->mCoords[7] = 0.0;
	//index
    engine->mIndexs[0] = 0; engine->mIndexs[1] = 1; engine->mIndexs[2] = 2;
    engine->mIndexs[3] = 0; engine->mIndexs[4] = 2; engine->mIndexs[5] = 3;

	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[0]);
	glBufferData(GL_ARRAY_BUFFER, sizeof(engine->mVertices), engine->mVertices, GL_DYNAMIC_DRAW);
	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[1]);
	glBufferData(GL_ARRAY_BUFFER, sizeof(engine->mCoords), engine->mCoords, GL_DYNAMIC_DRAW);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, engine->mGLBufs[2]);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(engine->mIndexs), engine->mIndexs, GL_STATIC_DRAW);

	LOGD("glesInit() --->");

	return (long)engine;
}

void GLImageRenderChanged(int handle, int w, int h)
{
	LPOPENGLES engine = (LPOPENGLES)handle;
    LOGD("glesChanged(%d, %d) %x<---", w, h, handle);
    if (engine != NULL) {
        //engine->m_bTexInit = -1;
        glViewport(0, 0, w, h);
    }
    LOGD("glesChanged() --->");
}

void GLImageRenderChangedConfig(int handle, int mirror, int ori)
{
    LPOPENGLES engine = (LPOPENGLES)handle;
    LOGD("GLChangedAngle(%d, %d) <---", mirror, ori);
	if (engine != NULL) {
		engine->mMirror = mirror;
		engine->mOrientation = ori;
	}
    LOGD("GLChangedAngle() --->");
}

void GLImageRender(int handle, unsigned char* pData, int w, int h)
{
	LPOPENGLES engine = (LPOPENGLES)handle;
	if (pData == NULL || engine == NULL) {
		LOGE("GLImageRender FAIL!: 0x%X 0x%X", engine, pData);
		return;
	}
	
	//clean
	glClear ( GL_COLOR_BUFFER_BIT );

	//Texture -> GPU
	if (engine->mPixelFormat == CP_PAF_NV21 || engine->mPixelFormat == CP_PAF_NV12) {
		glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w, h, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pData);
		glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, w >> 1, h >> 1, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, pData + w * h);
	} else if (engine->mPixelFormat == CP_PAF_YUYV) {
		glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, w, h, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, pData);
		glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w >> 1, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, pData);
	} else if (engine->mPixelFormat == CP_PAF_I420) {
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w, h, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pData);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w >> 1, h >> 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pData + w * h);
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[2]);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, w >> 1, h >> 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pData + w * h + w * h / 4);
	}
    GLfloat mViewMatrix[16];
    GLfloat mModelMatrix[16];
	/**
	 * left right bottom, top, near, far
	 */
	Matrix::matrixFrustumM(mModelMatrix, -0.5f, 0.5f, -0.5f, 0.5f, 1, 10);
	/**
	 * eye x, y, z, 	look x, y, z, 	up x, y ,z
	 */
	Matrix::matrixLookAtM(mViewMatrix, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
	Matrix::matrixRotateM(mViewMatrix, engine->mOrientation, 0, 0, 1);
	Matrix::matrixScaleM(mViewMatrix, (engine->mMirror & 0x1) == 1 ? -1.0f : 1.0f, (engine->mMirror & 0x2) == 0x2 ? -1.0f : 1.0f, 1.0f);
    LOGE("Image %d %d", engine->mOrientation, engine->mMirror);
	// use shader
	glUseProgram ( engine->mProgram );
	GLuint m1 = glGetUniformLocation(engine->mProgram, "uvMatrix");
	GLuint m2 = glGetUniformLocation(engine->mProgram, "umMatrix");
    GLuint textureUniformY = glGetUniformLocation(engine->mProgram, "y_texture");
    GLuint textureUniformU = glGetUniformLocation(engine->mProgram, "uv_texture");

	glUniformMatrix4fv(m1, 1, GL_FALSE, mViewMatrix);
	glUniformMatrix4fv(m2, 1, GL_FALSE, mModelMatrix);

    glBindTexture(GL_TEXTURE_2D, engine->mTextures[0]);
    glUniform1i(textureUniformY, 0);

    glBindTexture(GL_TEXTURE_2D, engine->mTextures[1]);
    glUniform1i(textureUniformU, 1);

    if (engine->mPixelFormat == CP_PAF_I420) {
        GLuint textureUniformV = glGetUniformLocation(engine->mProgram, "v_texture");
        glBindTexture(GL_TEXTURE_2D, engine->mTextures[2]);
        glUniform1i(textureUniformV, 2);
    }

	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[0]);
    glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(engine->mVertices), engine->mVertices);
	glVertexAttribPointer (  engine->mGLIndex[0], 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), 0 );
	glEnableVertexAttribArray( engine->mGLIndex[0]);

	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[1]);
    glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(engine->mCoords), engine->mCoords);
	glVertexAttribPointer (  engine->mGLIndex[1], 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), 0 );
	glEnableVertexAttribArray( engine->mGLIndex[1]);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, engine->mGLBufs[2]);
	glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0 );

}

void GLImageRenderUnInit(int handle)
{
	LPOPENGLES engine = (LPOPENGLES)handle;
    if (engine != NULL) {
        free(engine);
    }
}

GLuint LoadShader(GLenum shaderType, const char* pSource)
{
    GLuint shader = 0;
	shader = glCreateShader(shaderType);
	LOGD("glGetShaderiv called  shader = %d GL_INVALID_ENUM = %d GL_INVALID_OPERATION = %d", shader, GL_INVALID_ENUM, GL_INVALID_OPERATION);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 1;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        LOGD( "glGetShaderiv called compiled = %d, shader = %d", compiled, shader);
        if (!compiled)
		{
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen)
			{
                char* buf = (char*) malloc(infoLen);
                if (buf)
				{
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d: %s",
                            shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
			return 0;
        }
    }
    return shader;
}


