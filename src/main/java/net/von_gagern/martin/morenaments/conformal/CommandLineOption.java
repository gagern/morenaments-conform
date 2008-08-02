package net.von_gagern.martin.morenaments.conformal;

import net.von_gagern.martin.getopt.OptArg;
import net.von_gagern.martin.getopt.OptKey;
import static net.von_gagern.martin.getopt.OptArg.NONE;
import static net.von_gagern.martin.getopt.OptArg.OPTIONAL;
import static net.von_gagern.martin.getopt.OptArg.REQUIRED;

enum CommandLineOption implements OptKey {

    hypOut(REQUIRED, "file to which the hyperbolic triangulation is written"),
    eucOut(REQUIRED, "file to which the euclidean triangulation is written"),
    aes(NONE, "use adaptive edge subdivision instead of triangle subdivision"),
    tc(REQUIRED, "triangle count; minimum number of triangles in mesh"),
    help(NONE, "display this help information");

    private OptArg argMode;

    private String description;

    private CommandLineOption(OptArg argMode, String description) {
        this.argMode = argMode;
        this.description = description;
    }

    public String key() {
        return name();
    }

    public OptArg argMode() {
        return argMode;
    }

    public String description() {
        return description;
    }

}
