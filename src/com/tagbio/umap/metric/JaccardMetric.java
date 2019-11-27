/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap.metric;

/**
 * Dice distance.
 * @author Sean A. Irvine
 */
public class JaccardMetric extends Metric {

  public static final JaccardMetric SINGLETON = new JaccardMetric();

  private JaccardMetric() {
    super(true);
  }

  @Override
  public double distance(final float[] x, final float[] y) {
     int numNonZero = 0;
     int numEqual = 0;
     for (int i = 0; i < x.length; ++i) {
       final boolean xTrue = x[i] != 0;
       final boolean yTrue = y[i] != 0;
       numNonZero += xTrue || yTrue ? 1 : 0;
       numEqual += xTrue && yTrue ? 1 : 0;
     }

     if (numNonZero == 0) {
       return 0;
     } else {
       return (numNonZero - numEqual) / (double) numNonZero;
     }
  }
}
