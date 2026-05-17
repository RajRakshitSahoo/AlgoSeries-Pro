package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class CustomGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... params){
        List<Double> seeds=new ArrayList<>(); for(double p:params) seeds.add(p);
        if(seeds.isEmpty()){seeds.add(1.0);seeds.add(1.0);}
        List<Double> r=new ArrayList<>(seeds); long s=System.nanoTime(); int k=seeds.size();
        while(r.size()<n){int sz=r.size();double sum=0;for(int i=Math.max(0,sz-k);i<sz;i++)sum+=r.get(i);r.add(sum);}
        return new SeriesResult(SeriesType.CUSTOM,r,"Iterative",System.nanoTime()-s,r.size()*8L,"seeds="+seeds+",n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){return generateIterative(n,p);}
}
