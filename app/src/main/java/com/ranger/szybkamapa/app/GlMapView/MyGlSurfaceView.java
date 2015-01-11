package com.ranger.szybkamapa.app.GlMapView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.ranger.szybkamapa.app.Map.Crd;
import com.ranger.szybkamapa.app.Map.Map;

/**
 * Created by Michal on 2014-12-29.
 */
public class MyGlSurfaceView extends GLSurfaceView {

    public static int INVALID_POINTER_ID = -1;
    private final OpenGLRenderer renderer;
    private float sx,sy; // old mouse position;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private boolean scale_flag;
    private int mActivePointerId = INVALID_POINTER_ID;

    public MyGlSurfaceView(Context context)
    {
        super(context);
        renderer = new OpenGLRenderer();
        setRenderer(renderer);
    }

    public MyGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        renderer = new OpenGLRenderer();
        setRenderer(renderer);

        if(isInEditMode()) return; // w edytorze XML nie produkuj się z resztą kodu - tylko podstawowy view

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mScaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        mScaleDetector.onTouchEvent(e);

        if(mScaleDetector.isInProgress())
            return true;

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE:

                if(scale_flag)
                    return true;

                renderer.showTranslate((int) (sx - e.getX()), (int) (sy - e.getY()));
                sx = (int)e.getX();
                sy = (int)e.getY();
            break;

            case MotionEvent.ACTION_DOWN:
                sx = (int)e.getX();
                sy = (int)e.getY();
            break;

            case MotionEvent.ACTION_UP:
                scale_flag = false;
            break;
        }
        requestRender();
        return true;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        renderer.reInitialize();
    }
    @Override
    public  void onPause()
    {
        super.onPause();
    }

    public void setPosition(Crd pozycja)
    {
        renderer.setPosition(pozycja);
        requestRender();
    }
    public boolean setMap(Map mapa)
    {
        if(mapa.initialized == true) {
            renderer.setMap(mapa);
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 8.0f));
            renderer.setScale(mScaleFactor);
            //Log.v("APKA", "SKALUJE" + mScaleFactor);
            requestRender();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            requestRender();
            scale_flag = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            requestRender();
        }
    }
}
