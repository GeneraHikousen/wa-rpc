package org.wa.client.service;

/**
 * @Auther: XF
 * @Date: 2018/10/9 14:55
 * @Description:
 */
public class GetPrimeNumberService implements GetPrimeNumber{

    {
        System.out.println("GetPrimeNumberService was create");
    }

    public int NthPrime(int n){
        int i = 2, j = 1;
        while (true) {
            j = j + 1;
            if (j > i / j) {
                n--;
                if (n == 0)
                    break;
                j = 1;
            }
            if (i % j == 0) {
                i++;
                j = 1;
            }
        }
        return i;
    }
}
