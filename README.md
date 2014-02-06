GeoKettle FreeFrame Plugin
==========================

Transformation step plugin for GeoKettle using [triangular transformation networks.](http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/lv95/lv03-lv95/chenyx06.html) 
<h2>Installation</h2>
Download the zipped plugin (FreeFrame.zip) from [its github release section.](https://github.com/edigonzales/geokettle_freeframe_plugin/releases) 
Unzip the file and copy the folder with its content (freeframestep.jar, icon.png and plugin.xml) to the GeoKettle plugin-steps directory (e.g. `/home/stefan/Apps/geokettle-2.5/plugin/steps/`). 

You should have something like this:

![ScreenShot](https://github.com/edigonzales/geokettle_freeframe_plugin/raw/master/data/images/installation_01.png)

If the installation was successful you see a new entry (FreeFrame Plugin) in the Transform section:

![ScreenShot](https://github.com/edigonzales/geokettle_freeframe_plugin/raw/master/data/images/installation_02.png)

I assembled a [carefree package](http://www.catais.org/tmp/geokettle-2.5.zip) which contains GeoKettle including the plugin. It will *not* be updated with every new plugin release.

<h2>Use</h2>
See this [blog post.](http://www.sogeo.ch/)

<h2>Development</h2>
Fork and clone this repository. The sources come in the form of an eclipse project. You need to satisfy some external dependencies after importing it into your workspace. Add the the jars from GeoKettle's *lib* folder and the SWT library from GeoKettle's *libswt* folder. You only need the swt.jar for the system you are developing on (e.g `/home/stefan/Apps/geokettle-2.5/libswt/linux/x86_64/swt.jar` if you are on a 64bit linux os). Then add all the GeoTools stuff (every jar from the *libext* folder). 

Export your project as jar file to GeoKettle's step plugin directory (e.g. `/home/stefan/Apps/geokettle-2.5/plugins/steps/FreeFrame/`). You also need to export or copy the icon.png and the plugin.xml file into the same folder. That's it.

For general Kettle plugin development see also this [tutorial](http://type-exit.org/adventures-with-open-source-bi/2010/06/developing-a-custom-kettle-plugin-a-simple-transformation-step/).

