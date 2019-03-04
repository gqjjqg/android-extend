
/**
 *  When an array element i is transferred to the GL by the DrawArrays or DrawElements commands, 
 *  each generic attribute is expanded to four components. If size is one then the x component 
 *  of the attribute is specified by the array; the y, z, and w components are implicitly set to
 *  zero, zero, and one, respectively. If size is two then the x and y components of the attribute
 *  are specified by the array; the z, and w components are implicitly set to zero, and one, 
 *  respectively. If size is three then x, y, and z are specified, and w is implicitly set to one. 
 *  If size is four then all components are specified.
 *  
 */
static const  char* pVertexShaderStr =
"attribute vec4 a_position;   								\n \
uniform mat4 umMatrix;					       				\n \
uniform mat4 uvMatrix;										\n \
attribute vec2 a_texCoord;   								\n \
varying highp vec2 v_texCoord; 								\n \
void main()                  								\n \
{                            								\n \
	gl_Position = a_position* umMatrix * uvMatrix;	        \n \
	v_texCoord = a_texCoord;  								\n \
}                            								\n";

static const  char* pVertexShaderStrMatrix =
"uniform mat4 uMMatrix;					       				\n \
uniform mat4 uVMatrix;						    			\n \
attribute vec4 a_position;							    	\n \
void main() {									    		\n \
	gl_Position = a_position * uMMatrix * uVMatrix ;		\n \
}													   		\n";

static const  char* pVertexShaderSimple =
"attribute vec4 a_position;							    	\n \
void main() {									    		\n \
	gl_Position = a_position ;								\n \
}													   		\n";

static const  char* pFragmentShaderI420 =
"precision highp float;										\n \
uniform sampler2D y_texture;								\n \
uniform sampler2D uv_texture;								\n \
uniform sampler2D v_texture;								\n \
varying highp vec2 v_texCoord;								\n \
void main()													\n \
{															\n \
    mediump vec3 yuv;										\n \
    highp vec3 rgb; 										\n \
    yuv.x = texture2D(y_texture, v_texCoord).r;  			\n \
    yuv.y = texture2D(uv_texture, v_texCoord).r;		    \n \
    yuv.z = texture2D(v_texture, v_texCoord).r;		        \n \
    rgb = mat3(      1,       1,       1,					\n \
              0, -0.344, 1.770,								\n \
              1.403, -0.714,       0) * yuv;				\n \
    gl_FragColor = vec4(rgb, 1);							\n \
}															\n";

static const  char* pFragmentShaderYUYV =
"precision highp float;										\n \
uniform sampler2D y_texture;								\n \
uniform sampler2D uv_texture;								\n \
varying highp vec2 v_texCoord;								\n \
void main()													\n \
{															\n \
    mediump vec3 yuv;										\n \
    highp vec3 rgb; 										\n \
    yuv.x = texture2D(y_texture, v_texCoord).r;  			\n \
    yuv.y = texture2D(uv_texture, v_texCoord).g-0.5;		\n \
    yuv.z = texture2D(uv_texture, v_texCoord).a-0.5;		\n \
    rgb = mat3(      1,       1,       1,					\n \
              0, -0.344, 1.770,								\n \
              1.403, -0.714,       0) * yuv;				\n \
    gl_FragColor = vec4(rgb, 1);							\n \
}															\n";

static const  char* pFragmentShaderNV21 =
"precision highp float;										\n \
uniform sampler2D y_texture;								\n \
uniform sampler2D uv_texture;								\n \
varying highp vec2 v_texCoord;								\n \
void main()													\n \
{			 												\n \
    mediump vec3 yuv;										\n \
    highp vec3 rgb; 										\n \
    yuv.x = texture2D(y_texture, v_texCoord).r;  			\n \
    yuv.y = texture2D(uv_texture, v_texCoord).a-0.5;		\n \
    yuv.z = texture2D(uv_texture, v_texCoord).r-0.5;		\n \
    rgb = mat3( 1,       1,       1,					    \n \
              0, -0.344, 1.770,								\n \
              1.403, -0.714,       0) * yuv;				\n \
    gl_FragColor = vec4(rgb, 1);							\n \
}															\n";

static const  char* pFragmentShaderNV12 =
"precision highp float; 									\n \
uniform sampler2D y_texture;								\n \
uniform sampler2D uv_texture;								\n \
varying highp vec2 v_texCoord;								\n \
void main()													\n \
{															\n \
    mediump vec3 yuv;										\n \
    highp vec3 rgb; 										\n \
    yuv.x = texture2D(y_texture, v_texCoord).r;  			\n \
    yuv.y = texture2D(uv_texture, v_texCoord).r-0.5;		\n \
    yuv.z = texture2D(uv_texture, v_texCoord).a-0.5;		\n \
    rgb = mat3(      1,       1,       1,					\n \
              0, -0.344, 1.770,								\n \
              1.403, -0.714,       0) * yuv;				\n \
    gl_FragColor = vec4(rgb, 1);							\n \
}															\n";

static const  char* pFragmentShaderColor =
"precision mediump float;									\n \
uniform vec4 vColor;										\n \
void main()													\n \
{															\n \
 	gl_FragColor = vColor;									\n \
}															\n";

static const  char* pFragmentShaderSimple =
"precision mediump float;									\n \
void main()													\n \
{															\n \
	 gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); 				\n \
}															\n";
