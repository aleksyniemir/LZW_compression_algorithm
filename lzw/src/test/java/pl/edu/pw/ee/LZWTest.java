package pl.edu.pw.ee;

import org.junit.Test;

public class LZWTest {

    @Test
    public void compress() {
        LZW lzw = new LZW("C:\\Users\\olekn\\Desktop\\lzw\\test_files\\");
        lzw.compress();
    }

    @Test
    public void decompress() {
        LZW lzw = new LZW("C:\\Users\\olekn\\Desktop\\lzw\\test_files\\");
        lzw.decompress();
    }
}
