package com.ranger.szybkamapa.app.GlMapView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Environment;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final Group root;
    private float curX,curY,dX,dY,scale;
    private int wid,hei;
    private float angle;
    private SimplePlane plane;
    private Bitmap currentMap;

	public OpenGLRenderer() {
		Group group = new Group();
		root = group;
        dX = 0;
        dY = 0;
        scale = 1;
        angle = 0;

        plane = new SimplePlane(1,1);
        this.addMesh(plane);
	}
    public void showTranslate(int x,int y)
    {
        float ta = (float) Math.toRadians(angle);
        dY += (float) (((y/scale) * Math.cos(ta)) - ((x/scale) * Math.sin(ta)) );
        dX += (float) (((y/scale) * Math.sin(ta)) + ((x/scale) * Math.cos(ta)) );
    }
    public void setScale(float scale)
    {
        this.scale = scale;
    }

    @Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);
    }

    @Override
	public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glPushMatrix();

        gl.glTranslatef(wid/2 , hei/2, 0f);
        gl.glRotatef(angle,0,0,1);
        gl.glTranslatef( (-curX-dX) * Math.abs(scale),  (-curY-dY) * Math.abs(scale), 0f);
        gl.glScalef(scale, scale, 1.0f);
        gl.glTranslatef(-wid/2 , -(hei/2)+((hei-wid)/2), 0f);

        root.draw(gl);
        gl.glPopMatrix();

	}
    public void reInitialize()
    {
        if(currentMap != null)
        plane.loadBitmap(currentMap);
    }
    public void setMap(String path) // mapa jest przechowywana w pamieci.
    {
        currentMap = BitmapFactory.decodeFile(path);
        plane.loadBitmap(currentMap);
    }

    @Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.wid = width;
        this.hei = height;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, width, height, 0f, 0f, 1.0f);

        plane.setSize(width,width);
    }

	public void addMesh(Mesh mesh) {
		root.add(mesh);
	}
}
