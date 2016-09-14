![morenaments logo](http://www.morenaments.de/images/logo100.png)

# morenaments conform – conformal hyperbolization of ornaments

This is very experimental software used by the author to demonstrate
how conformal transformations can be used to transform Euclidean
ornaments into hyperbolic ones.
The approach is discussed e.g. in an article titled
[*Hyperbolization of Ornaments*][hyp] as well as in a dissertation titled
[*Creating Hyperbolic Ornaments*][diss].

## How to run

Since this is experimental software for proof of concept, it
originally wasn't designed to be easily executable by a wide audience,
nor tested on a sufficient number of systems. So there might be some
trouble for some people. If so, feel free to report any issues you
encounter, in order to make this software available to more easily.

### Building from source

In theory, all you need to do is [get the sources][get] and execute
`ant run` from the command line. This should get Apache Ivy unless
it's available already, and it should use that to fetch all required
dependencies. It should then open the main application window.

### Precompiled releases

I will try to put together all the required files in a way that makes
executing them easy on most platforms. But that will still require
some work. If you want to help, e.g. by offering to test such
compilations, please write me an email.

## How to use

To view a hyperbolized version of some Euclidean ornament,
one would usually follow these steps:

1. Launch application as described above.
1. Use menu *File* item *Stat euc* to launch *morenaments euc* (an
   ornament drawing program) in a separate window.
1. In that new window, select *Recognize…* from the *File* menu.
1. Select a file containing a sufficiently large portion of some
   Euclidean ornament.
1. When asked whether a given symmetry feature is present in that ornament,
   just follow the program's advice on whether it is present or not.
1. Verify that you now see a perfectly symmetric version of the
   Euclidean ornament.
1. Choose *Render to conformal* from the *File* menu for improved quality.
1. Switch back to the main window.
   Both of the previous steps should have created a subwindow there
   containing one translative cell of the ornament.
   The second window (from the *Render* step) is usually at higher
   resolution (larger image) than the former (from the *Recognize*
   step).
   The image may look skewed if your translative cell was not square,
   but that's not a problem; it will be compensated for later on.
1. Select *Hyperbolic Tile* from the *Transform* menu.
1. Increase at least one of the numbers. These numbers represent the
   order of the different centers of rotation in your ornament.
   Increasing them ensures a decrease in interior angle sum, thus
   making the ornament hyperbolic. Start rendering by pressing *OK*.
1. You see a large and mostly transparent subwindow. You may have to
   scroll in order to actually see the tile that just got rendered.
1. In the *View* menu, choose *OpenGL RPL* to view the whole ornament
   using computation on your GPU. If this fails, look at the output in
   your command line window, and file a bug report after looking
   through existing reports.
1. You can interactively navigate the resulting visualization:
   * Drag the right mouse button in the inner parts of the disk to
     translate the hyperbolic plane.
   * Drag the right mouse button near the rim to rotate the disk.
   * Use the scroll wheel to zoom into the model.
     If you zoom far towards the rim of the disk, you get a pretty
     good approximation of the Poincaré half plane model.
   * If you zoomed in, use the left mouse button to translate the
     model in a Euclidean sense.

There are other alternatives.
One could export an ornament from *morenaments euc* to a file,
and load that file in the main window.
One could create the fundamental cell in some other application,
adding required symmetry information in a comment as export from
*morenaments euc* does.
One could manually add strokes to the recognized ornament,
or start with a completely hand-drawn ornament in the first place.

## How to help

If you consider this application useful, please drop me a line to let
me know. I'm more inclined to improve a program that is being used
rather than one I just use myself to demo things.

If you want to get involved with the actual coding, feel free to make
use of GitHub and its pull requests. Contributions are very welcome.
I'll try to set up all the dependencies I wrote as GitHub projects as
well.

[get]: https://github.com/gagern/morenaments-conform/archive/master.zip
[hyp]: http://www.combinatorics.org/Volume_16/Abstracts/v16i2r12.html
[diss]: http://nbn-resolving.de/urn/resolver.pl?urn:nbn:de:bvb:91-diss-20140717-1210572-1-6
