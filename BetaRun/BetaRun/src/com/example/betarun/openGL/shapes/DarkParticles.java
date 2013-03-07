package com.example.betarun.openGL.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.example.betarun.openGL.MyGLRenderer;
import com.example.betarun.openGL.utils.ModeNodes;
import com.example.betarun.openGL.utils.SoundParticleHexBins;

import android.opengl.GLES20;

public class DarkParticles extends BaseParticles {

	public DarkParticles(int numParticles) {
		super(numParticles);
		float darkColor[] = { 1.0f, 0.0f, 1.0f, 1.0f };
		color = darkColor;
		towardNodes=1; //head toward nodes
	}
	
}
