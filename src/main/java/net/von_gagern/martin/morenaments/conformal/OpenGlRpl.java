package net.von_gagern.martin.morenaments.conformal;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
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
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

import org.apache.log4j.Logger;

import de.tum.in.gagern.hornamente.HypTrafo;
import de.tum.in.gagern.hornamente.Vec2C;
import net.von_gagern.martin.morenaments.conformal.groups.Group;

import static javax.media.opengl.GL.*;

class OpenGlRpl implements GLEventListener,
                           MouseWheelListener, MouseInputListener
{

    private final Logger logger = Logger.getLogger(OpenGlRpl.class);
    private boolean debug = true;

    private BufferedImage tile;
    private Group grp;
    private final Component cmp;

    private static Map<String, String> sources = new HashMap<String, String>();
    private Map<String, Integer> uniLocCache = new HashMap<String, Integer>();
    private int texName;
    private int shaderProgram;
    private static final float grayLevel = 12.f/15.f;
    private float[] background = { grayLevel, grayLevel, grayLevel, 1.0f };
    private int maxTextureSize;
    private static final HypTrafo flipReal =
        new HypTrafo(new Vec2C(0., 0., 0., 1.), true);
    private int intendedZoomStep, currentZoomStep;
    private double pixelSize = 1.;
    private Timer cancelDraftTimer;
    private boolean useDraft = false, intendedDraft = false, currentDraft;
    private Point panStartPos;
    private boolean updateViewport;
    private Point2D.Double lastCenter = new Point2D.Double();
    private Point2D.Double center = new Point2D.Double();
    private HypTrafo currentInitialTrafo = new HypTrafo();
    private HypTrafo intendedInitialTrafo = null, beginDragPoint = null;
    private Double rotationStartAngle = null;

    public OpenGlRpl(BufferedImage img, Group g) {
        this.tile = img;
        this.grp = g;
        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
        cmp = canvas;
        cancelDraftTimer = new Timer(200, new ActionListener() {
                public void actionPerformed(ActionEvent evnt) {
                    intendedDraft = false;
                    cmp.repaint();
                }
            });
        cancelDraftTimer.setRepeats(false);
    }

    public Component getComponent() {
        return cmp;
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
        String vendor = gl.glGetString(GL_VENDOR);
        String renderer = gl.glGetString(GL_RENDERER);
        String version = gl.glGetString(GL_VERSION);
        String slVersion = gl.glGetString(GL_SHADING_LANGUAGE_VERSION);
        String extensions = gl.glGetString(GL_EXTENSIONS);
        logger.info("OpenGL vendor: " + vendor);
        logger.info("OpenGL renderer: " + renderer);
        logger.info("OpenGL version: " + version);
        logger.info("GLSL version: " + slVersion);
        int[] intBuf = new int[1];
        gl.glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, intBuf, 0);
        if (intBuf[0] < 1)
            throw new UnsupportedOperationException
                ("At least one texture unit required");
        gl.glGetIntegerv(GL_MAX_TEXTURE_SIZE, intBuf, 0);
        maxTextureSize = intBuf[0];
        logger.info("max texture size: " + maxTextureSize);
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
        String[] fSrc = { "fsRpl.gls" };
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
            logger.info("Fragment shader log:");
            logger.info(log);
	}
	
	// check compilation status
	gl.glGetShaderiv(shader, GL_COMPILE_STATUS, intBuf, 0);
	if (intBuf[0] != GL_TRUE)
	    throw new RuntimeException("Error compiling GLS");

	// link program
	shaderProgram = gl.glCreateProgram();
	gl.glAttachShader(shaderProgram, shader);

        uniLocCache.clear();
	gl.glLinkProgram(shaderProgram);
	// get log message
	gl.glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, intBuf, 0);
	logBytes = new byte[intBuf[0] + 1];
	gl.glGetProgramInfoLog(shaderProgram, logBytes.length,
			       intBuf, 0, logBytes, 0);
	if (intBuf[0] > 0 && logBytes[intBuf[0]] == 0) --intBuf[0];
	log = new String(logBytes, 0, intBuf[0]);
	if (!"".equals(log)) {
            logger.info("Shader program log:");
            logger.info(log);
	}


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
            logger.info("Shader program log:");
            logger.info(log);
	}

	gl.glUseProgram(shaderProgram);
	gl.glUniform1i(uniLoc(gl, "texSampler"), 0);
        gl.glUniform4fv(uniLoc(gl, "bgColor"), 1, background, 0);
        draftQuality(gl, currentDraft);
        initialTrafo(gl, currentInitialTrafo);
        initGroup(gl);
    }

    private static final double detTolerance = 1e-6;

    private void vec4ForHypTrafo(HypTrafo tr, float[] v, int i) {
        double det = tr.vec.y.absSq() - tr.vec.x.absSq(), factor;
        if (Math.abs(det - 1.) > detTolerance)
            factor = Math.sqrt(1./det);
        else
            factor = 1.0;
        v[i++] = (float)(tr.vec.x.r*factor);
        v[i++] = (float)(tr.vec.x.i*factor);
        v[i++] = (float)(tr.vec.y.r*factor);
        v[i++] = (float)(tr.vec.y.i*factor);
    }

    private void initialTrafo(GL gl, HypTrafo initialTrafo) {
        currentInitialTrafo = initialTrafo;
        float[] it = new float[4];
        vec4ForHypTrafo(initialTrafo, it, 0);
        gl.glUniform4fv(uniLoc(gl, "initialTrafo"), 1, it, 0);
    }

    private void initGroup(GL gl) {
        HypTrafo[] gens = grp.getGenerators();
        HypTrafo[] incs = grp.getInsidenessChecks();
        final int n = gens.length;
        assert incs.length == n;
        float[] ic = new float[n<<2], gm = new float[n<<2], gp = new float[n];
        for (int i = 0; i < n; ++i) {
            HypTrafo inc = incs[i], gen = gens[i];
            gen = gen.getInverse();
            if (inc.doConj)
                inc = flipReal.clone().concatenate(inc);
            assert !inc.doConj;
            vec4ForHypTrafo(inc, ic, i<<2);
            vec4ForHypTrafo(gen, gm, i<<2);
            gp[i] = gen.doConj ? -1.f : 1.f;
        }
	gl.glUniform1i(uniLoc(gl, "numGenerators"), n);
        gl.glUniform4fv(uniLoc(gl, "insidenessChecks"), n, ic, 0);
	gl.glUniform4fv(uniLoc(gl, "genMatrix"), n, gm, 0);        
        gl.glUniform1fv(uniLoc(gl, "genParity"), n, gp, 0);
    }

    public void display(GLAutoDrawable drawable) {
	GL gl = drawable.getGL();
        if (updateViewport || intendedZoomStep != currentZoomStep)
            setupViewport(gl, cmp.getWidth(), cmp.getHeight());
        if (currentDraft != intendedDraft)
            draftQuality(gl, intendedDraft);
        if (intendedInitialTrafo != null) {
            initialTrafo(gl, intendedInitialTrafo);
            intendedInitialTrafo = null;
        }
	gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	gl.glEnable(GL_TEXTURE_2D);

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
        useDraft = width*height > 512*512;
        setupViewport(gl, width, height);
        triggerDraft();
    }

    private double clamp(double min, double val, double max) {
        return Math.min(max, Math.max(min, val));
    }

    private void setupViewport(GL gl, int width, int height) {
	gl.glMatrixMode(GL_PROJECTION);
	gl.glLoadIdentity();
        double w, h;
        if (width < height) {
            w = 1;
            h = height/(double)width;
            pixelSize = 2./width;
        }
        else {
            w = width/(double)height;
            h = 1;
            pixelSize = 2./height;
        }

        currentZoomStep = intendedZoomStep;
        double zoom = Math.pow(0.9, currentZoomStep);
        pixelSize *= zoom;
        double wz = w*zoom, hz = h*zoom;
        if (wz >= 1.) center.x = 0.;
        else center.x = clamp(wz - 1., center.x, 1. - wz);
        if (hz >= 1.) center.y = 0.;
        else center.y = clamp(hz - 1., center.y, 1. - hz);
        double cx = center.x, cy = center.y;
	gl.glOrtho(cx-wz, cx+wz, cy+hz, cy-hz, 0., 1.);

        float[] aaOffsets = new float[9*3];
        float basicOffset = (float)(pixelSize/3.);
        int i = 0;
        for (int dx = 1; dx <= 3; ++dx) {
            for (int dy = 1; dy <= 3; ++dy) {
                aaOffsets[i++] = (dx%3 - 1)*basicOffset;
                aaOffsets[i++] = (dy%3 - 1)*basicOffset;
                aaOffsets[i++] = 1.f;
            }
        }
        gl.glUniform3fv(uniLoc(gl, "aaOffsets"), 9, aaOffsets, 0);
    }

    public void displayChanged(GLAutoDrawable drawable,
			       boolean modeChanged,
			       boolean deviceChanged) {
	GL gl = drawable.getGL();
    }

    private int uniLoc(GL gl, String name) {
        Integer loc = uniLocCache.get(name);
        if (loc == null) {
            loc = Integer.valueOf(gl.glGetUniformLocation(shaderProgram, name));
            uniLocCache.put(name, loc);
        }
        return loc.intValue();
    }

    private void triggerDraft() {
        cmp.repaint();
        intendedDraft = useDraft;
        cancelDraftTimer.restart();
    }

    private void draftQuality(GL gl, boolean draft) {
	gl.glUniform1i(uniLoc(gl, "numAaOffsets"), draft ? 1 : 9);
	gl.glUniform1i(uniLoc(gl, "maxPathLength"), draft ? 12 : 50);
        currentDraft = draft;
    }

    private Point2D.Double windowToDisc(Point in) {
        double x = (in.x + 0.5 - cmp.getWidth ()/2.)*pixelSize + center.x;
        double y = (in.y + 0.5 - cmp.getHeight()/2.)*pixelSize + center.y;
        return new Point2D.Double(x, y);
    }

    private HypTrafo translation(Point2D d) {
        HypTrafo tr = new HypTrafo(new Vec2C(d.getX(), d.getY(), 1, 0), false);
        tr.normalize();
        return tr;
    }

    public void mouseEntered(MouseEvent evnt) {
    }

    public void mouseExited(MouseEvent evnt) {
    }

    private static final double inRadius2 = 0.8*0.8;

    public void mousePressed(MouseEvent evnt) {
        if (panStartPos != null || beginDragPoint != null)
            return;
        if (evnt.getButton() == MouseEvent.BUTTON3 ||
            (evnt.getButton() == MouseEvent.BUTTON1 &&
             (evnt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0)) {
            Point2D.Double pos = windowToDisc(evnt.getPoint());
            double r2 = pos.x*pos.x + pos.y*pos.y;
            if (r2 < inRadius2) {
                beginDragPoint = translation(pos);
                beginDragPoint.preConcatenate(currentInitialTrafo);
            }
            else {
                beginDragPoint = currentInitialTrafo;
                rotationStartAngle = Math.atan2(pos.y, pos.x);
            }
        }
        else if (evnt.getButton() == MouseEvent.BUTTON1) {
            panStartPos = evnt.getPoint();
            lastCenter.setLocation(center);
        }
    }

    public void mouseReleased(MouseEvent evnt) {
        panStartPos = null;
        beginDragPoint = null;
        rotationStartAngle = null;
    }

    public void mouseClicked(MouseEvent evnt) {
    }

    public void mouseMoved(MouseEvent evnt) {
    }

    public void mouseDragged(MouseEvent evnt) {
        if (panStartPos != null) {
            double dx = (evnt.getX() - panStartPos.x)*pixelSize;
            double dy = (evnt.getY() - panStartPos.y)*pixelSize;
            center.setLocation(lastCenter.x - dx, lastCenter.y - dy);
            updateViewport = true;
            triggerDraft();
        }
        if (beginDragPoint != null) {
            Point2D.Double pos = windowToDisc(evnt.getPoint());
            if (rotationStartAngle == null) {
                if (pos.x*pos.x + pos.y*pos.y > .999) return;
                intendedInitialTrafo = translation(pos);
                intendedInitialTrafo.invert();
            }
            else {
                double a = rotationStartAngle - Math.atan2(pos.y, pos.x);
                intendedInitialTrafo = HypTrafo.getRotation(a);
            }
            intendedInitialTrafo.preConcatenate(beginDragPoint);
            triggerDraft();
        }
    }

    public void mouseWheelMoved(MouseWheelEvent evnt) {
        intendedZoomStep = intendedZoomStep + evnt.getWheelRotation();
        if (intendedZoomStep < 0) intendedZoomStep = 0;
        if (intendedZoomStep > 100) intendedZoomStep = 100;
        if (intendedZoomStep != currentZoomStep) triggerDraft();
    }

}
