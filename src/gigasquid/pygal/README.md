# Pygal - Sexy Python Charting 

- Github - [Kozea/pygal](https://github.com/Kozea/pygal)
- Official [documentation](http://www.pygal.org/en/latest/documentation/index.html) page
- Based on the following examples from [pygal.org](http://pygal.org).

## Basic installation 

- Python 

```shell
pip install pygal

## Optionally you should also install the following library
pip install lxml # improve rendering speed (except on pypy)

## To render png output
pip install cairosvg
pip install tinycss
pip install cssselect
```

- Clojure 

Just run your favorite `cider-jack-in` if you are on Emacs.
For other editors, you will do the equivalent command for your editor.

### SVG Viewer (if you are on Mac OS)  

To render SVG on the Mac you may like to also install [http://gapplin.wolfrosch.com/](Gapplin) via [Mac's AppStore](https://apps.apple.com/us/app/gapplin/id768053424?mt=12)

This will make the `open some-file.svg` work properly.
