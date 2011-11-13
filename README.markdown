Intro
=====

This is a set of classes I've created and I've found useful while creating
Android applications.  Note that these do not represent final or stable APIs.
I'm still very much playing around with things and will change how these work
as I find better ways of doing things.

org.spurint.android.listview
----------------------------

A list adapter and ListRow interface that makes it easy to create lists
using ListView that have section headers.  The SectionHeaderRow class
implements the section header rows.  You need to create other impls of
ListRow for your own data rows.

org.spurint.android.net
-----------------------

Wrapper class around Apache's HttpClient class to make it easy to do
async, cancellable network requests.

org.spurint.android.viewcontroller
----------------------------------

This aims to be a (somewhat simpler) version of iOS's ViewController concept.
What I really missed when starting to work on Android was
UINavigationController.  This is super simple right now.  I intend to add
more to this over time.

Usage
=====

Add the project to your Eclipse workspace.  Select the dependent project,
open its properties, and in the Android section, find the Library box.
Click Add, and select BTAndroidUtils (or whatever you've named it).
