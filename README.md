# libpython-clj-examples

### Overview

This repo contains some examples of using libpython-clj with various python libraries.
So far there are source code examples meant to be walked through in the REPL

- GPT2 text generation from hugging-face
- MXNet MNIST classification using the Module API
- Matlib PyPlot
- NLTK
- SpaCy

In general, you will need a python3 env and pip install the various packages
before running


### Installation

The code is using the most recent snapshot from libpython-clj to get the latest and greatest interop syntax. To use it you will have to:

* `git clone git@github.com:cnuernber/libpython-clj.git`
* `cd cd libpython-clj`
* `lein install`

You should have `libpython-clj-1.31-SNAPSHOT.jar` installed locally then and be able to run the examples


## License

Copyright © 2020 Carin Meier

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
