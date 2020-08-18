![alt text](https://iili.io/djvGLl.jpg)

# BankCardReader
A try to read card number and expire date on printed cards.

## Built With
- [tess-two](https://github.com/rmtheis/tess-two/)  - A fork of [Tesseract](https://github.com/tesseract-ocr/tesseract/) Tools for Android. An OCR tool.
- [CameraView](https://github.com/natario1/CameraView/) - A high level custom camera library for capturing photo and video in Android.
- [GPUImage for Android](https://github.com/cats-oss/android-gpuimage/) - A port of GPUImage in iOS to Android. (For filter images captured from camera)
- [Dexter](https://github.com/Karumi/Dexter/) - Dexter is an Android library that simplifies the process of requesting permissions at runtime.

I used English language trained data files for Tesseract so you can use your own language. Pick up them from library page and copy to correct path on Internal memory.

In the source code i put the lines:

```
baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!?@#$%&*()<>_-+=.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "/0123456789");
```
You can remove them to read card owner name. Play with Tesseract arguments and GPUImage filters to achieve best results.
