/*
 * This file is part of Spout.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * Spout is licensed under the SpoutDev License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.engine.renderer;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spout.api.render.RenderMaterial;
import org.spout.engine.renderer.vertexbuffer.GLFloatBuffer;

public class GL20BatchVertexRenderer extends BatchVertexRenderer {
	final int SIZE_FLOAT = 4;
	int vbos = -1;

	TIntObjectHashMap<GLFloatBuffer > vertexBuffers = new TIntObjectHashMap<GLFloatBuffer>();


	/**
	 * Batch Renderer using OpenGL 2.0 mode.
	 * @param renderMode Mode to render in
	 */
	public GL20BatchVertexRenderer(int renderMode) {
		super(renderMode);

		//vertexBuffers.put(0, new GLFloatBuffer("vPosition", 4, 0));
	}

	@Override
	protected void doFlush() {
		for(Entry<Integer, Buffer> entry : buffers.entrySet()){
			int layout = entry.getKey();
			Buffer buffer = entry.getValue();

			if(buffer instanceof FloatBuffer){
				GLFloatBuffer vertexBuffer = vertexBuffers.get(layout);

				if(vertexBuffer == null) {
					vertexBuffer = new GLFloatBuffer("uselessname", buffer.limit() / numVertices, layout);
					vertexBuffers.put(layout, vertexBuffer);
				}

				vertexBuffer.flush((FloatBuffer)buffer);
			}else{
				throw new IllegalStateException("Buffer different of FloatBuffer not yet supported");	
			}
		}
	}

	/**
	 * Draws this batch
	 */
	@Override
	public void doRender(RenderMaterial material, int startVert, int endVert) {
		material.assign();

		for(GLFloatBuffer glBuffer : vertexBuffers.valueCollection()){
			glBuffer.bind();
			GL20.glEnableVertexAttribArray(glBuffer.getLayout());
			//GL20.glVertexAttribPointer(vb.getLayout(), vb.getElements(), GL11.GL_FLOAT, false, 0, 0);
			//material.getShader().enableAttribute(vb.getName(), vb.getElements(), GL11.GL_FLOAT, 0, 0, vb.getLayout());
		}

		GL11.glDrawArrays(renderMode, startVert, endVert);

		for(GLFloatBuffer glBuffer : vertexBuffers.valueCollection()){			
			GL20.glDisableVertexAttribArray(glBuffer.getLayout());		
		}

	}

	private void dispose() {
		for(GLFloatBuffer glBuffer : vertexBuffers.valueCollection()){
			glBuffer.dispose();
		}
	}

	public void finalize() {
		dispose();
	}
}
