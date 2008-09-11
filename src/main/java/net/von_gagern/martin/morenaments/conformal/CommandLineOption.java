package net.von_gagern.martin.morenaments.conformal;

import net.von_gagern.martin.getopt.OptArg;
import net.von_gagern.martin.getopt.OptKey;
import static net.von_gagern.martin.getopt.OptArg.NONE;
import static net.von_gagern.martin.getopt.OptArg.OPTIONAL;
import static net.von_gagern.martin.getopt.OptArg.REQUIRED;

enum CommandLineOption implements OptKey {

    size(REQUIRED, "Specify size for rendered images"),
    group(REQUIRED, "Group for automatic renderings"),
    tile(NONE, "Automatically render tiling of specified images"),
    debug(OPTIONAL, "enable debug output for given class/package"),
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
