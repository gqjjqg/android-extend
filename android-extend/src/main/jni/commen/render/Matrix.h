#ifndef MATRIX_H
#define MATRIX_H

#include <stdlib.h>
#include <math.h>



class Matrix
{

//	void Matrix();
//	void ~Matrix();

public:
	static void matrixSetIdentityM(float *m);
	static void matrixSetRotateM(float *m, float a, float x, float y, float z);
	static void matrixMultiplyMM(float *m, float *lhs, float *rhs);
	static void matrixScaleM(float *m, float x, float y, float z);
	static void matrixTranslateM(float *m, float x, float y, float z);
	static void matrixRotateM(float *m, float a, float x, float y, float z);
	static void matrixLookAtM(float *m,
	                float eyeX, float eyeY, float eyeZ,
	                float cenX, float cenY, float cenZ,
	                float  upX, float  upY, float  upZ);
	static void matrixFrustumM(float *m, float left, float right, float bottom, float top, float near, float far);

};


#endif
