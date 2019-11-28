/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap;

import java.util.Arrays;

/**
 * Base class for matrices.
 * @author Sean A. Irvine
 * @author Richard Littin
 */
abstract class Matrix {

  /** Array containing the dimensions of the matrix <code>(rows, columns)</code>. */
  protected int[] mShape;

  Matrix(final int... shape) {
    this.mShape = shape;
    for (int s : shape) {
      if (s < 0) {
        throw new IllegalArgumentException("Illegal dimension specification: " + s);
      }
    }
  }

  abstract float get(final int row, final int col);

  abstract void set(final int row, final int col, final float val);

  /**
   * Get the number of rows in the matrix.
   * @return number of rows
   */
  int rows() {
    return mShape[0];
  }

  /**
   * Get the number of columns in the matrix
   * @return number of cols
   */
  int cols() {
    return mShape[1];
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (int row = 0; row < rows(); ++row) {
      for (int col = 0; col < cols(); ++col) {
        if (col > 0) {
          sb.append(',');
        }
        sb.append(get(row, col));
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  public String toStringNumpy() {
    final StringBuilder sb = new StringBuilder("np.matrix([");
    for (int row = 0; row < rows(); ++row) {
      sb.append('[');
      for (int col = 0; col < cols(); ++col) {
        if (col > 0) {
          sb.append(',');
        }
        sb.append(get(row, col));
      }
      sb.append("],");
    }
    sb.append("])");
    return sb.toString();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Matrix)) {
      return false;
    }
    final Matrix m = (Matrix) obj;
    if (!Arrays.equals(shape(), m.shape())) {
      return false;
    }
    for (int i = 0; i < rows(); ++i) {
      for (int j = 0; j < cols(); ++j) {
        if (get(i, j) != m.get(i, j)) {
          return false;
        }
      }
    }
    return true;
  }

  int[] shape() {
    return mShape;
  }

  long length() {
    long len = 1;
    for (final int dim : shape()) {
      len *= dim;
    }
    return len;
  }

  Matrix transpose() {
    final float[][] res = new float[cols()][rows()];
    for (int i = 0; i < rows(); ++i) {
      for (int j = 0; j < cols(); ++j) {
        res[j][i] = get(i, j);
      }
    }
    return new DefaultMatrix(res);
  }

  Matrix add(final Matrix m) {
    //System.out.println("add: " + getClass().getSimpleName() + " + " + m.getClass().getSimpleName());
    if (!Arrays.equals(mShape, m.mShape)) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    final DefaultMatrix res = new DefaultMatrix(mShape);
    final int rows = rows();
    final int cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        res.set(i, j, get(i, j) + m.get(i, j));
      }
    }
    return res;
  }

  Matrix subtract(final Matrix m) {
    if (!Arrays.equals(mShape, m.mShape)) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    final DefaultMatrix res = new DefaultMatrix(mShape);
    final int rows = rows();
    final int cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        res.set(i, j, get(i, j) - m.get(i, j));
      }
    }
    return res;
  }

  Matrix multiply(final float x) {
    final DefaultMatrix res = new DefaultMatrix(mShape);
    final int rows = rows();
    final int cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        res.set(i, j, get(i, j) * x);
      }
    }
    return res;
  }

  Matrix multiply(final Matrix m) {
    if (cols() != m.rows()) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    final int rows = rows();
    final int cols = m.cols();
    final Matrix res = new DefaultMatrix(rows, cols);
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        float sum = 0;
        for (int k = 0; k < cols(); ++k) {
          sum += get(i, k) * m.get(k, j);
        }
        res.set(i, j, sum);
      }
    }
    return res;
  }

  Matrix hadamardMultiply(final Matrix m) {
    if (!Arrays.equals(mShape, m.mShape)) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    final DefaultMatrix res = new DefaultMatrix(mShape);
    final int rows = rows();
    final int cols = cols();
    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        res.set(i, j, get(i, j) * m.get(i, j));
      }
    }
    return res;
  }

  /**
   * Compute the Hadamard product of this matrix with its own transpose.
   * @return <code>this circ this^T</code>
   */
  Matrix hadamardMultiplyTranspose() {
    if (rows() != cols()) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    return hadamardMultiply(transpose());
  }

  /**
   * Compute the addition of this matrix with its own transpose.
   * @return <code>this + this^T</code>
   */
  Matrix addTranspose() {
    if (rows() != cols()) {
      throw new IllegalArgumentException("Incompatible matrix sizes");
    }
    return add(transpose());
  }

  private int countZeros() {
    int cnt = 0;
    for (int r = 0; r < rows(); ++r) {
      for (int c = 0; c < cols(); ++c) {
        if (get(r, c) == 0) {
          ++cnt;
        }
      }
    }
    return cnt;
  }

  Matrix eliminateZeros() {
    throw new UnsupportedOperationException();
  }

  CooMatrix toCoo() {
    final int len = (int) (length() - countZeros());
    final int[] row = new int[len];
    final int[] col = new int[len];
    final float[] data = new float[len];
    for (int k = 0, r = 0; r < rows(); ++r) {
      for (int c = 0; c < cols(); ++c) {
        final float x = get(r, c);
        if (x != 0) {
          row[k] = r;
          col[k] = c;
          data[k++] = x;
        }
      }
    }
    return new CooMatrix(data, row, col, mShape);
  }

  CsrMatrix toCsr() {
    final int len = (int) (length() - countZeros());
    final int[] indptr = new int[rows() + 1];
    final int[] indices = new int[len];
    final float[] data = new float[len];
    for (int k = 0, r = 0; r < rows(); ++r) {
      indptr[r] = k;
      for (int c = 0; c < cols(); ++c) {
        final float x = get(r, c);
        if (x != 0) {
          indices[k] = c;
          data[k++] = x;
        }
      }
    }
    indptr[rows()] = len;
    return new CsrMatrix(data, indptr, indices, mShape);
  }

  Matrix copy() {
    // todo this should be a copy of the matrix of same type -- generics on params?
    throw new UnsupportedOperationException();
  }

  float[][] toArray() {
    final float[][] res = new float[rows()][cols()];
    for (int r = 0; r < rows(); ++r) {
      for (int c = 0; c < cols(); ++c) {
        res[r][c] = get(r, c);
      }
    }
    return res;
  }

  /**
   * Return a copy of 1-dimensional row slice from the matrix.
   * @param row row number to get
   * @return row
   */
  float[] row(int row) {
    final float[] data = new float[cols()];
    for (int k = 0; k < data.length; ++k) {
      data[k] = get(row, k);
    }
    return data;
  }

  Matrix max(final Matrix other) {
    if (!Arrays.equals(shape(), other.shape())) {
      throw new IllegalArgumentException("Incompatible sizes");
    }
    final DefaultMatrix m = new DefaultMatrix(shape());
    for (int k = 0; k < rows(); ++k) {
      for (int j = 0; j < cols(); ++j) {
        m.set(k, j, Math.max(get(k, j), other.get(k, j)));
      }
    }
    return m;
  }

  Matrix take(int[] indicies) {
    // todo return elements from array along selected axes
    throw new UnsupportedOperationException();
  }

  static Matrix eye(int n, int m, int k) {
    // todo
    /*
    Return a 2-D array with ones on the diagonal and zeros elsewhere.
    N : int
      Number of rows in the output.
    M : int, optional
      Number of columns in the output. If None, defaults to `N`.
    k : int, optional
      Index of the diagonal: 0 (the default) refers to the main diagonal,
      a positive value refers to an upper diagonal, and a negative value
      to a lower diagonal.
    */
    throw new UnsupportedOperationException();
  }

  Matrix dot(Matrix x) {
    // todo dot product
    throw new UnsupportedOperationException();
  }

  Matrix inv() {
    // todo invert
    throw new UnsupportedOperationException();
  }

  Matrix triu(int k) {
    // todo Upper triangle of an array.
    // Return a copy of a matrix with the elements below the `k`-th diagonal zeroed.
    // may want tri and tril versions as well
    throw new UnsupportedOperationException();
  }

  Float max() {
    // todo maximum value in matrix
    // this might only be wanted for float[], not matrix...
    // min as well?
    throw new UnsupportedOperationException();
  }

  Float sum() {
    // todo sum of values in matrix
    // return double?
    throw new UnsupportedOperationException();
  }

}
