# Introduction #

The basic way of building gstreamer applications, is to construct a pipeline of
Elements together.


# Details #

Lets have a look at a simple pipeline that demonstrates this linking.
```
public class SimplePipeline {
    public static void main(String[] args) {
        args = Gst.init("SimplePipeline", args);
        Pipeline pipe = new Pipeline("SimplePipeline");
        Element src = ElementFactory.make("fakesrc", "Source");
        Element sink = ElementFactory.make("fakesink", "Destination");
        pipe.addMany(src, sink);
        src.link(sink);
        pipe.setState(State.PLAYING);
        Gst.main();
        pipe.setState(State.NULL);
    }
}
```

Although that is a very simple pipeline, it can look a bit daunting at first,
so lets break it up.

The first thing you need to do is to initialize the gstreamer framework.  This
is achieved via a call to Gst.init():
```
        args = Gst.init("SimplePipeline", args);
```

The first argument is the program name.  You can supply any string here, as it
is only used by gstreamer for debugging and error messages, but supplying the
name of your main class would be a good idea.



Next, we get a new Pipeline element to hold the other elements.
```
        Pipeline pipe = new Pipeline("SimplePipeline");
```

Every application you construct will have a Pipeline (or a Pipeline subclass),
as it is the primary mechanism by which gstreamer processing happens.




Now lets add the first element to the Pipeline.  All pipelines will need to
start with a source element - i.e. where the data comes from.
```
        Element src = ElementFactory.make("fakesrc", "Source");
```

In this case, we are using a "fake" source - this generates empty buffers and
sends them downstream.  In other tutorials, you will use a real source, such as
a file src ("filesrc") which can read from files, but for this tutorial, we
don't want the real world to interfere.





Just as all pipelines have a source to read from, they need a sink that performs
processing, or outputs the data to a device, such as the speakers, the screen,
or a file.
```
        Element sink = ElementFactory.make("fakesink", "Destination");
```

Analogous to how the fakesrc generates empty buffers and sends them downstream,
fakesink accepts buffers and throws them away.



We now need to construct the pipeline by adding them elements to the pipeline,
and then linking them together.
```
        pipe.addMany(src, sink);
        src.link(sink);
```
We're almost done!  All that remains to do is to start the pipeline processing,
and wait until it completes.
```
        pipe.setState(State.PLAYING);
        Gst.main();
```

The first line will start the !Pipeline in a background thread, where it will
continue processing data until its state is changed to State.NULL or
State.READY.

The Gst.main() call simply waits until Gst.quit() is called.  This is useful
when you don't have a GUI, and you want to wait until the pipeline finishes.



Finally, after processing has completed, we need to clean up the pipeline by
setting its state back to the default state, or else gstreamer will get upset.
```
        pipe.setState(State.NULL);
```

Thats it!   You have now built a gstreamer application, using all the basic
operations you will need to build something more complex.