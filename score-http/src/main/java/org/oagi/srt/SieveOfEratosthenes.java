package org.oagi.srt;

import com.google.common.collect.ContiguousSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SieveOfEratosthenes {

    public static void main(String[] args) throws Exception {

        int max = 50;
        Set<Integer> set = new HashSet(ContiguousSet.closed(2, max));

        for (int i = 2; i <= max; ++i) {
            if (!set.contains(i)) {
                continue;
            }

            int j = 2;
            while (i * j <= max) {
                if (set.contains(i * j)) {
                    set.remove(i * j);
                }
                j++;
            }
        }

        List<Integer> list = new ArrayList(set);
        System.out.println(list.size());
        System.out.println(list);
        System.out.println( list.stream().map(e -> BigInteger.valueOf(e)).reduce(BigInteger.ONE, (a, b) -> a.multiply(b)) );

        System.out.println(String.join(" * ", list.stream().map(e -> String.valueOf(e)).collect(Collectors.toList())));
    }

}
