# Simple MongoDB/Casbah plugin for Play! Framework

Work in progress, in the 'works for me' kind of way. YMMV.

## Setup

If you still want to use it, the best way now is to add the play-casbah as submodule to your project. You can find more
info about git submodules in [Git documentation](http://git-scm.com/book/en/Git-Tools-Submodules).

    mkdir modules
    git submodule add git://github.com/mprihoda/play-casbah.git play-casbah
    cd play-casbah
    git checkout develop

That will add the current `develop` branch as submodule. Next, you need to add the dependency to SBT. You need to edit
`project/Build.scala` as noted in [Play Wiki](https://github.com/playframework/Play20/wiki/SBTSubProjects). Add:

    val playCasbah = Project("play-casbah", file("modules/play-casbah"))

and dependency for the Play project:

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
        // Add your own project settings here
    ).dependsOn(playCasbah)

And, finally, you need to add plugin to project configuration, which means creating file `conf/play.plugins`, if it does
not exist, and adding a line:

    2000:net.prihoda.play.casbah.MongoDatabasePlugin

and adding a Mongo section to your `conf/application.conf` file with correct URL:

    # Mongo
    # ~~~~~
    mongo.db.uri="mongodb://127.0.0.1/somedb"

Note that with defaul Mongo setup, the `somedb` gets created automatically, if it does not exists.

Good luck, have fun.