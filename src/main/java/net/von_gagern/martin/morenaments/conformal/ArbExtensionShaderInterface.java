package net.von_gagern.martin.morenaments.conformal;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

class ArbExtensionShaderInterface extends ShaderInterface {

    public ArbExtensionShaderInterface(final GL gl) {
        super(gl);
    }

    private int getParameteri(int object, int pname) {
        int[] buf = new int[1];
        gl.glGetObjectParameterivARB(object, pname, buf, 0);
        return buf[0];
    }

    @Override public int createFragmentShader() {
        return gl.glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);
    }

    @Override public void shaderSource(int shader, int count, String[] string) {
        gl.glShaderSource(shader, count, string, null);
    }

    @Override public void compileShader(int shader) {
        gl.glCompileShaderARB(shader);
    }

    @Override public int getShaderInfoLogLength(int shader) {
        return getParameteri(shader, GL_OBJECT_INFO_LOG_LENGTH_ARB);
    }

    @Override public void getShaderInfoLog(int shader, int bufSize,
                                           int[] length, int length_offset,
                                           byte[] infoLog, int infoLog_offset) {
        gl.glGetInfoLogARB(shader, bufSize, length, length_offset,
                           infoLog, infoLog_offset);
    }

    @Override public int getShaderCompileStatus(int shader) {
        return getParameteri(shader, GL_OBJECT_COMPILE_STATUS_ARB);
    }

    @Override public int createProgram() {
        return gl.glCreateProgramObjectARB();
    }

    @Override public void attachShader(int program, int shader) {
        gl.glAttachObjectARB(program, shader);
    }

    @Override public void linkProgram(int program) {
	gl.glLinkProgramARB(program);
    }

    @Override public int getProgramInfoLogLength(int program) {
        return getParameteri(program, GL_OBJECT_INFO_LOG_LENGTH_ARB);
    }

    @Override public void getProgramInfoLog(int program, int bufSize,
                                            int[] length, int length_offset,
                                            byte[] infoLog, int infoLog_offset)
    {
        gl.glGetInfoLogARB(program, bufSize, length, length_offset,
                           infoLog, infoLog_offset);
    }

    @Override public int getLinkStatus(int program) {
        return getParameteri(program, GL_OBJECT_LINK_STATUS_ARB);
    }

    @Override public void validateProgram(int program) {
	gl.glValidateProgramARB(program);
    }

    @Override public int getValidateStatus(int program) {
        return getParameteri(program, GL_OBJECT_VALIDATE_STATUS_ARB);
    }

    @Override public void useProgram(int program) {
        gl.glUseProgramObjectARB(program);
    }

    @Override public void uniform1i(int location, int i1) {
	gl.glUniform1iARB(location, i1);
    }

    @Override public void uniform1fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform1fvARB(location, count, value, value_offset);
    }

    @Override public void uniform3fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform3fvARB(location, count, value, value_offset);
    }

    @Override public void uniform4fv(int location, int count,
                                     float[] value, int value_offset) {
	gl.glUniform4fvARB(location, count, value, value_offset);
    }

    @Override public int getUniformLocation(int program, String name) {
        return gl.glGetUniformLocationARB(program, name);
    }

}