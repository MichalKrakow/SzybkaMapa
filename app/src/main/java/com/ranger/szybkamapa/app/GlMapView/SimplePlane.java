package com.ranger.szybkamapa.app.GlMapView;

import android.util.Log;

/**
 * SimplePlane is a setup class for Mesh that creates a plane mesh.
 * 
 * @author Per-Erik Bergman (per-erik.bergman@jayway.com)
 * 
 */
public class SimplePlane extends Mesh {
	/**
	 * Create a plane with a default with and height of 1 unit.
	 */
	public SimplePlane() {
		this(1, 1);
	}

	/**
	 * Create a plane.
	 * 
	 * @param width
	 *            the width of the plane.
	 * @param height
	 *            the height of the plane.
	 */
	public SimplePlane(float width, float height) {
		// Mapping coordinates for the vertices
		float textureCoordinates[] = {
                0.0f, 0.0f, //
				1.0f, 0.0f, //
				0.0f, 1.0f, //
				1.0f, 1.0f, //
		};

		//short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };
        short[] indices = new short[] { 0, 2, 1, 1, 2, 3 };

        /*
		float[] vertices = new float[] { -200.5f, -200.5f, 0.0f,
                                          200.5f, -200.5f, 0.0f,
                                         -200.5f,  200.5f, 0.0f,
                                          200.5f,  200.5f, 0.0f };
        */
        float[] vertices = new float[] {

                0f, 0f, 0.0f,
                width, 0f, 0.0f,
                0f,  height, 0.0f,
                width,  height, 0.0f };



		setIndices(indices);
		setVertices(vertices);
		setTextureCoordinates(textureCoordinates);
	}
    public void setSize(int width,int height)
    {
        float[] vertices = new float[] {

                0f, 0f, 0.0f,
                width, 0f, 0.0f,
                0f,  height, 0.0f,
                width,  height, 0.0f };
        setVertices(vertices);
    }
}
