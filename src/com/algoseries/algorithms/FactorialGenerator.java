package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.math.BigInteger;
import java.util.*;
public class FactorialGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        BigInteger f=BigInteger.ONE;
        for(int i=1;i<=n;i++){f=f.multiply(BigInteger.valueOf(i));r.add(f.doubleValue());}
        return new SeriesResult(SeriesType.FACTORIAL,r,"Iterative",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        Map<Integer,BigInteger> m=new HashMap<>(); List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        for(int i=1;i<=n;i++) r.add(fact(i,m).doubleValue());
        return new SeriesResult(SeriesType.FACTORIAL,r,"Recursive (memoised)",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    private BigInteger fact(int n,Map<Integer,BigInteger> m){
        if(n<=1)return BigInteger.ONE; if(m.containsKey(n))return m.get(n);
        BigInteger v=BigInteger.valueOf(n).multiply(fact(n-1,m)); m.put(n,v); return v;
    }
}
