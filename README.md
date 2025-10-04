# Mandelbrot CL

Simple Swing animation, allowing to explore the Mandelbrot set.
By default, it uses CPU but a boolean can be set to use OpenCL.

## Requirements

- JDK 25
- Maven 3.6.3+
- Git

## Run it

```shell
git clone https://github.com/Achaaab/mandelbrot-cl.git
cd mandelbrot-cl
mvn package
java -jar target/mandelbrot-cl.jar
```

## Controls

- **Zoom in / out**: mouse wheel
- **Move**: mouse drag
- **Increase / decrease depth**: <kbd>Shift</kbd> + mouse wheel
- **Show / hide coordinates**: mouse click

## Screenshots
<img src="data/screenshots/mandelbrot.png" width="1024" alt="Mandelbrot set"/>
<img src="data/screenshots/mandelbrot_0.png" width="1024" alt="Mandelbrot zoom"/>
<img src="data/screenshots/mandelbrot_1.png" width="1024" alt="Mandelbrot zoom"/>
<img src="data/screenshots/mandelbrot_2.png" width="1024" alt="Mandelbrot zoom"/>
<img src="data/screenshots/mandelbrot_3.png" width="1024" alt="Mandelbrot zoom"/>

## Authors
* **Jonathan Guéhenneux** - *Programmer* - [Achaaab](https://github.com/Achaaab)

## License
This project is licensed under the GNU General Public License (GPL) - see the [LICENSE.md](LICENSE.md) for the details.
