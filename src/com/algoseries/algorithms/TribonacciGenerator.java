package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class TribonacciGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n, double... p) {
        List<Double> r = new ArrayList<>();
        long s = System.nanoTime();
        if (n>=1) r.add(0.0); if (n>=2) r.add(1.0); if (n>=3) r.add(1.0);
        for (int i=3;i<n;i++) r.add(r.get(i-1)+r.get(i-2)+r.get(i-3));
        return new SeriesResult(SeriesType.TRIBONACCI,r,"Iterative",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n, double... p) {
        Map<Integer,Double> m = new HashMap<>(); List<Double> r = new ArrayList<>();
        long s = System.nanoTime();
        for (int i=0;i<n;i++) r.add(trib(i,m));
        return new SeriesResult(SeriesType.TRIBONACCI,r,"Recursive (memoised)",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    private double trib(int n,Map<Integer,Double> m){
        if(n==0)return 0; if(n<=2)return 1; if(m.containsKey(n))return m.get(n);
        double v=trib(n-1,m)+trib(n-2,m)+trib(n-3,m); m.put(n,v); return v;
    }
}
