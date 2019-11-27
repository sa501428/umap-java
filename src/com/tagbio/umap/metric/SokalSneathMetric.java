/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap.metric;

/**
 * Sokal Sneath distance.
 */
public class SokalSneathMetric extends Metric {

  public static final SokalSneathMetric SINGLETON = new SokalSneathMetric();

  private SokalSneathMetric() {
    super(false);
  }

  @Override
  public double distance(final float[] x, final float[] y) {
    int numTrueTrue = 0;
    int numNotEqual = 0;
    for (int i = 0; i < x.length; ++i) {
      final boolean xTrue = x[i] != 0;
      final boolean yTrue = y[i] != 0;
      if (xTrue && yTrue) {
        ++numTrueTrue;
      }
      if (xTrue != yTrue) {
        ++numNotEqual;
      }
    }
    return numNotEqual == 0.0 ? 0.0 : numNotEqual / (0.5 * numTrueTrue + numNotEqual);
  }
}
