package com.tagbio.umap.metric;

/**
 * Mahalanobis distance.
 */
public class MahalanobisMetric extends Metric {

  private final float[][] mV;

  public MahalanobisMetric(final float[][] v) {
    super(false);
    mV = v;
  }

  @Override
  public double distance(final float[] x, final float[] y) {
    double result = 0.0;
    final float[] diff = new float[x.length];
    for (int i = 0; i < x.length; ++i) {
      diff[i] = x[i] - y[i];
    }
    for (int i = 0; i < x.length; ++i) {
      double tmp = 0.0;
      for (int j = 0; j < x.length; ++j) {
        tmp += mV[i][j] * diff[j];
      }
      result += tmp * diff[i];
    }
    return Math.sqrt(result);
  }
}

