import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class Log {
	private class Triple {
		public double x1, x2, y;

		public Triple(double x1, double x2, double y) {
			super();
			this.x1 = x1;
			this.x2 = x2;
			this.y = y;
		}
		
		public String toString() {
			return String.format("x1=%f, x2=%f, y=%f", x1, x2, y);
		}
	}
	
	double step = 4e-3;
	final double eps = 0.3;
	double k1 = 1;
	double k2 = 1; //y = k1 * x1 + k2 * x2 + k3
	double k3 = 1; //L = sum((g(X * theta) - y) ^ 2) / length(y)

	public static void main(String[] args) throws FileNotFoundException {
		new Log().work();
	}

	private double g(double tetaTX) {
		return 1.0 / (1 + Math.exp(-tetaTX));
	}


	//partial derivatives for gradient descent
	public double dy(Triple t) {
		return (g(k1 * t.x1 + k2 * t.x2 + k3) - t.y) * (Math.exp(k1 * t.x1 + k2 * t.x2 + k3) / Math.pow((Math.exp(k1 * t.x1 + k2 * t.x2 + k3) + 1), 2));
	}

	public double dx1(Triple t) {
		return k1 * (g(k1 * t.x1 + k2 * t.x2 + k3) - t.y) * (Math.exp(k1 * t.x1 + k2 * t.x2 + k3) / Math.pow((Math.exp(k1 * t.x1 + k2 * t.x2 + k3) + 1), 2));
	}

	public double dx2(Triple t) {
		return k2 * (g(k1 * t.x1 + k2 * t.x2 + k3) - t.y) * (Math.exp(k1 * t.x1 + k2 * t.x2 + k3) / Math.pow((Math.exp(k1 * t.x1 + k2 * t.x2 + k3) + 1), 2));
	}

	public void work() throws FileNotFoundException {
		Scanner sc = new Scanner(new FileInputStream("chips.txt"));
		List<Triple> ls = new ArrayList<Triple>();
		while (sc.hasNext()) {
			String s = sc.nextLine();
			String[] ss = s.split(",");
			ls.add(new Triple(Double.parseDouble(ss[0]), 
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
				t1 += step * (dx1(t) / learn.size());
				t2 += step * (dx2(t) / learn.size());
				t3 += step * (dy(t) / learn.size());
			}

			k1 += t1;
			k2 += t2;
			k3 += t3;
		}
		test(check);
	}

	private void test(List<Triple> check) {
		double count = 0;
		double errCount = 0;
		for (Triple t: check) {
			double res = Math.round(f(t));
			if (res != t.y)
				++errCount;
			++count;
			System.out.println(t.toString() + "__" + Math.round(f(t))+ "___" + f(t));
		}
		System.out.println("ErrRate = " + errCount/count);
	}

	int i = 0;
	private boolean stop(List<Triple> learn) {	
		if (i++ > 60000) {
			step /= 3;
			//return true;
		}
		if (step < 1e-7)
			return true;
		
		double count = 0;
		double errCount = 0;
		for (Triple t: learn) {
			double res = Math.round(f(t));
			if (res != t.y)
				++errCount;
			++count;
		}
		final double errRate = errCount/count;
		System.err.println("tErrRate = " + errRate);
		return errRate < eps;
	}

	private double f(Triple t) {
		return g(k1 * t.x1 + k2 * t.x2 + k3);
	}

}
