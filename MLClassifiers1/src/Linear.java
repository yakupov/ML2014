import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;



public class Linear {
	private class Triple {
		public double s, r, p;

		public Triple(double s, double r, double p) {
			super();
			this.s = s;
			this.r = r;
			this.p = p;
		}
		
		public String toString() {
			return String.format("S=%f, nRooms=%f, expPrice=%f", s, r, p);
		}
	}
	
	final double step = 2e-12;
	final double eps = 1e-6;
	double k1 = 1;
	double k2 = 1; //y = k1 * s + k2 * r + k3
	double k3 = 1; //L = (y - p)^2
	
	public static void main(String[] args) throws FileNotFoundException {
		new Linear().work();
	}
	
	//partial derivatives for gradient descent
	public double dy(Triple t) {
		//System.err.println(t.toString());
		//System.err.println(k1 + "_" + k2 + "_" + k3);
		//System.err.println(k1 * t.s + k2 * t.r + k3 - t.p);

		//return 2 * Math.pow(k1 * t.s + k2 * t.r + k3 - t.p, 2);
		return -(k1 * t.s + k2 * t.r + k3 - t.p);
	}

	public double ds(Triple t) {
		return k1 * (k1 * t.s + k2 * t.r + k3 - t.p);
	}
	
	public double dr(Triple t) {
		return k2 * (k1 * t.s + k2 * t.r + k3 - t.p);
	}

	public void work() throws FileNotFoundException {
		Scanner sc = new Scanner(new FileInputStream("prices.txt"));
		List<Triple> ls = new ArrayList<Triple>();
		while (sc.hasNext()) {
			String s = sc.nextLine();
			String[] ss = s.split(",");
			ls.add(new Triple(
					Double.parseDouble(ss[0]), 
					Double.parseDouble(ss[1]),
					Double.parseDouble(ss[2])));
		}
		sc.close();
		
		Collections.shuffle(ls, new Random());
		final int m = ls.size() * 2 / 3 + 1;
		List<Triple> learn = ls.subList(0, m);
		List<Triple> check = ls.subList(m, ls.size());

		while (!stop(learn)) {
			//System.err.println(k1 + "_" + k2 + "_" + k3);
			if (Double.isNaN(k1 + k2 + k3)) throw new RuntimeException("nan");
			if (Double.isInfinite(k1 + k2 + k3)) throw new RuntimeException("inf");
			
			double t1, t2, t3;
			t1 = t2 = t3 = 0;
			for (Triple t: learn) {
				t1 += step * (ds(t) / learn.size());
				t2 += step * (dr(t) / learn.size());
				t3 += step * (dy(t) / learn.size());
			}

			k1 -= t1;
			k2 -= t2;
			k3 -= t3;
		}
		test(check);
	}

	private void test(List<Triple> check) {
		for (Triple t: check) {
			System.out.println(t.toString() + "__" + f(t));
		}
	}

	int i = 0;
	private boolean stop(List<Triple> learn) {	
		if (i++ > 100000) return true;
		
		double totalErr = 0;
		for (Triple t: learn) {
			totalErr += Math.sqrt(Math.pow(f(t) - t.p, 2)) / learn.size();
			//System.err.println("_____" + f(t));
		}
		System.err.println("__" + totalErr);
		return totalErr < eps;
	}

	private double f(Triple t) {
		return k1 * t.s + k2 * t.r + k3;
	}

}
