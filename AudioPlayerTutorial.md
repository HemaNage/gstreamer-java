# Introduction #

Now for a real application - playing audio files.


# Details #

Since putting together pipelines is tedious, boring and somewhat error-prone
for newcomers to gstreamer, there are already pre-fabricated Pipelines that
do it all for you, such as a PlayBin.

So, we will be lazy and use a PlayBin to build our audio player.
```
public class AudioPlayer {
    public static void main(String[] args) {
        args = Gst.init("AudioPlayer", args);
        PlayBin playbin = new PlayBin("AudioPlayer");
        playbin.setInputFile(new File(args[0]));
        playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));
        playbin.setState(State.PLAYING);
        Gst.main();
        playbin.setState(State.NULL);
    }
}

```
You've already seen this line in the SimplePipelineTutorial:

> `args = Gst.init("AudioPlayer", args);`

Next we created a PlayBin instead of a basic Pipeline:

> `PlayBin playbin = new PlayBin("AudioPlayer");`

A PlayBin is just a special !Pipeline, so all !Pipeline methods work on it just
as they would on a normal Pipeline.

The PlayBin needs to know what file you want it to play, so tell it via:

> `playbin.setInputFile(new File(args[0]));`

You can use playbin.setURI() instead when playing a file from a URL.

By default, if the PlayBin detects video in the file being played, it will open
a window to display the video - we don't want that, so we use our friend the
fakesink to turn off that ability by throwing away the video data.

> `playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));`

After the PlayBin is set up, we set it playing, and cleanup after it finishes.
```
        playbin.setState(State.PLAYING);
        Gst.main();
        playbin.setState(State.NULL);
```
You will recognise the above lines from the SimplePipelineTutorial.

Note: The AudioPlayer does not exit - we will address how to deal with this in
another tutorial.