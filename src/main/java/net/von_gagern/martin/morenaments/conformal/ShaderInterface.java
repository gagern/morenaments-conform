/*
 * morenaments conformal - Hyperbolization of ornaments
 *                         via discrete conformal maps
 * Copyright (C) 2009-2010 Martin von Gagern
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.von_gagern.martin.morenaments.conformal;

import com.jogamp.opengl.GL;

abstract class ShaderInterface {

    public abstract int createFragmentShader();

    public abstract void shaderSource(int shader, int count, String[] string);

    public abstract void compileShader(int shader);

    public abstract int getShaderInfoLogLength(int shader);

    public abstract void getShaderInfoLog(int shader, int bufSize,
                                          int[] length, int length_offset,
                                          byte[] infoLog, int infoLog_offset);

    public abstract int getShaderCompileStatus(int shader);

    public abstract int createProgram();

    public abstract void attachShader(int program, int shader);

    public abstract void linkProgram(int program);

    public abstract int getProgramInfoLogLength(int program);

    public abstract void getProgramInfoLog(int program, int bufSize,
                                           int[] length, int length_offset,
                                           byte[] infoLog, int infoLog_offset);

    public abstract int getLinkStatus(int program);

    public abstract void validateProgram(int program);

    public abstract int getValidateStatus(int program);

    public abstract void useProgram(int program);

    public abstract void uniform1i(int location, int i1);

    public abstract void uniform1fv(int location, int count,
                                    float[] value, int value_offset);

    public abstract void uniform3fv(int location, int count,
                                    float[] value, int value_offset);

    public abstract void uniform4fv(int location, int count,
                                    float[] value, int value_offset);

    public abstract int getUniformLocation(int program, String name);

}
