## gcu - (G)it (C)atch-(U)p - run git pull for all projects from common directory.

[![Clojars Project](https://img.shields.io/clojars/v/gcu.svg)](https://clojars.org/gcu)
[![Dependencies Status](https://jarkeeper.com/agilecreativity/gcu/status.svg)](https://jarkeeper.com/agilecreativity/gcu)
[![NPM](https://badge.fury.io/js/gcu.svg)](https://badge.fury.io/js/gcu)

Run `git pull` on all projects from a given directory using Lumo/ClojureScript.

### Pre-requisites

- [Lumo](https://github.com/anmonteiro/lumo)
- [ClojureScript](https://github.com/clojure/clojurescript)
- [NodeJS](https://nodejs.org/en/)
- [NPM](https://www.npmjs.com/get-npm)

For OSX:

```
brew install lumo
brew install coreutils
```

### Basic Installation

```sh
# Install Lumo via NPM (if not already)
npm install -g lumo-cljs

# Install the library from npm
npm install -g gcu

# This will allow you to run something like
gcu ~/path/to/your-base-directory

# To allow verbose mode try
gcu ~/projects/clojure-src verbose true
```

### Sample Session

![gcu-sample-session](https://github.com/agilecreativity/gcu/raw/master/gcu-sample-session.gif "sample session")

## Licenses

Copyright © 2018 Burin Choomnuan

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
