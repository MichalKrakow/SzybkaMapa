package com.ranger.szybkamapa.app.GlMapView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ranger.szybkamapa.app.Map.Crd;
import com.ranger.szybkamapa.app.Map.Map;
import com.ranger.szybkamapa.app.Tools.BitmapResources;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final Group root;
    private float dX,dY,scale;
    private int wid,hei,dx,dy,plane_w,plane_h; //dx,dy - przesuniecie dla wysrodkowania
    private float angle;
    private SimplePlane plane;
    private SimplePlane pointer;
    private Bitmap currentMap;
    private Map mapa;
    private boolean boundToPosition = false;
    private Crd pozycja;

	public OpenGLRenderer() {
		Group group = new Group();
		root = group;
        dX = 0;
        dY = 0;
        scale = 1;
        angle = 0;
        pozycja = new Crd(0,0);
        plane_w = 1;
        plane_h = 1;

        plane = new SimplePlane(plane_w,plane_h);
        //this.addMesh(plane);

        pointer = new SimplePlane(64,64);
        //this.addMesh(pointer);
	}
    public void showTranslate(int x,int y)
    {
        float ta = (float) Math.toRadians(angle);
        dY += (float) (((y/scale) * Math.cos(ta)) - ((x/scale) * Math.sin(ta)) );
        dX += (float) (((y/scale) * Math.sin(ta)) + ((x/scale) * Math.cos(ta)) );
        Log.v("POZYCJA",dX + "/ "+dY);
    }
    public void setPosition(Crd pozycja)
    {
        //this.pozycja = pozycja;
        this.pozycja.x = ((pozycja.x/mapa.image_width)*plane_w) - (wid/2);
        this.pozycja.y = ((pozycja.y/mapa.image_height)*plane_h) - (hei/2);
        Log.v("POZYCJA",this.pozycja.toString());
    }
    public void setScale(float scale)
    {
        this.scale = scale;
    }

    @Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
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

        //boundToPosition = true;

        if(!boundToPosition)
        gl.glTranslatef( (-dX) * Math.abs(scale),  (-dY) * Math.abs(scale), 0f);
        else
        gl.glTranslatef((float) -pozycja.x * Math.abs(scale),(float) -pozycja.y * Math.abs(scale), 0f);
        //gl.glTranslatef((float) ((-pozycja.x) * Math.abs(scale)), (float) ((-pozycja.y) * Math.abs(scale)), 0f);

        gl.glScalef(scale, scale, 1.0f);

        //gl.glTranslatef(-wid/2 , -(hei/2)+((hei-wid)/2), 0f);
        gl.glTranslatef((-wid/2)+dx , (-hei/2)+dy, 0f);


        plane.draw(gl);
        gl.glPopMatrix();


        gl.glPushMatrix();

        gl.glTranslatef(wid/2 , hei/2, 0f);
        gl.glRotatef(angle,0,0,1);


        if(!boundToPosition)
            gl.glTranslatef( (-dX) * Math.abs(scale),  (-dY) * Math.abs(scale), 0f);
        else
            gl.glTranslatef((float) -pozycja.x * Math.abs(scale),(float) -pozycja.y * Math.abs(scale), 0f);

        gl.glTranslatef((float) (pozycja.x + (wid/2)) * Math.abs(scale),(float) (pozycja.y+(hei/2)) * Math.abs(scale), 0f);

        gl.glScalef(scale, scale, 1.0f);

        //gl.glTranslatef(-wid/2 , -(hei/2)+((hei-wid)/2), 0f);
        gl.glTranslatef((-wid/2)+dx , (-hei/2)+dy, 0f);

        gl.glScalef(1/scale, 1/scale, 1.0f);
        gl.glTranslatef(-32,-32,0);

        pointer.draw(gl);

        gl.glPopMatrix();

	}
    public void reInitialize()
    {
        if(currentMap != null)
        plane.loadBitmap(currentMap);
        pointer.loadBitmap(BitmapResources.getInstance().getBitmap(0));
    }
    public void setMap(Map mapa) // mapa jest przechowywana w pamieci.
    {
        this.mapa = mapa;
        currentMap = BitmapFactory.decodeFile(mapa.image);
        plane.loadBitmap(currentMap);

        setSize();
    }

    private void setSize()
    {
        if(mapa == null)
        {
            plane.setSize(wid,hei);
            return;
        }

        if(mapa.image_width > mapa.image_height)
        {
            float ch = mapa.image_height *((float)wid/mapa.image_width);
            plane_w = wid;
            plane_h = (int) ch;
            plane.setSize(plane_w, plane_h);

            dx = 0;
            dy = (int) ((hei-ch)/2);
        }
        else if(mapa.image_width < mapa.image_height)
        {
            float cw = mapa.image_width*((float)hei/mapa.image_height);
            plane_w = (int) cw;
            plane_h = hei;
            plane.setSize(plane_w, plane_h);

            dy = 0;
            dx = (int) ((wid-cw)/2);
        }
    }

    @Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.wid = width;
        this.hei = height;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, width, height, 0f, 0f, 1.0f);

        setSize(); // usaw rozmiar kafla w zaleznosci od wczytanej mapy
    }

	public void addMesh(Mesh mesh) {
		root.add(mesh);
	}
}
