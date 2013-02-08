package com.example.betarun;

import com.example.betarun.MyGLRenderer;

import android.app.AlertDialog;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    
    Context mContext;
    
    private int[] NoteSpectrum;
	float maxAmplitude = 0;
	int maxAmpIdx = 0;
    
    public TextView note_display;

    public MyGLSurfaceView(Context context,int[] noteSpectrum) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        NoteSpectrum = noteSpectrum;
        
        mContext = context;
        
        //bring textView into view
        //note_display = findViewById(R.id.note_reading).;
        
        
        //note_display = createTextView(mContext);
        
        //builder.makeText(context, R.array.notes, 1/44100);
        //builder.setView(this);
        //builder.show();
    }
    
    private TextView createTextView(Context context) {
        TextView noteView = new TextView(context);
        noteView.setId(R.id.note_reading);
        noteView.setGravity(TEXT_ALIGNMENT_TEXT_START);
        noteView.setHeight(100);
        noteView.setWidth(100);
        noteView.setText(R.string.StartingNote);
        noteView.setBackgroundColor(0);
    	noteView.bringToFront();
    	/*<TextView
        android:id="@+id/note_reading"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:text="@string/StartingNote"
        android:textAppearance="?android:attr/textAppearanceMedium" />*/
		return noteView;
	}

	public MyGLSurfaceView getMyGLSurfaceView(Context context){
    	return this;
    }
    

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }

                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
    public boolean updateAmplitudes(float[] amplitudes) throws Exception{
    	mRenderer.mAmplitude = amplitudes;
    	for (int i=0;i<96;i++){
    		if (mRenderer.mAmplitude[i]>maxAmplitude){
    			maxAmplitude = mRenderer.mAmplitude[i];
    			maxAmpIdx = i;
    		}
    	}
    	//builder.cancel();
    	//builder.
    	//builder.show();
    	mRenderer.mNote = NoteSpectrum[maxAmpIdx];
    	//note_display.bringToFront();
    	
        requestRender();
    	return true;
    }
}
