import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;


public class Main {

	public static double dist(Point.Double x, Point.Double y)  {
		return Math.sqrt(Math.pow(x.x - y.x, 2) + Math.pow(x.y - y.x, 2));
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Scanner sc = new Scanner(new FileInputStream("chips.txt"));
		List<Map.Entry<Point.Double, Integer>> ls = new ArrayList<Map.Entry<Point.Double, Integer>>(); 
		while (sc.hasNext()) {
			String s = sc.nextLine();
			String[] ss = s.split(",");
			ls.add(new AbstractMap.SimpleEntry<Point.Double, Integer>(
					new Point.Double(Double.parseDouble(ss[0]), Double.parseDouble(ss[1])), 
					Integer.parseInt(ss[2])));
		}
		Collections.shuffle(ls, new Random());
		final int k = 4;
		final int m = ls.size() * 2 / 3 + 1;
		List<Map.Entry<Point.Double, Integer>> learn = ls.subList(0, m);
		List<Map.Entry<Point.Double, Integer>> check = ls.subList(m, ls.size());
		
		int errCount = 0;
		for (final Map.Entry<Point.Double, Integer> checkEntry : check) {
			Collections.sort(learn, new Comparator<Map.Entry<Point.Double, Integer>>() {
				@Override
				public int compare(Entry<java.awt.geom.Point2D.Double, Integer> o1,
								Entry<java.awt.geom.Point2D.Double, Integer> o2) {
					if (dist(o1.getKey(), checkEntry.getKey()) > dist(o2.getKey(), checkEntry.getKey()))
						return 1;
					if (dist(o1.getKey(), checkEntry.getKey()) < dist(o2.getKey(), checkEntry.getKey()))
						return -1;
					return 0;
				}
			});
			int sum = 0;
			for (Map.Entry<Point.Double, Integer> learnEntry : learn.subList(0, k)) {
				sum += learnEntry.getValue();
			}
			int res = sum > k/2 ? 1 : 0;
			PrintStream ps = System.out;
			if (res != checkEntry.getValue()) {
				ps = System.err;
				++errCount;
			}
			ps.printf("%f %f exp %d res %d\n", checkEntry.getKey().x, checkEntry.getKey().y, 
					checkEntry.getValue(), sum > k/2 ? 1 : 0);			
		}
		System.out.println("Precision = " + (1.0 - ((double)errCount / (double)check.size())));
		sc.close();
	}

}

/*
Read: cross-validation, F1-measure, F-beta, Precision recall, 

*/