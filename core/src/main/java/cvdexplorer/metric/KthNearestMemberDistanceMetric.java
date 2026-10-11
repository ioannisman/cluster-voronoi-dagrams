package cvdexplorer.metric;

import cvdexplorer.model.ClusterMember;
import cvdexplorer.geometry.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Distance from a query point to the k-th nearest member (1-based order statistic of distances).
 */
public final class KthNearestMemberDistanceMetric implements ClusterMetric {
    private final int k;

    public KthNearestMemberDistanceMetric(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be at least 1");
        }
        this.k = k;
    }

    @Override
    public Result evaluate(Vector point, List<ClusterMember> members) {
        if (members.isEmpty() || members.size() < k) {
            return new Result(Double.POSITIVE_INFINITY, -1);
        }

        record DistanceWithIndex(double distance, int index) implements Comparable<DistanceWithIndex> {
            @Override
            public int compareTo(DistanceWithIndex o) {
                return Double.compare(distance, o.distance);
            }
        }

        List<DistanceWithIndex> distances = new ArrayList<>(members.size());
        for (int i = 0; i < members.size(); i++) {
            distances.add(new DistanceWithIndex(members.get(i).distanceTo(point), i));
        }
        Collections.sort(distances);
        DistanceWithIndex kth = distances.get(k - 1);
        return new Result(kth.distance(), kth.index());
    }
}
