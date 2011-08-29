Intro
=====

This is a set of classes I've created and I've found useful while creating
Android applications.  So far this includes 2 things:

org.spurint.android.listview
----------------------------

A list adapter and ListRow interface that makes it easy to create lists
using ListView that have section headers.  The SectionHeaderRow class
implements the section header rows.  You need to create other impls of
ListRow for your own data rows.

org.spurint.android.net
-----------------------

Wrapper class around Apache's HttpClient class to make it easy to do
async network requests.  Uses AsyncTask under the hood.

Usage
=====

Add the project to your Eclipse workspace.  Select the dependent project,
open its properties, and in the Android section, find the Library box.
Click Add, and select BTAndroidUtils (or whatever you've named it).
