package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class PrimeGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n, double... p) {
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        int limit=n<6?15:(int)(n*(Math.log(n)+Math.log(Math.log(n)))+3);
        boolean[] sieve=new boolean[limit+1]; Arrays.fill(sieve,true); sieve[0]=sieve[1]=false;
        for(int i=2;i*i<=limit;i++) if(sieve[i]) for(int j=i*i;j<=limit;j+=i) sieve[j]=false;
        for(int i=2;r.size()<n&&i<=limit;i++) if(sieve[i]) r.add((double)i);
        return new SeriesResult(SeriesType.PRIME,r,"Sieve (Iterative)",System.nanoTime()-s,sieve.length,"n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n, double... p) {
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        int c=2; while(r.size()<n){if(isPrime(c,2))r.add((double)c);c++;}
        return new SeriesResult(SeriesType.PRIME,r,"Trial Division (Recursive)",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    private boolean isPrime(int num,int div){
        if(div*div>num)return true; if(num%div==0)return false; return isPrime(num,div+1);
    }
}
