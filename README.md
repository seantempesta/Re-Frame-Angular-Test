# re-frame example app using Angular as view layer

Warning:  I have no idea what I'm doing. :)

Run "`lein figwheel`" in a terminal to compile the app, and then open example.html.

Any changes to ClojureScript source files (in `src`) will be reflected in the running page immediately (while "`lein figwheel`" is running).

Run "`lein clean; lein with-profile prod compile`" to compile an optimized version.

You'll also need to run a webserver in the root directory.  The built in one for Python works pretty well:

python -m SimpleHTTPServer 8080

Original reagent example code found at https://github.com/reagent-project/reagent
