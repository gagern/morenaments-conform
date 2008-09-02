package net.von_gagern.martin.morenaments.conformal;

import javax.swing.JFrame;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import net.von_gagern.martin.getopt.OptException;
import net.von_gagern.martin.getopt.OptPair;
import net.von_gagern.martin.getopt.OptParser;

public class CommandLine {

    public static void main(String[] args) throws Exception {
        configureLog4j();
        CommandLine cl = new CommandLine(args);
        cl.run();
    }

    public static void configureLog4j() {
        DOMConfigurator.configure(CommandLine.class.getResource("log4j.xml"));
    }

    private Integer size;

    public CommandLine(String[] args) throws OptException {
        OptParser<CommandLineOption> parser =
            OptParser.getInstance(CommandLineOption.values());
        for (OptPair<CommandLineOption> pair: parser.parse(args)) {
            CommandLineOption key = pair.getKey();
            String value = pair.getValue();
            if (key == null) {
                if (true)
                    throw new OptException("No arguments allowed", pair);
                continue;
            }
            switch (key) {
            case size:
                size(pair);
                break;
            case debug:
                debug(pair);
                break;
            case help:
                System.out.println("Usage: java " +
                                   CommandLine.class.getName() +
                                   " [options...]");
                System.out.println();
                System.out.println("Options:");
                parser.printHelp(System.out);
                break;
            default:
                throw new OptException("Unsupported command line option", pair);
            }
        }
    }

    private void size(OptPair<CommandLineOption> pair) throws OptException {
        this.size = intValue(pair, 2, (1 << 16) - 1);
    }

    private int intValue(OptPair<CommandLineOption> pair, int min, int max)
        throws OptException
    {
        try {
            int i = Integer.parseInt(pair.getValue());
            if (i < min)
                throw new OptException("Argument less than " + min, pair);
            if (i > max)
                throw new OptException("Argument greater than " + max, pair);
            return i;
        }
        catch (NumberFormatException e) {
            throw new OptException("Integer argument expected", pair);
        }
    }

    private void debug(OptPair<CommandLineOption> pair) throws OptException {
        String value = pair.getValue();
        String[] parts;
        if (value == null)
            parts = new String[0];
        else
            parts = value.split(":");
        if (parts.length > 2)
            throw new OptException("Illegal debug request", pair);
        String name = "", levelStr = "";
        if (parts.length > 1) levelStr = parts[1];
        if (parts.length > 0) name = parts[0];
        Logger logger;
        Level level;
        if (name.equals("")) logger = Logger.getRootLogger();
        else logger = Logger.getLogger(name);
        if (levelStr.equals("")) level = Level.DEBUG;
        else level = Level.toLevel(levelStr, null);
        if (level == null) throw new OptException("Illegal level", levelStr);
        logger.setLevel(level);
    }

    public void run() {
        JFrame frm = new JFrame("morenaments conformal");
        GUI gui = new GUI(this, frm);
        frm.getContentPane().add(gui);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.pack();
        frm.setLocationByPlatform(true);
        frm.setVisible(true);
    }

    public int getSize(int defaultValue) {
        if (size == null) return defaultValue;
        return size;
    }

}
