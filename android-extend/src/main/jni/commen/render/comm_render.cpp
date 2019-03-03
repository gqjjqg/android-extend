#include <jni.h>

#include <stdio.h>
#include <string.h>

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "Matrix.h"

#include "image.h"
#include "logger.h"
#include "comm_render.h"
#include "shader.h"

typedef struct opengl_t {
    GLuint	mProgram;
    GLuint	mGLBufs[3];
	GLfloat mColors[4];
	GLfloat mFloatData[12];
    GLuint  mGLIndex[3];
	int	mMirror;
	int	mOrientation;
	int	mPixelFormat;
}OPENGLES, *LPOPENGLES;

static GLuint LoadShader(GLenum shaderType, const char* pSource);

INT GLCommRenderInit(int mirror, int ori, int format)
{
	LPOPENGLES engine;
	GLuint	vertexShader;
	GLuint	fragmentShader;
	GLint	linked;

	LOGD("GLDrawInit glesInit() <--- format = %d", format);

	engine = (LPOPENGLES)malloc(sizeof(OPENGLES));
	engine->mProgram		= 0;
	engine->mMirror		    = mirror;
	engine->mOrientation	= ori;
	engine->mPixelFormat 	= format;
    engine->mGLIndex[0]     = 0;
    engine->mGLIndex[1]     = 1;
    engine->mGLIndex[2]     = 2;

	vertexShader = LoadShader(GL_VERTEX_SHADER, pVertexShaderStrMatrix); //
	fragmentShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentShaderColor);
	LOGD("GLDrawInit glCreateProgram");
	engine->mProgram = glCreateProgram();
    if (0 == engine->mProgram) {
        LOGE("create programObject failed");
        return 0;
    }

	glAttachShader(engine->mProgram, vertexShader);
	glAttachShader(engine->mProgram, fragmentShader);

	glBindAttribLocation(engine->mProgram, engine->mGLIndex[0], "a_position");

	glLinkProgram(engine->mProgram);

	LOGD("GLDrawInit glLinkProgram");

	glGetProgramiv( engine->mProgram, GL_LINK_STATUS, &linked);
	LOGD("GLDrawInit glGetProgramiv");
	if (0 == linked) {
		GLint infoLen = 0;
		LOGE("GLDrawInit link failed");
		glGetProgramiv( engine->mProgram, GL_INFO_LOG_LENGTH, &infoLen);

		if (infoLen > 1) {
			char* infoLog = (char*)malloc(sizeof(char) * infoLen);

			glGetProgramInfoLog( engine->mProgram, infoLen, NULL, infoLog);
			LOGE( "Error linking program: %s", infoLog);

			free(infoLog);
			infoLog = NULL;
		}

		glDeleteProgram( engine->mProgram);
        LOGE("GLDrawInit link failed -> out");
		return 0;
	}

	glValidateProgram(engine->mProgram);
	glGetProgramiv( engine->mProgram, GL_VALIDATE_STATUS, &linked);
	if (linked == 0) {
		LOGE("program failed");
		return 0;
	}

	glGenBuffers(1, engine->mGLBufs);

	//VBO
	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[0]);
	glBufferData(GL_ARRAY_BUFFER, sizeof(engine->mFloatData), engine->mFloatData, GL_STREAM_DRAW);

	LOGD("glesInit() --->");
	return (INT)engine;
}

void GLCommRenderChanged(INT handle, int w, int h)
{
	LPOPENGLES engine = (LPOPENGLES)handle;
    LOGD("glesChanged(%d, %d) %x<---", w, h, handle);
    if (engine != NULL) {
        //engine->m_bTexInit = -1;
        glViewport(0, 0, w, h);
    }
    LOGD("glesChanged() --->");
}

void GLCommRenderChangedConfig(INT handle, int mirror, int ori)
{
    LPOPENGLES engine = (LPOPENGLES)handle;
    LOGD("GLChangedAngle(%d, %d) <---", mirror, ori);
	if (engine != NULL) {
		engine->mMirror = mirror;
		engine->mOrientation = ori;
	}
    LOGD("GLChangedAngle() --->");
}

void GLCommRenderDrawRect( INT handle, int w, int h, int *point, int rgb, int size)
{
	int i, j;
	LPOPENGLES engine = (LPOPENGLES)handle;
	if (engine == NULL) {
		LOGE("engine == MNull");
		return;
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
    Matrix::matrixScaleM(mViewMatrix, (engine->mMirror & 0x1) == 0x1 ? -1.0f : 1.0f, (engine->mMirror & 0x2) == 0x2 ? -1.0f : 1.0f, 1.0f);
	//if ((engine->mTest & 1) == 1) {
    //    Matrix::matrixScaleM(mViewMatrix, 1.0f, 1.0f, 1.0f);
	//} else {
    //    Matrix::matrixScaleM(mViewMatrix, -1.0f, -1.0f , 1.0f);
	//}
    //engine->mTest++;
    LOGE("Rect %d %d", engine->mOrientation, engine->mMirror);

	// use shader
	glUseProgram ( engine->mProgram );
	engine->mColors[0] = (((rgb >> 16) & 0xFF) / 255.0f);
	engine->mColors[1] = (((rgb >> 8) & 0xFF) / 255.0f);
	engine->mColors[2] = ((rgb & 0xFF) / 255.0f);
	engine->mColors[3] = (((rgb >> 24) & 0xFF) / 255.0f);

	GLuint m1 = glGetUniformLocation(engine->mProgram, "uVMatrix");
	GLuint m2 = glGetUniformLocation(engine->mProgram, "uMMatrix");
	GLuint c1 = glGetUniformLocation(engine->mProgram, "vColor");

	glUniformMatrix4fv(m1, 1, GL_FALSE, mViewMatrix);
	glUniformMatrix4fv(m2, 1, GL_FALSE, mModelMatrix);
	glUniform4fv(c1, 1, engine->mColors);

	for (i = 0, j = 0; i < 8; i += 2, j += 3){
		engine->mFloatData[j] = ((2.0f * point[i]) / (w - 1)) - 1.0f;
		engine->mFloatData[j + 1] = 1.f - ((2.0f * point[i + 1]) / (h - 1));
        engine->mFloatData[j + 2] = 0;
	}

	// update data.
	glBindBuffer(GL_ARRAY_BUFFER, engine->mGLBufs[0]);
	glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(engine->mFloatData), engine->mFloatData);
	glVertexAttribPointer(engine->mGLIndex[0], 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), 0);
	glEnableVertexAttribArray(engine->mGLIndex[0]);

    glLineWidth(size);
	glDrawArrays(GL_LINE_LOOP, 0, 4);
}

void GLCommRenderUnInit(INT handle)
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


