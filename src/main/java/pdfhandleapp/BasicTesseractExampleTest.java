package pdfhandleapp;

import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;

public class BasicTesseractExampleTest {

//    @Test
    public String givenTessBaseApi_whenImageOcrd_thenTextDisplayed(String f) throws Exception {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
//        api.oem();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
        PIX image = pixRead(f);
        api.SetImage(image);
        // Get OCR result
        outText = api.GetUTF8Text();
        String string = outText.getString();
//        assertTrue(!string.isEmpty());
        System.out.println("OCR output:\n" + string);

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
        return string;
    }

    public String getTextToken(byte[] imagedata, int width, int height, int bytes_per_pixel, int bytes_per_line) throws Exception {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        // Open input image with leptonica library
//        PIX image = pixRe
        api.SetImage(imagedata, width, height, bytes_per_pixel, bytes_per_line);
        // Get OCR result
        outText = api.GetUTF8Text();
        String string = outText.getString();
//        assertTrue(!string.isEmpty());
        System.out.println("OCR output:\n" + string);

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
//        pixDestroy(image);
        return string;
    }

}
