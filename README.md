# Image Style Transfer

#### A project that can compose an image into a cartoon or the style of another image by using pre-trained deep learning models.

A GLSurfaceView is used for rendering image into view and the image data is transfered from view to ML models, and vice versa, through a pixel buffer rather than bitmap. This GLSurfaceView provides a OpenGL ES context which will be effective in further pre/post processing.

### Used Deep Learning Models
- [CartoonGAN model ](https://tfhub.dev/sayakpaul/lite-model/cartoongan/int8/1)
- [Arbitrary-image-stylization](https://tfhub.dev/google/lite-model/magenta/arbitrary-image-stylization-v1-256/)

### Image Resources
- Image resources as *Styled Images* or *Sample Input* are picked from [Unsplash](https://unsplash.com/)

### Outputs
![](/sample/outputs.png)

### Acknowledgments
- Thank you to @MasayukiSuda for his work on [Mp4Composer-android](https://github.com/MasayukiSuda/Mp4Composer-android) project where OpenGL ES components are organized very well.
- Thanks to Google TensorFlow team and the people who review and publish [TFLite version of ML models](https://tfhub.dev/).
