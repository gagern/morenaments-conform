package net.von_gagern.martin.morenaments.conformal;

import javax.media.opengl.GL;

abstract class ShaderInterface {

    protected final GL gl;

    public ShaderInterface(final GL gl) {
        this.gl = gl;
    }

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
