/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap.metric;

/**
 * Yule distance.
 */
public class YuleMetric extends Metric {

  public static final YuleMetric SINGLETON = new YuleMetric();

  private YuleMetric() {
    super(false);
  }

  @Override
  public double distance(final float[] x, final float[] y) {
    int numTrueTrue = 0;
    int numTrueFalse = 0;
    int numFalseTrue = 0;
    for (int i = 0; i < x.length; ++i) {
      final boolean xTrue = x[i] != 0;
      final boolean yTrue = y[i] != 0;
      if (xTrue && yTrue) {
        ++numTrueTrue;
      }
      if (xTrue && !yTrue) {
        ++numTrueFalse;
      }
      if (!xTrue && yTrue) {
        ++numFalseTrue;
      }
    }
    int numFalseFalse = x.length - numTrueTrue - numTrueFalse - numFalseTrue;

    return numTrueFalse == 0 || numFalseTrue == 0 ? 0.0 : (2.0 * numTrueFalse * numFalseTrue) / (float) (numTrueTrue * numFalseFalse + numTrueFalse * numFalseTrue);
  }
}
