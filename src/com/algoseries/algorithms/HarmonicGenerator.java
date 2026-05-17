package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class HarmonicGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        double a=p.length>0?p[0]:1, d=p.length>1?p[1]:1;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        for(int i=0;i<n;i++){double ap=a+i*d;r.add(ap==0?Double.NaN:1.0/ap);}
        return new SeriesResult(SeriesType.HARMONIC,r,"Iterative",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,d=%.2f,n=%d",a,d,n),"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        double a=p.length>0?p[0]:1, d=p.length>1?p[1]:1;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime(); buildHP(r,a,d,0,n);
        return new SeriesResult(SeriesType.HARMONIC,r,"Recursive",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,d=%.2f,n=%d",a,d,n),"");
    }
    private void buildHP(List<Double> l,double a,double d,int i,int n){if(i>=n)return;double ap=a+i*d;l.add(ap==0?Double.NaN:1.0/ap);buildHP(l,a,d,i+1,n);}
}
