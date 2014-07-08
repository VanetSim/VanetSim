/** 
 * Classes and methods for the aggregation anonymization process. The basis of the aggregations process is a clustering process.
 * The clustering process is based on the k-means algorithm of author Jens Spehr.
 * Link: http://www.rob.cs.tu-bs.de/content/04-teaching/06-interactive/Kmeans/Kmeans.html
 */
package vanetsim.anonymizer;

import java.awt.Point;
import java.io.File;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Vector;
import javax.swing.*;
import vanetsim.localization.Messages;

public class AnonClustering extends AnonymityMethod {

	Vector CarPositionList; // ArrayList containing all carpositions as vectors
							// (red out from data)
	Vector ClusterList; // ArrayList containing all positions of the cluster
						// centroids
	Random rand; // Random to place the cluster centroids randomly in the first
					// step
	boolean clusteringDone; // abort criterion --> if true the clustering
							// process is finished
	int step; // current step of the clustering process
	boolean parsing = false;
	String aggMethod;

	public AnonClustering(Data data, JLabel info) {
		super(data, info);
	}

	@Override
	// check if the user-supplied values ​​are correct. If so set the number of
	// clusters (numOfClusters) and proceed with the aggregateClusters() method.
	public int anonymize(String[] params) {
		if (!isValid(params)) {
			return -1;
		}
		rand = new Random();
		int numOfClusters = Integer.parseInt(params[0]);
		aggregateClusters(numOfClusters);
		return 0;
	}

	// method to check if the user-supplied values ​​are correct
	private boolean isValid(String[] params) {
		int numOfClusters = Integer.parseInt(params[0]);
		if (numOfClusters < 10 || numOfClusters > 100) {
			info.setText(Messages
					.getString("AnonymizeDataDialog.anonymityMethod.aggregation.fail1"));
			return false;
		}
		info.setText("");
		return true;
	}

	public void aggregateClusters(int numOfClusters) {
		ClusterList = new Vector();
		CarPositionList = new Vector();
		step = -1;
		clusteringSteps(numOfClusters);
	}

	// All steps in the clustering process
	public void clusteringSteps(int numOfClusters) {
		if (parsing == false)
			parsing(numOfClusters);
		else if (step == 0)
			step1(numOfClusters);
		else if (step == 1)
			step2(numOfClusters);
		else if (step == 2)
			step3(numOfClusters);
		else if ((step == 3) && (clusteringDone == true)) {
			step4(numOfClusters);
		} else if ((step == 3) && (clusteringDone == false))
			step2(numOfClusters);
	}

	// sets the values of the CarPositionList based on the values of "data"
	public void parsing(int numOfClusters) {
		int total = data.getRowCount();

		for (int i = 0; i < total; i++) {
			setCarPositionList((Integer) data.getValueAt(i, 1),
					(Integer) data.getValueAt(i, 2));
		}
		parsing = true;
		step = 0;
		clusteringSteps(numOfClusters);
	}

	public boolean setCarPositionList(int x, int y) {

		CarPosition s = new CarPosition();

		s.clusterId = -1;
		s.x = x;
		s.y = y;
		CarPositionList.addElement(s);

		return true;
	}

	// step1 places the cluster centroids randomly
	public void step1(int numOfClusters) {
		clusteringDone = false;
		int totalCars = CarPositionList.size();
		boolean ch[] = new boolean[totalCars];
		for (int i = 0; i < totalCars; i++)
			ch[i] = false;
		for (int i = 0; i < numOfClusters;) {
			CarPosition s;
			Cluster p = new Cluster();
			int r = Math.abs(rand.nextInt() % totalCars);
			if (ch[r] == false) {
				s = (CarPosition) CarPositionList.elementAt(r);
				p.x = s.x;
				p.y = s.y;

				p.clusterId = i + 1;
				ClusterList.addElement(p);

				ch[r] = true;
				i++;
			}
		}
		step = 1;
		clusteringSteps(numOfClusters);
	}

	// step2 assigns each CarPosition to a Cluster
	public void step2(int numOfClusters) {
		CarPosition s;
		Cluster p;
		int totalCars = CarPositionList.size();
		for (int i = 0; i < totalCars; i++) {
			s = (CarPosition) CarPositionList.elementAt(i);

			int totalClusters = ClusterList.size();
			int min = 0;
			double dist_min = 99999999999.9; // Ursprünglich: double dist_min =
												// 99999999.9;
			for (int j = 0; j < totalClusters; j++) {
				p = (Cluster) ClusterList.elementAt(j);

				double dist = Point.distance(s.x, s.y, p.x, p.y);
				if (dist < dist_min) {
					dist_min = dist;
					min = j;
				}
			}
			p = (Cluster) ClusterList.elementAt(min);
			s.clusterId = p.clusterId;

		}
		step = 2;
		clusteringSteps(numOfClusters);
	}

	// step3 - Recalculation of the cluster centroids
	public void step3(int numOfClusters) {
		Cluster p;
		CarPosition s;
		Point m = new Point();
		int changes = 0;
		int totalClusters = ClusterList.size();

		for (int j = 0; j < totalClusters; j++) {
			p = (Cluster) ClusterList.elementAt(j);

			m.x = 0;
			m.y = 0;
			int Count = 0;
			int totalCars = CarPositionList.size();
			for (int i = 0; i < totalCars; i++) {
				s = (CarPosition) CarPositionList.elementAt(i);

				if (s.clusterId == p.clusterId) {
					m.x += s.x;
					m.y += s.y;

					Count++;
				}
			}
			if (Count > 0) {
				changes += Point.distance(p.x, p.y, m.x / Count, m.y / Count);
				Point pt = new Point();
				pt.x = p.x;
				pt.y = p.y;
				p.x = m.x / Count;
				p.y = m.y / Count;

			}

		}
		if (changes < 100) {
			clusteringDone = true;

		}
		step = 3;
		clusteringSteps(numOfClusters);
	}

	// step4 - last step. When the clustering process is done, override the
	// positions (X and Y) off the cars with the positions of the assigned
	// clusters
	public void step4(int numOfClusters) {
		CarPosition a;
		Cluster p = null;
		int totalCars = CarPositionList.size();
		int totalClusters = ClusterList.size();

		for (int i = 0; i < totalCars; i++) {
			a = (CarPosition) CarPositionList.elementAt(i);
			for (int j = 0; j < totalClusters; j++) {
				p = (Cluster) ClusterList.elementAt(j);
				if (a.clusterId == p.clusterId) {
					data.setValueAt(i, 1, p.x);
					data.setValueAt(i, 2, p.y);
				}
			}

		}

		// Save the cluster centroid positions in a file, to match the
		// anonymized car positions with the cluster centroid positions. The
		// user has to match them manually.
//		try {
//			File file = new File("C:\\temp\\Clusters.txt");
//			file.getParentFile().mkdirs();
//
//			PrintWriter pr = new PrintWriter(file);
//
//			for (int l = 0; l < numOfClusters; l++) {
////				String s = (String) ClusterList.get(l).toString();
//				pr.println(ClusterList.get(l));
//			}
//
//			pr.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("No such file exists.");
//		}
	}

	class Cluster {
		int clusterId;
		int x;
		int y;

		@Override
		public String toString() {
			return "ClusterId: " + clusterId + " x: " + x + " y: " + y;
		}

	}

	class CarPosition {
		int clusterId;
		int x;
		int y;
	}

}