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

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

class OpenGlShaderInterface extends ShaderInterface {

    public OpenGlShaderInterface(final GL gl) {
        super(gl);
    }

    private int getShaderParameteri(int shader, int pname) {
        int[] buf = new int[1];
        gl.glGetShaderiv(shader, pname, buf, 0);
        return buf[0];
    }

    private int getProgramParameteri(int program, int pname) {
        int[] buf = new int[1];
        gl.glGetProgramiv(program, pname, buf, 0);
        return buf[0];
    }

    @Override public int createFragmentShader() {
        return gl.glCreateShader(GL_FRAGMENT_SHADER);
    }

    @Override public void shaderSource(int shader, int count, String[] string) {
        gl.glShaderSource(shader, count, string, null);
    }

    @Override public void compileShader(int shader) {
        gl.glCompileShader(shader);
    }

    @Override public int getShaderInfoLogLength(int shader) {
        return getShaderParameteri(shader, GL_INFO_LOG_LENGTH);
    }

    @Override public void getShaderInfoLog(int shader, int bufSize,
                                           int[] length, int length_offset,
                                           byte[] infoLog, int infoLog_offset) {
        gl.glGetShaderInfoLog(shader, bufSize, length, length_offset,
                           infoLog, infoLog_offset);
    }

    @Override public int getShaderCompileStatus(int shader) {
        return getShaderParameteri(shader, GL_COMPILE_STATUS);
    }

    @Override public int createProgram() {
        return gl.glCreateProgram();
    }

    @Override public void attachShader(int program, int shader) {
        gl.glAttachShader(program, shader);
    }

    @Override public void linkProgram(int program) {
	gl.glLinkProgram(program);
    }

    @Override public int getProgramInfoLogLength(int program) {
        return getProgramParameteri(program, GL_INFO_LOG_LENGTH);
    }

    @Override public void getProgramInfoLog(int program, int bufSize,
                                            int[] length, int length_offset,
                                            byte[] infoLog, int infoLog_offset)
    {
        gl.glGetProgramInfoLog(program, bufSize, length, length_offset,
                               infoLog, infoLog_offset);
    }

    @Override public int getLinkStatus(int program) {
        return getProgramParameteri(program, GL_LINK_STATUS);
    }

    @Override public void validateProgram(int program) {
	gl.glValidateProgram(program);
    }

    @Override public int getValidateStatus(int program) {
        return getProgramParameteri(program, GL_VALIDATE_STATUS);
    }

    @Override public void useProgram(int program) {
        gl.glUseProgram(program);
    }

    @Override public void uniform1i(int location, int i1) {
	gl.glUniform1i(location, i1);
    }

    @Override public void uniform1fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform1fv(location, count, value, value_offset);
    }

    @Override public void uniform3fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform3fv(location, count, value, value_offset);
    }

    @Override public void uniform4fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform4fv(location, count, value, value_offset);
    }

    @Override public int getUniformLocation(int program, String name) {
        return gl.glGetUniformLocation(program, name);
    }

}
