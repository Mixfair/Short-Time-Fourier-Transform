package bpf;

import java.util.ArrayList;

import java.util.Collections;
import java.lang.Math; 
import java.io.File;
import java.io.IOException;
import java.io.FileWriter; 
import java.io.FileReader; 
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import bpf.ComplexNumber;
import java.util.Arrays;
import java.util.Locale;
import com.google.common.primitives.Doubles;

public class Sfft {
	
	public static double PI = 3.14159265358979323846264338327950288;
	
	public ArrayList<ComplexNumber> sPoints = new ArrayList<ComplexNumber>();
	double [][] powerful;
	double [] time;
	double [] freq;
	double fs = 175.0;
	double width = 0.1;
	
	public static void main(String args[]){
		
		Locale.setDefault(new Locale("ru", "RU"));
		
		Sfft inst = new Sfft();
		
		inst.readConfig(); 
		inst.sfft();
		inst.savePoints();
	}
	
	public void sigPrepare() {
		
		int k = 0;
		
		int N = (int)((1-1/this.fs)/(1/this.fs)) + 1;
		
		for (k=0; k < N; k++) {
			sPoints.add(new ComplexNumber(Math.sin(2 * PI * 10 * k / this.fs), 0));
		}
		
		for (k=0; k < N; k++) {
			sPoints.add(new ComplexNumber(Math.sin(2 * PI * 20 * k / this.fs), 0));
		}
		
		for (k=0; k < N; k++) {
			sPoints.add(new ComplexNumber(Math.sin(2 * PI * 30 * k / this.fs), 0));
		}
		
	}
	
	public void sfft() {
		
		int i, j;
		double wL = this.width;
		double fs = this.fs;
		int N = sPoints.size();
		int window = (int)(fs * wL);
		int countW = (int)Math.ceil((N / window));
		powerful = new double[countW][window/2];
		time = new double[countW];
		freq = new double[window/2];
		
		ComplexNumber [][] power = new ComplexNumber[countW][window];
		
		for (i = 0; i < countW; i++) {
			ArrayList<ComplexNumber> fftPoints = fft(sPoints, i*window, window);
	        for (j = 0; j < window; j++) {
	        	power[i][j] = new ComplexNumber(fftPoints.get(j).real, fftPoints.get(j).img);
	        }
	    }
		for (i = 0; i < countW; i++) {
			for (j = 0; j < window/2; j++) {
				powerful[i][j] = 2 * Math.sqrt(power[i][j].real * power[i][j].real + power[i][j].img * power[i][j].img) / window;
			}
		}
		for (i = 0; i < countW; i++) {
			time[i] = (i+1)*wL;
		}
		for (i = 0; i < window/2; i++) {
			freq[i] = (double)i/window*fs;
		}
	}
	
	ArrayList<ComplexNumber> fft( ArrayList<ComplexNumber> Points, int nstart, int nstep )
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
	    
	    for (i = N >> 1; i>0; i >>= 1) // �������� ����� �������� ��������� ������������ �� �������
	    {
	        for (j = 0; j < (N >> 1) / i; j++)
	        {
	            for (k = 0; k < i; k++)
	            {
	                // ���������� ��������
	                nn = n + i;
	                tmp = re.get(n).real + re.get(nn).real;
	                re.get(nn).real = re.get(n).real - re.get(nn).real;
	                re.get(n).real = tmp;
	                
	                tmp = re.get(n).img + re.get(nn).img;
	                re.get(nn).img = re.get(n).img - re.get(nn).img;
	                re.get(n).img = tmp;

	                // �������� �� ���������� ������������
	                angle = PI / i * k;
	                rw = Math.cos(angle);
	                iw = Math.sin(angle);
	                tmp = rw * re.get(nn).real - iw * re.get(nn).img;
	                re.get(nn).img = rw * re.get(nn).img + iw * re.get(nn).real;
	                re.get(nn).real = tmp;
	                
	                n++;
	            }
	            n = (n + i) & (N - 1); // �������� n � ������ �������
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
	
	int getAddr(int in, int logn)
	{
	    int res = 0;
	    for (int i = 0; i < logn; ++i)
	    {
	        res <<= 1;
	        res |= (in >> i) & 1;

	    }

	    return res;
	}
	
	public void readConfig() {
		String sigdata[];
		try {
			FileReader conf = new FileReader("sig.csv");
			CSVParser parser = CSVParser.parse(conf, CSVFormat.EXCEL);
			int i = 0;
			for (CSVRecord rec : parser) {
			     switch (i) {

		    	 	case 0: // sig
		    	 		sigdata = rec.get(0).split(";");
		    	 		for (String val : sigdata) if (!val.isEmpty()) sPoints.add(new ComplexNumber(Double.parseDouble(val), 0));
		    	 		break;
		    	 	case 1: // fs
		    	 		sigdata = rec.get(0).split(";");
		    	 		this.fs = Double.parseDouble(sigdata[1]);
		    	 		break;
		    	 	case 2: // width
		    	 		sigdata = rec.get(0).split(";");
			    		this.width = Double.parseDouble(sigdata[1]);
		    	 		break;
			     }
			    i++;	
			 }
		} catch (IOException e) {
		    this.sigPrepare();
		    this.writeDefaultConfig();
		}
		
		System.out.println("Starting sftf with: Width: " + this.width + ";Fs: "+ this.fs);
	}
	
	public void savePoints() {
		int i,j;
		try {
			FileWriter points = new FileWriter("points.csv");
			String x = ";" + Doubles.join(";", time)+'\n';
			points.write(String.format(Locale.ENGLISH, x));
			for (i = 0; i < powerful[0].length; i++) {
				x = Math.ceil(freq[i]) + ";";
				for (j = 0; j < powerful.length; j++) {
					x += powerful[j][i] + ";";
				}
				points.write(x+'\n');
			}
			System.out.println(Locale.getDefault());
			points.close();
		 } catch (IOException e) {
		    System.out.println("err");
		 }
		
	}
	
	public void writeDefaultConfig() {
		try {
			FileWriter points = new FileWriter("sig.csv");
			String x = ";";
			for (ComplexNumber num : sPoints) x += num.real + ";";
			points.write(String.format(Locale.ENGLISH, x));
			x = "\nfs;"+this.fs+"\nwidth;"+this.width+"";
			points.write(x);
			points.close();
			
		 } catch (IOException e) {
		    System.out.println("err");
		 }
	}
	
	
}
