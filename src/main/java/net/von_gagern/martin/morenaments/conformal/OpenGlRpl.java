package net.von_gagern.martin.morenaments.conformal;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.*;
import javax.swing.*;
import javax.media.opengl.*;
import org.apache.log4j.Logger;

import static javax.media.opengl.GL.*;

class OpenGlRpl implements GLEventListener {

    private final Logger logger = Logger.getLogger(OpenGlRpl.class);
    private boolean debug = true;

    private static final int checkImageWidth = 64;
    private static final int checkImageHeight = 64;
    private Buffer checkImage;
    private int texName;
    private int shaderProgram;
    private static final float grayLevel = 12.f/15.f;
    private float[] background = { grayLevel, grayLevel, grayLevel, 1.0f };
    private int maxTextureSize;
    private BufferedImage tile;

    public OpenGlRpl(BufferedImage img) {
        this.tile = img;
	// makeCheckImage();
    }

    private void makeCheckImage() {
	ByteBuffer img;
	img = ByteBuffer.allocate(checkImageWidth*checkImageHeight*4);
	for (int i = 0; i < checkImageHeight; i++) {
	    for (int j = 0; j < checkImageWidth; j++) {
		byte c = ((((i&0x8)==0)^((j&0x8))==0)) ? (byte)255 : (byte)0;
		img.put(c).put(c).put(c).put((byte)255);
	    }
	}
	img.rewind();
	checkImage = img;
    }

    public void init(GLAutoDrawable drawable) {
        if (debug)
            drawable.setGL(new DebugGL(drawable.getGL()));
        GL gl = drawable.getGL();
        checkParams(gl);
        initDefaults(gl);
        initTexture(gl);
        initShader(gl);
    }

