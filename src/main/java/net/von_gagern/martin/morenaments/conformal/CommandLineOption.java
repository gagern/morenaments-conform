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

import net.von_gagern.martin.getopt.OptArg;
import net.von_gagern.martin.getopt.OptKey;
import static net.von_gagern.martin.getopt.OptArg.NONE;
import static net.von_gagern.martin.getopt.OptArg.OPTIONAL;
import static net.von_gagern.martin.getopt.OptArg.REQUIRED;

enum CommandLineOption implements OptKey {

    size(REQUIRED, "Specify size for rendered images"),
    group(REQUIRED, "Group for automatic renderings"),
    tile(NONE, "Automatically render tiling of specified images"),
    opengl(NONE, "Display file in OpenGL viewer"),
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
