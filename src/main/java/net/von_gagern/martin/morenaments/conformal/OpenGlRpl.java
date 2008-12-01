package net.von_gagern.martin.morenaments.conformal;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.log4j.Logger;

import static javax.media.opengl.GL.*;

class OpenGlRpl implements GLEventListener {

    private final Logger logger = Logger.getLogger(OpenGlRpl.class);
    private boolean debug = true;
    private static Map<String, String> sources = new HashMap<String, String>();

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

    private static String loadSource(String name) {
        InputStream stream = OpenGlRpl.class.getResourceAsStream(name);
        if (stream == null)
            throw new MissingResourceException("Missing shader source code",
                                               OpenGlRpl.class.getName(), name);
        try {
            int sizeEstimate = Math.max(500, stream.available() + 8);
            CharsetDecoder dec = Charset.forName("US-ASCII").newDecoder();
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            dec.onMalformedInput(CodingErrorAction.REPORT);
            Reader reader = new InputStreamReader(stream, dec);
            reader = new BufferedReader(reader, sizeEstimate);
            StringBuilder buf = new StringBuilder(sizeEstimate);
            int c;
            while ((c = reader.read()) != -1)
                buf.append((char)c);
            reader.close();
            return buf.toString();
        }
        catch (IOException e) {
            throw new RuntimeException("Error loading shader source code", e);
        }
    }

    private static String getSource(String name) {
        String src = sources.get(name);
        if (src == null) {
            src = loadSource(name);
            sources.put(name, src);
        }
        return src;
    }

    private void initShader(GL gl) {
        String[] fSrc = { "vsRpl.gsl" };
        for (int i = 0; i < fSrc.length; ++i)
            fSrc[i] = getSource(fSrc[i]);
	int[] intBuf = new int[1];
	int shader = gl.glCreateShader(GL_FRAGMENT_SHADER);
	gl.glShaderSource(shader, fSrc.length, fSrc, null);
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
