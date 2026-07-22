package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class FibonacciGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        if(n>=1)r.add(0.0); if(n>=2)r.add(1.0);
        for(int i=2;i<n;i++) r.add(r.get(i-1)+r.get(i-2));
        return new SeriesResult(SeriesType.FIBONACCI,r,"Iterative",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        Map<Integer,Double> m=new HashMap<>(); List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        for(int i=0;i<n;i++) r.add(fib(i,m));
        return new SeriesResult(SeriesType.FIBONACCI,r,"Recursive (memoised)",System.nanoTime()-s,r.size()*8L+m.size()*24L,"n="+n,"");
    }
    private double fib(int n,Map<Integer,Double> m){
        if(n<=1)return n; if(m.containsKey(n))return m.get(n);
        double v=fib(n-1,m)+fib(n-2,m); m.put(n,v); return v;
    }
}
 