    private void checkParams(GL gl) {
        int[] intBuf = new int[1];
        gl.glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, intBuf, 0);
        if (intBuf[0] < 1)
            throw new UnsupportedOperationException
                ("At least one texture unit required");
        gl.glGetIntegerv(GL_MAX_TEXTURE_SIZE, intBuf, 0);
        maxTextureSize = intBuf[0];
        logger.debug("max texture size: " + maxTextureSize);
    }

    private void initDefaults(GL gl) {
        gl.glClearColor(background[0], background[1], background[2],
                        background[3]);
    }

    private int roundUpToPow2(int in) {
        if ((in & (in - 1)) == 0) return in;
        for (int i = 1; i > 0; i <<= 1)
            if (i >= in) return i;
        throw new IllegalArgumentException("size too large");
    }

    private void initTexture(GL gl) {
        int iw = tile.getWidth(), ih = tile.getHeight();
        int ow = Math.min(maxTextureSize, roundUpToPow2(iw));
        int oh = Math.min(maxTextureSize, roundUpToPow2(ih));
        boolean premult = false;
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, true, premult,
                                                Transparency.TRANSLUCENT,
                                                DataBuffer.TYPE_BYTE);
        int[] bandOffsets = { 0, 1, 2, 3 };
        SampleModel sm = new PixelInterleavedSampleModel
            (DataBuffer.TYPE_BYTE, ow, oh, 4, 4*ow, bandOffsets);
        byte[] array = new byte[ow*oh*4];
        DataBufferByte db = new DataBufferByte(array, array.length);
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        BufferedImage img = new BufferedImage(cm, raster, premult, null);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(tile, 0, 0, ow, oh, null);
        g2d.dispose();
        Buffer data = ByteBuffer.wrap(array);

	int[] intBuf = new int[1];
	gl.glGenTextures(1, intBuf, 0);
	texName = intBuf[0];
	gl.glBindTexture(GL_TEXTURE_2D, texName);

	gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, 
			   GL_NEAREST);
	gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, 
			   GL_NEAREST);
        // gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, ow, oh, 
			0, GL_RGBA, GL_UNSIGNED_BYTE, data);
    }

    private void initShader(GL gl) {
	String fSrc =
          "const vec4 bgColor = vec4(12./15., 12./15., 12./15., 1.);\n" +
          "uniform sampler2D texSampler;\n" +
          "void main() {\n" +
          " vec2 v = vec2(gl_TexCoord[0]);\n" +
          " if(v.x*v.x + v.y*v.y > 1.) {\n" +
          "  gl_FragColor = bgColor;\n" +
          " } else {\n" +
          "  gl_FragColor = texture2D(texSampler, v);\n" +
          " }" +
          "}";

	int[] intBuf = new int[1];
	int shader = gl.glCreateShader(GL_FRAGMENT_SHADER);
	gl.glShaderSource(shader, 1, new String[]{fSrc}, null);
	gl.glCompileShader(shader);

	// get log message
	gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, intBuf, 0);
	byte[] logBytes = new byte[intBuf[0] + 1];
	gl.glGetShaderInfoLog(shader, logBytes.length,
			      intBuf, 0, logBytes, 0);
	if (intBuf[0] > 0 && logBytes[intBuf[0]] == 0) --intBuf[0];
	String log = new String(logBytes, 0, intBuf[0]);
	if (!"".equals(log)) {
	    System.out.println("=== begin shader info log ===");
	    System.out.println(log);
	    System.out.println("=== end shader info log ===");
	}
	
	// check compilation status
	gl.glGetShaderiv(shader, GL_COMPILE_STATUS, intBuf, 0);
	if (intBuf[0] != GL_TRUE)
	    throw new RuntimeException("Error compiling GLS");

	// link program
	shaderProgram = gl.glCreateProgram();
	gl.glAttachShader(shaderProgram, shader);

	gl.glLinkProgram(shaderProgram);
	gl.glGetProgramiv(shaderProgram, GL_LINK_STATUS, intBuf, 0);
	if (intBuf[0] != GL_TRUE)
	    throw new RuntimeException("Error linking GLS");

	gl.glValidateProgram(shaderProgram);
	gl.glGetProgramiv(shaderProgram, GL_VALIDATE_STATUS, intBuf, 0);
	if (intBuf[0] != GL_TRUE)
	    throw new RuntimeException("Error validating GLS");

	// get log message
	gl.glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, intBuf, 0);
	logBytes = new byte[intBuf[0] + 1];
	gl.glGetProgramInfoLog(shaderProgram, logBytes.length,
			       intBuf, 0, logBytes, 0);
	if (intBuf[0] > 0 && logBytes[intBuf[0]] == 0) --intBuf[0];
	log = new String(logBytes, 0, intBuf[0]);
	if (!"".equals(log)) {
	    System.out.println("=== begin shader program info log ===");
	    System.out.println(log);
	    System.out.println("=== end shader program info log ===");
	}

	gl.glUseProgram(shaderProgram);
	int texLoc = gl.glGetUniformLocation(shaderProgram, "texSampler");
	gl.glUniform1i(texLoc, 0);
    }

    public void display(GLAutoDrawable drawable) {
	GL gl = drawable.getGL();
	gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	gl.glEnable(GL_TEXTURE_2D);

	gl.glEnable(GL_MULTISAMPLE);
	//gl.glSampleCoverage(0.f, false);
	int[] intBuf = new int[1];
	gl.glGetIntegerv(GL_SAMPLE_BUFFERS, intBuf, 0);
	//System.out.println("display: " + intBuf[0]);

	gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
	gl.glBindTexture(GL_TEXTURE_2D, texName);
	gl.glBegin(GL_QUADS);
	gl.glTexCoord2f(-1.0f, -1.0f); gl.glVertex3f(-1.0f, -1.0f, 0.0f);
	gl.glTexCoord2f(-1.0f, 1.0f); gl.glVertex3f(-1.0f, 1.0f, 0.0f);
	gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(1.0f, 1.0f, 0.0f);
	gl.glTexCoord2f(1.0f, -1.0f); gl.glVertex3f(1.0f, -1.0f, 0.0f);
	gl.glEnd();
	gl.glFlush();
	gl.glDisable(GL_TEXTURE_2D);
    }

    public void reshape(GLAutoDrawable drawable,
			int x, int y, int width, int height) {
	GL gl = drawable.getGL();
	gl.glMatrixMode(GL_PROJECTION);
	gl.glLoadIdentity();
        double w, h;
        if (width < height) {
            w = 1;
            h = height/(double)width;
        }
        else {
            w = width/(double)height;
            h = 1;
        }
	gl.glOrtho(-w, w, -h, h, 0., 1.);
    }

    public void displayChanged(GLAutoDrawable drawable,
			       boolean modeChanged,
			       boolean deviceChanged) {
	GL gl = drawable.getGL();
    }

}
