/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap.metric;

/**
 * Special indicator for categorical data.
 * @author Sean A. Irvine
 */
public class CategoricalMetric extends Metric {

  public static final CategoricalMetric SINGLETON = new CategoricalMetric();

  private CategoricalMetric() {
    super(false);
  }

  @Override
  public double distance(final float[] x, final float[] y) {
    throw new IllegalStateException();
  }
}
