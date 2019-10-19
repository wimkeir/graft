package utils;

import java.io.*;
import java.util.Iterator;

public class PiIterator implements Iterator<Integer> {
    FileReader fIn;
    int n;
    int numRead;

    public PiIterator() throws IOException {
        fIn = new FileReader(new File(getClass().getClassLoader().getResource("dPi.in").getFile()));
        char[] buff = new char[1];
        numRead = fIn.read(buff);
        n = buff[0] - '0';

    }

    @Override
    public boolean hasNext() {
        return numRead != -1;
    }

    @Override
    public Integer next() {
        int ret = n;
        try {
            char[] buff = new char[1];
            numRead = fIn.read(buff);
            n = buff[0] - '0';
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) throws IOException {
        PiIterator it = new PiIterator();
        for (int i = 0; i < 10; i++) {
            System.out.print(it.next());
        }
    }
}
