package com.example.betarun.openGL.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.example.betarun.openGL.MyGLRenderer;
import com.example.betarun.openGL.utils.ModeNodes;
import com.example.betarun.openGL.utils.SoundParticleHexBins;

import android.opengl.GLES20;

public class BaseParticles {
	private final String vertexShaderCode =
	        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = vPosition * uMVPMatrix;" +
        "  gl_PointSize = 2.0;" +
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
    float[] particleCoords, particleMomentums; 
    private int vertexCount; 
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    protected float color[];
    protected float towardNodes;

    public BaseParticles(int numParticles) {
    	
    	particleCoords = randomPositions(numParticles);
    	particleMomentums = new float[numParticles*2]; // direction and speed vectors
    	
    	vertexCount = particleCoords.length / COORDS_PER_VERTEX;
    	    
    	
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
        		particleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(particleCoords);
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

    private float[] randomPositions(int numParticles) {
		float[] positions = new float[numParticles*3];
		
		for (int i = 0; i < positions.length;){
			float randAngle = (float) (2.0*Math.PI*Math.random());
			float randDist = (float) (Math.random());
			positions[i++] = (float) (randDist*Math.cos(randAngle));
			positions[i++] = (float) (randDist*Math.sin(randAngle));
			positions[i++] = 0.0f; //in z plane
		}
		return positions;
	}

	public void draw(float[] mvpMatrix, float radius, int mode, float amp) {
		
		MoveParticiles(mode,radius,amp);
		
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
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

	private void MoveParticiles(int mode, float radius, float amplitude) {
		int k = 0;
		float[][] nodes = ModeNodes.GetNodes(mode); 
		for (int i = 0; i<particleCoords.length;){
			float x = particleCoords[i];
			float y = particleCoords[i+1];
			double angle;
			float force;
			float damping; 
			
			// move particle inside the circle if out
			float dist2center = SoundParticleHexBins.distance(0.0f, 0.0f, x,y);
			if (dist2center>radius) {
				angle = Math.atan2(y,x);
				x = (float) (radius*Math.cos(angle));
				y = (float) (radius*Math.sin(angle));
			}
			
			// find the closest node
			float dist = 100; 
			int node = 0;
			float nodeX = 0;
			float nodeY = 0;
			if (nodes==null){
				angle = Math.atan2(y,x);
				int happy = (int) angle;
			}
			for (int j = 0; j<nodes.length; j++){
				nodeX = nodes[j][0]*radius;
				nodeY = nodes[j][1]*radius;
				float newDis = SoundParticleHexBins.distance(nodeX,nodeY,x,y);
				if (newDis < dist) { dist = newDis; node = j;}
			}
			angle = Math.atan2((y-(nodes[node][1])),(x-(nodes[node][0])));
			
			float nodeRadius = radius*ModeNodes.GetNodeRadius(mode);
			/* calculate force from node
			nodeRadius = radius/mode;
			if (dist>=nodeRadius) {
				force = 1.0f;
				angle = 2.0f*Math.PI*Math.random();
				damping = 0.98f; // no damping
			} else {
				force = (dist/nodeRadius);
				damping = 0.98f;
			}//*/
			
			//* calculate distance from node
			force = amplitude;
			angle = 2.0f*Math.PI*Math.random(); //new random angle
			if (towardNodes<0){
				damping = (float) (1 - Math.pow(Math.cos(2.0f*Math.PI*dist/nodeRadius),3));
			} else {
				damping = (float) (1 - Math.pow(Math.sin(2.0f*Math.PI*dist/nodeRadius),3));
			}
			
			//*/
			
			// update particleMomentum
			 
			particleMomentums[k] = (float) (damping*particleMomentums[k]-
					towardNodes*force*Math.cos(angle));
			particleMomentums[k+1] = (float) (damping*particleMomentums[k+1]-
					towardNodes*force*Math.sin(angle));
			
			// update particlePosition
			x+=particleMomentums[k++]/1000;
			y+=particleMomentums[k++]/1000;
			
			if (SoundParticleHexBins.distance(0.0f, 0.0f, x,y)>radius){
				angle = Math.atan2(y,x);
				x = (float) (radius*Math.cos(angle));
				y = (float) (radius*Math.sin(angle));
				particleMomentums[k-2]=0;
				particleMomentums[k-1]=0;
			}
			
			particleCoords[i++] = x; // x iter to y
			particleCoords[i++] = y; // y iter to z
			i++; // skip z
			
		}
		
		// clear certexBuffer
		vertexBuffer.clear();
		// add the coordinates to the FloatBuffer
        vertexBuffer.put(particleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
		
	}
}