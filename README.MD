# DigiDuck

DuckyScript to DigiKeyboard compiler and editor for the [Digispark USB Development Board](http://digistump.com/products/1). Made this to replace Duck2Spark with an easier-to-use solution.

For more info on DuckyScript read the [documentation](https://github.com/hak5darren/USB-Rubber-Ducky/wiki/Duckyscript).

## Overview

The Hak5 [USB Rubber Ducky](https://shop.hak5.org/products/usb-rubber-ducky-deluxe) is a popular pentesting tool that delivers payloads by posing as a keyboard. At $50, it's also an expensive tool. A popular alternative is the cheaper [Digispark USB Development Board](http://digistump.com/products/1), available online for as low as $2. While Rubber Ducky payloads are written in a simple language called [DuckyScript](https://github.com/hak5darren/USB-Rubber-Ducky/wiki/Duckyscript), payloads for the Digispark board must be written in Arduino's C++ using a library called DigiKeyboard. This editor transpiles DuckyScript to C++ that's ready to be uploaded to a Digispark board.

## Installation

The editor is written in Java and requires no external dependencies. Simply open the project in your IDE of choice and run `Editor.java`. Runnable jar files will be provided for a future version.

Currently working on rewriting the project to Kotlin before fixing all the jank.

## Instructions

To use the Digispark board, you'll need to download the board's compatibility package. Add the following URL to your Arduino IDE under **File > Preferences > Additional Boards Manager URLs**:

`http://digistump.com/package_digistump_index.json`

For a more detailed tutorial on setting up the Digispark board, see [this HackerNoon article](https://hackernoon.com/low-cost-usb-rubber-ducky-pen-test-tool-for-3-using-digispark-and-duck2spark-5d59afc1910).

This editor requires replacing your DigiKeyboard.h with the one found in [this gist](https://gist.github.com/EmilyGoose/16bd8b67982ad84dfc79d425e6d8f077), as the original leaves out a few important keys. Currently looking into making the editor do this automatically.

Note: The compiler doesn't check if the compiled code is larger than the board will allow. If you're getting an error uploading, try reducing the size of your script.


