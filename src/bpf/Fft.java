package bpf;

import java.util.ArrayList;
import java.util.Locale;

public class Fft {

	public static double PI = 3.14159265358979323846264338327950288;
	
	public static void main(String args[]){
		
		Locale.setDefault(new Locale("ru", "RU"));
		
		Fft inst = new Fft();
		
	}

	public static ArrayList<ComplexNumber> fft( ArrayList<ComplexNumber> Points, int nstart, int nstep )
	{
		
	    double tmp, angle, rw, iw;
	    int n = 0, nn, i, j, k;
	    int logN = 2;
	    int N;
	    
	    if (nstep+nstart > Points.size()) nstep=Points.size()-nstart; 
	    
	    while (nstep > Math.pow(2, logN)) logN++;
	    
	    ArrayList<ComplexNumber> re = new ArrayList<ComplexNumber>((int)Math.pow(2, logN));

	    for (i=0; i < (int)Math.pow(2, logN)+nstart; i++)
	    {
	    	re.add(i, new ComplexNumber(0,0));
	        
	    }
	    
	    for (i=nstart; i < nstep+nstart; i++)
	    {
	        re.get(i-nstart).real =  Points.get(i).real;
	        re.get(i-nstart).img =  Points.get(i).img;
	        
	    }

	    
	    N = 1 << logN;
	    
	    for (i = N >> 1; i>0; i >>= 1) // выбираем числа согласно алгоритму прореживания по частоте
	    {
	    	
	        for (j = 0; j < (N >> 1) / i; j++)
	        {
	        	
	        	
	            for (k = 0; k < i; k++)
	            {
	                // складываем значения
	                nn = n + i;
	                
	                tmp = re.get(n).real + re.get(nn).real;
	                re.get(nn).real = re.get(n).real - re.get(nn).real;
	                re.get(n).real = tmp;
	                
	                tmp = re.get(n).img + re.get(nn).img;
	                re.get(nn).img = re.get(n).img - re.get(nn).img;
	                re.get(n).img = tmp;
	                
	                
	                // умножаем на поворотные коэффициенты
	                angle = PI / i * k;
	                rw = Math.cos(angle);
	                iw = Math.sin(angle);
	                tmp = rw * re.get(nn).real - iw * re.get(nn).img;
	                re.get(nn).img = rw * re.get(nn).img + iw * re.get(nn).real;
	                re.get(nn).real = tmp;
	                
	                n++;
	            }
	            
	            
	            n = (n + i) & (N - 1); // обнуляет n в нужные моменты
	            
	        } 
	    } 
	    

	    for (i=0; i < (int)Math.pow(2, logN); i++)
	    {
	    	ComplexNumber tm;
	    	tm = re.get(i);
	    	
	    	re.get(i).img = re.get(getAddr(i, logN)).img;
	    	re.get(i).real = re.get(getAddr(i, logN)).real;
	    	
	    	re.get(getAddr(i, logN)).img = tm.img;
	    	re.get(getAddr(i, logN)).real = tm.real;	    	
	    }
	    
	    
	    return re;
	} 
	
	public static int getAddr(int in, int logn)
	{
	    int res = 0;
	    for (int i = 0; i < logn; ++i)
	    {
	        res <<= 1;
	        res |= (in >> i) & 1;

	    }

	    return res;
	}
}
