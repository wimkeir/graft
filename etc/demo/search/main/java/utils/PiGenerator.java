package utils;

import java.math.BigInteger;
import java.util.Iterator;

import static java.math.BigInteger.*;


/**
 * A generator for the digits of pi
 * using the methods described on rosettacode.org
 * See https://rosettacode.org/wiki/Pi#Java
 */
public class PiGenerator implements Iterator<Integer> {

    final BigInteger TWO = valueOf(2);
    final BigInteger THREE = valueOf(3);
    final BigInteger FOUR = valueOf(4);
    final BigInteger SEVEN = valueOf(7);

    BigInteger q = ONE;
    BigInteger r = ZERO;
    BigInteger t = ONE;
    BigInteger k = ONE;
    BigInteger n = valueOf(3);
    BigInteger l = valueOf(3);

    BigInteger nn, nr;

    public static void main(String[] args) {
        PiGenerator p = new PiGenerator();
        for (int j = 0; j < 100; j++) {
            System.out.print(p.next());
        }
    }


    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        //loop to find next digit
        while (FOUR.multiply(q).add(r).subtract(t).compareTo(n.multiply(t)) != -1) {
            nr = TWO.multiply(q).add(r).multiply(l);
            nn = q.multiply((SEVEN.multiply(k))).add(TWO).add(r.multiply(l)).divide(t.multiply(l));
            q = q.multiply(k);
            t = t.multiply(l);
            l = l.add(TWO);
            k = k.add(ONE);
            n = nn;
            r = nr;
        }

        // The digit
        BigInteger tmp = n;

        //Setup for next loop
        nr = TEN.multiply(r.subtract(n.multiply(t)));
        n = TEN.multiply(THREE.multiply(q).add(r)).divide(t).subtract(TEN.multiply(n));
        q = q.multiply(TEN);
        r = nr;
        System.out.flush();

        return tmp.intValue();
    }

}