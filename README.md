Flip
====

See <a href="http://genzeb.github.io/flip/">project website</a> for more detail.

----

Flip 3D View Animation and Transition

Create awesome looking 3D view transition in your android applications

The view transition animations in Android are very limited. One particular omission is the 3D flip transition that is common in iOS. This project contains a animation subclass that allows for a 3D flip animation and a library that takes a ViewAnimator (such as ViewFlipper) and animates a flip transition b/n its subviews (left-to-right or right-to-left).

The key missing ingredient from most flip animation is the zoom effect. Without it, simple flips on the y-axis appear very much 2D and lose their distinctive 3D feel.

Take a look at <a href="http://youtu.be/52mXHqX9f3Y">this video</a> to see how this looks.

There are also a number of other animations in the AnimationFactory class. Enjoy.



How to Install
----

Get a copy of the source here. Copy-paste the /src/com folder to your project's source folder. Use the flip transition in any of your ViewAnimator (such as ViewFipper) by doing:

      AnimationFactory.flipTransition(viewFlipper, FlipDirection.LEFT_RIGHT);
      
It's that easy!

If you want to create a FlipAnimation and use it some other way, you may do so by the constructor or the AnimationFactory.flipAnimation method.

Using as a Gradle Dependency
----

Get a copy of the source here.

Download and configure Gradle 1.9 from [here](http://www.gradle.org/downloads)

Execute the following command to assemble the AAR file

    ./gradlew assembleRelease

Upload the AAR file into your artifact repository.

Declare a dependency in your build.gradle

    dependencies {
        compile 'com.tekle.oss.android:flip:1.0'
    }

Disclaimer
----
You may use, modify, etc. this code. However, give credit per the license agreement.
