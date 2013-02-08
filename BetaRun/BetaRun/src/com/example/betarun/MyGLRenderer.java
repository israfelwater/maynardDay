/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.betarun;
import com.example.betarun.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.LevelListDrawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {
	
	private Context mContext;
	private int numSquares = 96;

    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private Square mSquare[] = new Square[numSquares];
    private NoteBillboard mBillboard;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private final float[] mTransposeMatrix = new float[16];
    private int mTextureID;
    private Paint mLabelPaint;
    //private LabelMaker mLabels;
    //private NumericSprite mNumericSprite;
    private float mWidth = (float) 1.0;
    private float mHeight = (float) 1.0;
    
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;
     
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;
     
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;
     
    /** This is a handle to our texture data. */
    private int mTextureDataHandle;
    
    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    public volatile float[] mAmplitude = new float[numSquares];
    public volatile int mNote = 0;
    
    public MyGLRenderer(Context context){
    	mContext = context;
        mLabelPaint = new Paint();
        mLabelPaint.setTextSize(32);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
              
        // Set the background frame color
        GLES20.glClearColor(10.08f, 12.096f, 12.768f, 1.0f);

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
        		GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
        		GLES20.GL_TEXTURE_MAG_FILTER,
        		GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
        		GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
        		GLES20.GL_CLAMP_TO_EDGE);

        //GLES20.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
        //        GL10.GL_REPLACE);
        
        /*
        if (mLabels != null) {
            mLabels.shutdown(gl);
        } else {
            mLabels = new LabelMaker(true, 256, 256);
        }

        if (mNumericSprite != null) {
            mNumericSprite.shutdown(gl);
        } else {
            mNumericSprite = new NumericSprite();
        }
        mNumericSprite.initialize(gl, mLabelPaint);*/
        
        //mTriangle = new Triangle();
        //mSquare = new Square(0.f);
        for (int i = 0; i<mSquare.length; i++){
        	mSquare[i] = new Square(0.01f*i-0.48f);
        }
      
        mBillboard = new NoteBillboard(mContext);
        
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        // Start on far left
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.transposeM(mTransposeMatrix, 0, mRotationMatrix,0);
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
        
        // Draw squares
        int j = 0;
        //Matrix.setIdentityM(mTranslationMatrix, 0);
        //Matrix.translateM(mTranslationMatrix, 0,mMVPMatrix,0, -0.5f, 0.0f, 0.0f);
        //Matrix.transposeM(mTransposeMatrix,0,mTranslationMatrix,0);
        //Matrix.transposeM(mTransposeMatrix, 0, mTranslationMatrix, 0);
        // Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mTranslationMatrix, 0);
        //mSquare.draw(mMVPMatrix);
        //Matrix.multiplyMM(mMVPMatrix, 0, mTransposeMatrix, 0, mMVPMatrix, 0);
        //Matrix.scaleM(mTranslationMatrix, 0, 1.0f, 0.5f, 1.0f);
    	//Matrix.transposeM(mTransposeMatrix, 0, mTranslationMatrix, 0);
    	//Matrix.multiplyMM(mTransposeMatrix, 0, mTranslationMatrix, 0,mMVPMatrix, 0);
        for (Square mSquares:mSquare){
        	Matrix.setIdentityM(mTranslationMatrix, 0);
        	//Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        	//Matrix.transposeM(mTransposeMatrix, 0, mRotationMatrix,0);
        	//Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
        	Matrix.scaleM(mTranslationMatrix, 0, 1.0f, mAmplitude[j++], 1.0f);
        	//Matrix.transposeM(mTransposeMatrix, 0, mTranslationMatrix, 0);
        	Matrix.multiplyMM(mTransposeMatrix, 0, mTranslationMatrix , 0,mMVPMatrix, 0);
        	mSquares.draw(mTransposeMatrix);
        	
        	//Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mTransposeMatrix, 0);
        	//Matrix.multiplyMM(mMVPMatrix, 0, mTransposeMatrix, 0,mMVPMatrix , 0);
        	//Matrix.setIdentityM(mTranslationMatrix, 0);
        	//Matrix.translateM(mTranslationMatrix, 0,mMVPMatrix,0, -0.5f+(0.05f*++j), 0.0f, 0.0f);
        	//Matrix.transposeM(mTransposeMatrix,0,mTranslationMatrix,0);
        }
        
        //Matrix.multiplyMM(mMVPMatrix, 0,mMVPMatrix , 0,mTransposeMatrix , 0);
        // Create a rotation for the triangle
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.translateM(mRotationMatrix, 0, 0.1f, 0.0f, 0.0f);
               
        // Combine the rotation matrix with the projection and camera view
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
        
        // Draw triangle
        mBillboard.draw(mMVPMatrix,mNote);
        
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public void sizeChanged(GL10 gl, int w, int h) {
        mWidth = w;
        mHeight = h;
    }
    
    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

class Triangle {

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = vPosition * uMVPMatrix;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

class Square {

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = vPosition * uMVPMatrix;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[];

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Square(float xOffset) {
    	float[] Coords = {-0.005f + xOffset,  0.5f, 0.0f,   // top left
    					   -0.005f + xOffset, -0.5f, 0.0f,   // bottom left
    					    0.005f + xOffset, -0.5f, 0.0f,   // bottom right
    					    0.005f + xOffset,  0.5f, 0.0f }; // top right
    	squareCoords = Coords;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        //Matrix.multiplyMV(vertexBuffer, 0, scaleMatrix, 0, vertexBuffer, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

class NoteBillboard {
	
	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;	

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 vPosition;\n" +
        "attribute vec2 a_TexCoordinate;\n" +
        "varying vec2 v_TexCoordinate;\n" +
        "void main() {\n" +
        "  gl_Position = uMVPMatrix * vPosition;\n" +
        "  v_TexCoordinate = a_TexCoordinate;\n" +
        "}\n";
    		
    private final String fragmentShaderCode =
        "precision mediump float;\n" +
        "varying vec2 v_TexCoordinate;\n" +
        "uniform sampler2D u_Texture;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);\n" +
        "}\n";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    //private int mColorHandle;
    private int mMVPMatrixHandle;
    /** Store our model data in a float buffer. */
    private final FloatBuffer mCubeTextureCoordinates;
     
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;
     
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;
     
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;
     
    /** This is a handle to our texture data. */
    private int[] mTextureDataHandles = new int[12];

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[];

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public NoteBillboard(Context context) {
    	float[] Coords = {-0.1f, -0.3f, 0.0f,   // top left
    					   -0.1f, -0.5f, 0.0f,   // bottom left
    					    0.1f, -0.5f, 0.0f,   // bottom right
    					    0.1f, -0.3f, 0.0f }; // top right
    	squareCoords = Coords;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        
        final float[] cubeTextureCoordinateData =	{
        		1.0f, 0.0f,
        		1.0f, 1.0f,
        		0.0f, 1.0f,
        		0.0f, 0.0f   	        
    	        };
		mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
		.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
         
        
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
       
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

             
        // Load the texture
        mTextureDataHandles = loadTexture(context);
    }
    
    
    public static int[] loadTexture(final Context context)
    {
    	
        final int[] textureHandles = new int[12];
     
        GLES20.glGenTextures(12, textureHandles, 0);
        
        int[] note = {R.drawable.note0, R.drawable.note1, R.drawable.note2,
        		R.drawable.note3, R.drawable.note4, R.drawable.note5,
        		R.drawable.note6, R.drawable.note7, R.drawable.note8,
        		R.drawable.note9, R.drawable.note10, R.drawable.note11
        };
     
        int i = 0;
        for (int textureHandle:textureHandles){
        	if (textureHandle != 0){
        		
        		
        		int resourceId = note[i++];
        	
        		final BitmapFactory.Options options = new BitmapFactory.Options();
	            //options.inScaled = false;   // No pre-scaling
	     
	            // Read in the resource
	            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
	     
	            // Bind to the texture in OpenGL
	            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
	     
	            // Set filtering
	            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	     
	            // Load the bitmap into the bound texture.
	            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	     
	            // Recycle the bitmap, since its data has been loaded into OpenGL.
	            bitmap.recycle();
	        }
	     
	        if (textureHandle == 0)
	        {
	            throw new RuntimeException("Error loading texture.");
	        }
        }
	     
        return textureHandles;
    }
    
    public void draw(float[] mvpMatrix, int note) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        MyGLRenderer.checkGlError("glUseProgram");

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        //Matrix.multiplyMV(vertexBuffer, 0, scaleMatrix, 0, vertexBuffer, 0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);
        
        // get handle to fragment shader's vColor member
        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
     
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandles[note]);
     
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        
        // Set color for drawing the triangle
        //GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 
        		0, mCubeTextureCoordinates);
        
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        
        
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

