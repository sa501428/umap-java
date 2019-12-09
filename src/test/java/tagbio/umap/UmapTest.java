/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package tagbio.umap;

//from umap.umap_ import (
//    INT32_MAX,
//    INT32_MIN,
//    make_forest,
//    rptree_leaf_array,
//    nearest_neighbors,
//    smooth_knn_dist,
//    fuzzy_simplicial_set,
//    UMAP,
//)
//from umap.utils import deheap_sort
//from umap.nndescent import (
//    make_initialisations,
//    make_initialized_nnd_search,
//    initialise_search,
//)
//import umap.validation as valid
//import umap.sparse as spdist
//import umap.distances as dist
//from sklearn import datasets
//from nose import SkipTest
//from functools import wraps
//from tempfile import mkdtemp
//from scipy.stats import mode
//from sklearn.cluster import KMeans
//from sklearn.manifold.t_sne import trustworthiness
//from sklearn.preprocessing import StandardScaler, normalize
//from sklearn.utils import shuffle
//from sklearn.neighbors import KDTree, BallTree
//from sklearn.metrics import pairwise_distances, adjusted_rand_score
//from sklearn.utils.testing import (
//    assert_equal,
//    assert_array_equal,
//    assert_almost_equal,
//    assert_array_almost_equal,
//    assert_raises,
//    assert_in,
//    assert_not_in,
//    assert_no_warnings,
//)
//from sklearn.utils.estimator_checks import check_estimator
//from scipy import stats
//from scipy import sparse
//from scipy.spatial import distance
//import numpy as np
//import os.path
//from nose.tools import assert_greater_equal
//from nose.tools import assert_less
//
//"""
//Tests for UMAP to ensure things are working as expected.
//"""
//import warnings
//
//warnings.filterwarnings("ignore", category=UserWarning)

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import tagbio.umap.metric.PrecomputedMetric;

import junit.framework.TestCase;

/**
 * Tests the corresponding class.
 */
public class UmapTest extends TestCase {

  public void testIris() throws IOException {
    final Data data = new IrisData();
    final Umap umap = new Umap();
    umap.setInit("random");
    umap.setVerbose(true);
    final float[][] d = data.getData();
    final long start = System.currentTimeMillis();
    final float[][] matrix = umap.fitTransform(d);
    System.out.println("UMAP time: " + Math.round((System.currentTimeMillis() - start) / 1000.0) + " s");
    //System.out.println(matrix);
    assertEquals(150, matrix.length);
    assertEquals(2, matrix[0].length);
  }

  public void testDigits() throws IOException {
    final Data data = new DigitData();
    final Umap umap = new Umap();
    umap.setInit("random");
    umap.setVerbose(true);
    umap.setNumberComponents(3);
    final float[][] d = data.getData();
    final long start = System.currentTimeMillis();
    final float[][] matrix = umap.fitTransform(d);
    System.out.println("UMAP time: " + Math.round((System.currentTimeMillis() - start) / 1000.0) + " s");
    assertEquals(1797, matrix.length);
    assertEquals(3, matrix[0].length);
//    final int[] classIndexes = data.getSampleClassIndex();
//    for (int r = 0; r < matrix.rows(); ++r) {
//      System.out.println(matrix.get(r, 0) + " " + matrix.get(r, 1) + " " + matrix.get(r, 2) + " " + classIndexes[r]);
//    }
  }

//  public void testMammoth() throws IOException {
//    final Data data = new MammothData();
//    final float[][] d = data.getData();
//    final long start = System.currentTimeMillis();
//    final Umap umap = new Umap();
//    umap.setInit("random");
//    umap.setVerbose(true);
//    umap.setNumberComponents(2);
//    umap.setNumberNearestNeighbours(100);
//    final float[][] matrix = umap.fitTransform(d);
//    System.out.println("UMAP time: " + Math.round((System.currentTimeMillis() - start) / 1000.0) + " s");
//    assertEquals(10000, matrix.length);
//    assertEquals(2, matrix[0].length);
//    final int[] classIndexes = data.getSampleClassIndex();
//    for (int r = 0; r < matrix.length; ++r) {
//      System.out.println(matrix[r][0] + " " + matrix[r][1] + " " + classIndexes[r]);
//    }
//  }

//  public void testGenes() throws IOException {
//    final Data data = new GeneData();
//    final Umap umap = new Umap();
//    umap.setInit("random");
//    umap.setVerbose(true);
//    umap.setNumberComponents(2);
//    final float[][] d = data.getData();
//    final long start = System.currentTimeMillis();
//    final float[][] matrix = umap.fitTransform(d);
//    System.out.println("UMAP time: " + Math.round((System.currentTimeMillis() - start) / 1000.0) + " s");
//    assertEquals(5902, matrix.length);
//    assertEquals(2, matrix[0].length);
//    final int[] classIndexes = data.getSampleClassIndex();
//    for (int r = 0; r < matrix.length; ++r) {
//      System.out.println(matrix[r][0] + " " + matrix[r][1] + " " + classIndexes[r]);
//    }
//  }

  private int[] primes(final int m) {
    final List<Integer> primes = new ArrayList<>();
    final boolean[] state = new boolean[m];
    Arrays.fill(state, true);
    state[0] = false;
    state[1] = false;
    for (int k = 2; k < m; ++k) {
      if (state[k]) {
        primes.add(k);
      }
      for (int j = k; j < m; j += k) {
        state[j] = false;
      }
    }
    final int[] res = new int[primes.size()];
    for (int k = 0; k < res.length; ++k) {
      res[k] = primes.get(k);
    }
    return res;
  }

  private float[][] factorizations(final int[] omega, final int m) {
    final int[] primes = primes(m);
    final float[][] data = new float[omega.length][primes.length + 1];
    for (int k = 0; k < omega.length; ++k) {
      int s = k;
      for (int j = 0; j < primes.length && s > 1; ++j) {
        final int p = primes[j];
        while (s % p == 0) {
          ++data[k][j];
          ++omega[k];
          s /= p;
        }
      }
      data[k][primes.length] = s;
    }
    return data;
  }

  public void testPrimes() {
    //final int[] omega = new int[1000000];
    //final float[][] d = factorizations(omega, 10000);
    final int[] omega = new int[1000];
    final float[][] d = factorizations(omega, 100);
    final long start = System.currentTimeMillis();
    final Umap umap = new Umap();
    umap.setInit("random");
    umap.setVerbose(true);
    umap.setNumberComponents(2);
    final float[][] matrix = umap.fitTransform(d);
    System.out.println("UMAP time: " + Math.round((System.currentTimeMillis() - start) / 1000.0) + " s");
//    for (int r = 0; r < matrix.length; ++r) {
//      System.out.println(matrix[r][0] + " " + matrix[r][1] + " " + omega[r]);
//    }
  }

  public void testFindABParams() throws IOException {
    final Data data = new IrisData();
    final Umap umap = new Umap();
    umap.setInit("random");

    for (float spread : new float[]{-1.234F, 0.0F, 2.0F, 0.49F, 1.51F}) {
      umap.setSpread(spread);
      try {
        umap.fitTransform(data.getData());
        fail("Accepted bad spread " + spread);
      } catch (IllegalArgumentException iae) {
        assertTrue(iae.getMessage().contains("spread"));
      }
    }
    umap.setSpread(1.0F);
    for (float dist : new float[]{-1.234F, -0.01F, 1.01F, 1.51F}) {
      try {
        umap.setMinDist(dist);
        umap.fitTransform(data.getData());
        fail("Accepted bad dist " + dist);
      } catch (IllegalArgumentException iae) {
        assertTrue(iae.getMessage(), iae.getMessage().contains("ist"));
      }
    }
  }

//np.random.seed(42)
//spatial_data = np.random.randn(10, 20)
//spatial_data = np.vstack(
//    [spatial_data, np.zeros((2, 20))]
//)  # Add some all zero data for corner case test
//binary_data = np.random.choice(a=[False, True], size=(10, 20), p=[0.66, 1 - 0.66])
//binary_data = np.vstack(
//    [binary_data, np.zeros((2, 20), dtype="bool")]
//)  # Add some all zero data for corner case test
//sparse_spatial_data = sparse.csr_matrix(spatial_data * binary_data)
//sparse_binary_data = sparse.csr_matrix(binary_data)

  private static Matrix makeNNData() {
    final Random r = new Random();
    final float[][] data = new float[1002][5]; // last two rows remain 0 to test corner cases
    for (int k = 0; k < 1000; ++k) {
      for (int j = 0; j < data[k].length; ++j) {
        data[k][j] = r.nextFloat();
      }
    }
    return new DefaultMatrix(data);
  }
  private static final Matrix nn_data = makeNNData();

//binary_nn_data = np.random.choice(a=[False, True], size=(1000, 5), p=[0.66, 1 - 0.66])
//binary_nn_data = np.vstack(
//    [binary_nn_data, np.zeros((2, 5), dtype="bool")]
//)  # Add some all zero data for corner case test
//sparse_nn_data = sparse.csr_matrix(nn_data * binary_nn_data)
//
//iris = datasets.load_iris()
//iris_selection = np.random.choice([True, False], 150, replace=True, p=[0.75, 0.25])
//
//
//def spatial_check(metric):
//    dist_matrix = pairwise_distances(spatial_data, metric=metric)
//    # scipy is bad sometimes
//    if metric == "braycurtis":
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 0.0
//    if metric in ("cosine", "correlation"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 1.0
//        # And because distance between all zero vectors should be zero
//        dist_matrix[10, 11] = 0.0
//        dist_matrix[11, 10] = 0.0
//    dist_function = dist.named_distances[metric]
//    test_matrix = np.array(
//        [
//            [
//                dist_function(spatial_data[i], spatial_data[j])
//                for j in range(spatial_data.shape[0])
//            ]
//            for i in range(spatial_data.shape[0])
//        ]
//    )
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric {}".format(metric),
//    )
//
//
//def binary_check(metric):
//    dist_matrix = pairwise_distances(binary_data, metric=metric)
//    if metric in ("jaccard", "dice", "sokalsneath", "yule"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 0.0
//    if metric in ("kulsinski", "russellrao"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 0.0
//        # And because distance between all zero vectors should be zero
//        dist_matrix[10, 11] = 0.0
//        dist_matrix[11, 10] = 0.0
//    dist_function = dist.named_distances[metric]
//    test_matrix = np.array(
//        [
//            [
//                dist_function(binary_data[i], binary_data[j])
//                for j in range(binary_data.shape[0])
//            ]
//            for i in range(binary_data.shape[0])
//        ]
//    )
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric {}".format(metric),
//    )
//
//
//def sparse_spatial_check(metric):
//    if metric in spdist.sparse_named_distances:
//        dist_matrix = pairwise_distances(sparse_spatial_data.todense(), metric=metric)
//    if metric in ("braycurtis", "dice", "sokalsneath", "yule"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 0.0
//    if metric in ("cosine", "correlation", "kulsinski", "russellrao"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 1.0
//        # And because distance between all zero vectors should be zero
//        dist_matrix[10, 11] = 0.0
//        dist_matrix[11, 10] = 0.0
//
//    dist_function = spdist.sparse_named_distances[metric]
//    if metric in spdist.sparse_need_n_features:
//        test_matrix = np.array(
//            [
//                [
//                    dist_function(
//                        sparse_spatial_data[i].indices,
//                        sparse_spatial_data[i].data,
//                        sparse_spatial_data[j].indices,
//                        sparse_spatial_data[j].data,
//                        sparse_spatial_data.shape[1],
//                    )
//                    for j in range(sparse_spatial_data.shape[0])
//                ]
//                for i in range(sparse_spatial_data.shape[0])
//            ]
//        )
//    else:
//        test_matrix = np.array(
//            [
//                [
//                    dist_function(
//                        sparse_spatial_data[i].indices,
//                        sparse_spatial_data[i].data,
//                        sparse_spatial_data[j].indices,
//                        sparse_spatial_data[j].data,
//                    )
//                    for j in range(sparse_spatial_data.shape[0])
//                ]
//                for i in range(sparse_spatial_data.shape[0])
//            ]
//        )
//
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Sparse distances don't match " "for metric {}".format(metric),
//    )
//
//
//def sparse_binary_check(metric):
//    if metric in spdist.sparse_named_distances:
//        dist_matrix = pairwise_distances(sparse_binary_data.todense(), metric=metric)
//    if metric in ("jaccard", "dice", "sokalsneath", "yule"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 0.0
//    if metric in ("kulsinski", "russellrao"):
//        dist_matrix[np.where(~np.isfinite(dist_matrix))] = 1.0
//        # And because distance between all zero vectors should be zero
//        dist_matrix[10, 11] = 0.0
//        dist_matrix[11, 10] = 0.0
//
//    dist_function = spdist.sparse_named_distances[metric]
//    if metric in spdist.sparse_need_n_features:
//        test_matrix = np.array(
//            [
//                [
//                    dist_function(
//                        sparse_binary_data[i].indices,
//                        sparse_binary_data[i].data,
//                        sparse_binary_data[j].indices,
//                        sparse_binary_data[j].data,
//                        sparse_binary_data.shape[1],
//                    )
//                    for j in range(sparse_binary_data.shape[0])
//                ]
//                for i in range(sparse_binary_data.shape[0])
//            ]
//        )
//    else:
//        test_matrix = np.array(
//            [
//                [
//                    dist_function(
//                        sparse_binary_data[i].indices,
//                        sparse_binary_data[i].data,
//                        sparse_binary_data[j].indices,
//                        sparse_binary_data[j].data,
//                    )
//                    for j in range(sparse_binary_data.shape[0])
//                ]
//                for i in range(sparse_binary_data.shape[0])
//            ]
//        )
//
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Sparse distances don't match " "for metric {}".format(metric),
//    )
//
//
//# Transform isn't stable under batching; hard to opt out of this.
//@SkipTest
//def test_scikit_learn_compatibility():
//    check_estimator(UMAP)

//  public void testNnDescentNeighborAccuracy() {
//    final Object[] nn = Umap.nearestNeighbors(nn_data, 10, EuclideanMetric.SINGLETON, false, new Random(), false);
//    final int[][] knnIndices = (int[][]) nn[0];
//    final float[][] knnDists = (float[][]) nn[1];
//
//    KDTree tree = KDTree(nn_data);
//    true_indices = tree.query(nn_data, 10, return_distance = False);
//
//    double num_correct = 0.0;
//    for (int i = 0; i < nn_data.rows(); ++i) {
//      num_correct += np.sum(np.in1d(true_indices[i], knn_indices[i]));
//    }
//
//    double percent_correct = num_correct / (spatial_data.shape[0] * 10);
//    assert_greater_equal(      percent_correct,      0.99,      "NN-descent did not get 99% ""accuracy on nearest neighbors"      );
//  }


//def test_angular_nn_descent_neighbor_accuracy():
//    knn_indices, knn_dists, _ = nearest_neighbors(
//        nn_data, 10, "cosine", {}, True, np.random
//    )
//
//    angular_data = normalize(nn_data, norm="l2")
//    tree = KDTree(angular_data)
//    true_indices = tree.query(angular_data, 10, return_distance=False)
//
//    num_correct = 0.0
//    for i in range(nn_data.shape[0]):
//        num_correct += np.sum(np.in1d(true_indices[i], knn_indices[i]))
//
//    percent_correct = num_correct / (spatial_data.shape[0] * 10)
//    assert_greater_equal(
//        percent_correct,
//        0.99,
//        "NN-descent did not get 99% " "accuracy on nearest neighbors",
//    )
//
//
//def test_sparse_nn_descent_neighbor_accuracy():
//    knn_indices, knn_dists, _ = nearest_neighbors(
//        sparse_nn_data, 10, "euclidean", {}, False, np.random
//    )
//
//    tree = KDTree(sparse_nn_data.todense())
//    true_indices = tree.query(sparse_nn_data.todense(), 10, return_distance=False)
//
//    num_correct = 0.0
//    for i in range(nn_data.shape[0]):
//        num_correct += np.sum(np.in1d(true_indices[i], knn_indices[i]))
//
//    percent_correct = num_correct / (spatial_data.shape[0] * 10)
//    assert_greater_equal(
//        percent_correct,
//        0.99,
//        "Sparse NN-descent did not get " "99% accuracy on nearest " "neighbors",
//    )
//
//
//def test_sparse_angular_nn_descent_neighbor_accuracy():
//    knn_indices, knn_dists, _ = nearest_neighbors(
//        sparse_nn_data, 10, "cosine", {}, True, np.random
//    )
//
//    angular_data = normalize(sparse_nn_data, norm="l2").toarray()
//    tree = KDTree(angular_data)
//    true_indices = tree.query(angular_data, 10, return_distance=False)
//
//    num_correct = 0.0
//    for i in range(nn_data.shape[0]):
//        num_correct += np.sum(np.in1d(true_indices[i], knn_indices[i]))
//
//    percent_correct = num_correct / (spatial_data.shape[0] * 10)
//    assert_greater_equal(
//        percent_correct,
//        0.99,
//        "NN-descent did not get 99% " "accuracy on nearest neighbors",
//    )


//  public void testSmoothKnnDistL1Norms() {
//    final Object[] nn = Umap.nearestNeighbors(nn_data, 10, EuclideanMetric.SINGLETON, false, new Random(), false);
//    final int[][] knnIndices = (int[][]) nn[0];
//    final float[][] knnDists = (float[][]) nn[1];
//    final float[][] sigmasRhos = Umap.smoothKnnDist(knnDists, 10, 1);
//    final float[] sigmas = sigmasRhos[0];
//    final float[] rhos = sigmasRhos[1];
//    final Matrix shifted_dists = new DefaultMatrix(knnDists).subtract(MathUtils.promoteTranspose(rhos));
//    for (int k = 0; k < shifted_dists.rows(); ++k) {
//      for (int j = 0; j < shifted_dists.cols(); ++j) {
//        if (shifted_dists.get(k, j) < 0) {
//          shifted_dists.set(k, j, 0);
//        }
//      }
//    }
//    vals = np.exp(-(shifted_dists / MathUtils.promoteTranspose(sigmas)));
//    norms = np.sum(vals, axis = 1);
//
//    assert_array_almost_equal(      norms,      1.0 + np.log2(10) * np.ones(norms.shape[0]),      decimal = 3,      err_msg = "Smooth knn-dists does not give expected norms"    );
//  }


//def test_nn_descent_neighbor_accuracy_callable_metric():
//    knn_indices, knn_dists, _ = nearest_neighbors(
//        nn_data, 10, dist.euclidean, {}, False, np.random
//    )
//
//    tree = KDTree(nn_data)
//    true_indices = tree.query(nn_data, 10, return_distance=False)
//
//    num_correct = 0.0
//    for i in range(nn_data.shape[0]):
//        num_correct += np.sum(np.in1d(true_indices[i], knn_indices[i]))
//
//    percent_correct = num_correct / (spatial_data.shape[0] * 10)
//    assert_greater_equal(
//        percent_correct,
//        0.99,
//        "NN-descent did not get 99% "
//        "accuracy on nearest neighbors with callable metric",
//    )
//
//
//def test_smooth_knn_dist_l1norms_w_connectivity():
//    knn_indices, knn_dists, _ = nearest_neighbors(
//        nn_data, 10, "euclidean", {}, False, np.random
//    )
//    sigmas, rhos = smooth_knn_dist(knn_dists, 10, local_connectivity=1.75)
//    shifted_dists = knn_dists - rhos[:, np.newaxis]
//    shifted_dists[shifted_dists < 0.0] = 0.0
//    vals = np.exp(-(shifted_dists / sigmas[:, np.newaxis]))
//    norms = np.sum(vals, axis=1)
//
//    assert_array_almost_equal(
//        norms,
//        1.0 + np.log2(10) * np.ones(norms.shape[0]),
//        decimal=3,
//        err_msg="Smooth knn-dists does not give expected"
//        "norms for local_connectivity=1.75",
//    )
//
//    # sigmas, rhos = smooth_knn_dist(knn_dists, 10, local_connectivity=0.75)
//    # shifted_dists = knn_dists - rhos[:, np.newaxis]
//    # shifted_dists[shifted_dists < 0.0] = 0.0
//    # vals = np.exp(-(shifted_dists / sigmas[:, np.newaxis]))
//    # norms = np.sum(vals, axis=1)
//    # diff = np.mean(norms) - (1.0 + np.log2(10))
//    #
//    # assert_almost_equal(diff, 0.0, decimal=1,
//    #                     err_msg='Smooth knn-dists does not give expected'
//    #                             'norms for local_connectivity=0.75')
//
//
//def test_nn_search():
//    train = nn_data[100:]
//    test = nn_data[:100]
//    (knn_indices, knn_dists, rp_forest) = nearest_neighbors(
//        train, 10, "euclidean", {}, False, np.random
//    )
//
//    graph = fuzzy_simplicial_set(
//        nn_data,
//        10,
//        np.random,
//        "euclidean",
//        {},
//        knn_indices,
//        knn_dists,
//        False,
//        1.0,
//        1.0,
//        False,
//    )
//
//    search_graph = sparse.lil_matrix((train.shape[0], train.shape[0]), dtype=np.int8)
//    search_graph.rows = knn_indices
//    search_graph.data = (knn_dists != 0).astype(np.int8)
//    search_graph = search_graph.maximum(search_graph.transpose()).tocsr()
//
//    random_init, tree_init = make_initialisations(dist.euclidean, ())
//    search = make_initialized_nnd_search(dist.euclidean, ())
//
//    rng_state = np.random.randint(INT32_MIN, INT32_MAX, 3).astype(np.int64)
//    init = initialise_search(
//        rp_forest, train, test, int(10 * 3), random_init, tree_init, rng_state
//    )
//    result = search(train, search_graph.indptr, search_graph.indices, init, test)
//
//    indices, dists = deheap_sort(result)
//    indices = indices[:, :10]
//
//    tree = KDTree(train)
//    true_indices = tree.query(test, 10, return_distance=False)
//
//    num_correct = 0.0
//    for i in range(test.shape[0]):
//        num_correct += np.sum(np.in1d(true_indices[i], indices[i]))
//
//    percent_correct = num_correct / (test.shape[0] * 10)
//    assert_greater_equal(
//        percent_correct,
//        0.99,
//        "Sparse NN-descent did not get " "99% accuracy on nearest " "neighbors",
//    )
//
//
//def test_euclidean():
//    spatial_check("euclidean")
//
//
//def test_manhattan():
//    spatial_check("manhattan")
//
//
//def test_chebyshev():
//    spatial_check("chebyshev")
//
//
//def test_minkowski():
//    spatial_check("minkowski")
//
//
//def test_hamming():
//    spatial_check("hamming")
//
//
//def test_canberra():
//    spatial_check("canberra")
//
//
//def test_braycurtis():
//    spatial_check("braycurtis")
//
//
//def test_cosine():
//    spatial_check("cosine")
//
//
//def test_correlation():
//    spatial_check("correlation")
//
//
//def test_jaccard():
//    binary_check("jaccard")
//
//
//def test_matching():
//    binary_check("matching")
//
//
//def test_dice():
//    binary_check("dice")
//
//
//def test_kulsinski():
//    binary_check("kulsinski")
//
//
//def test_rogerstanimoto():
//    binary_check("rogerstanimoto")
//
//
//def test_russellrao():
//    binary_check("russellrao")
//
//
//def test_sokalmichener():
//    binary_check("sokalmichener")
//
//
//def test_sokalsneath():
//    binary_check("sokalsneath")
//
//
//def test_yule():
//    binary_check("yule")
//
//
//def test_sparse_euclidean():
//    sparse_spatial_check("euclidean")
//
//
//def test_sparse_manhattan():
//    sparse_spatial_check("manhattan")
//
//
//def test_sparse_chebyshev():
//    sparse_spatial_check("chebyshev")
//
//
//def test_sparse_minkowski():
//    sparse_spatial_check("minkowski")
//
//
//def test_sparse_hamming():
//    sparse_spatial_check("hamming")
//
//
//def test_sparse_canberra():
//    sparse_spatial_check("canberra")
//
//
//def test_sparse_cosine():
//    sparse_spatial_check("cosine")
//
//
//def test_sparse_correlation():
//    sparse_spatial_check("correlation")
//
//
//def test_sparse_jaccard():
//    sparse_binary_check("jaccard")
//
//
//def test_sparse_matching():
//    sparse_binary_check("matching")
//
//
//def test_sparse_dice():
//    sparse_binary_check("dice")
//
//
//def test_sparse_kulsinski():
//    sparse_binary_check("kulsinski")
//
//
//def test_sparse_rogerstanimoto():
//    sparse_binary_check("rogerstanimoto")
//
//
//def test_sparse_russellrao():
//    sparse_binary_check("russellrao")
//
//
//def test_sparse_sokalmichener():
//    sparse_binary_check("sokalmichener")
//
//
//def test_sparse_sokalsneath():
//    sparse_binary_check("sokalsneath")
//
//
//def test_seuclidean():
//    v = np.abs(np.random.randn(spatial_data.shape[1]))
//    dist_matrix = pairwise_distances(spatial_data, metric="seuclidean", V=v)
//    test_matrix = np.array(
//        [
//            [
//                dist.standardised_euclidean(spatial_data[i], spatial_data[j], v)
//                for j in range(spatial_data.shape[0])
//            ]
//            for i in range(spatial_data.shape[0])
//        ]
//    )
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric seuclidean",
//    )
//
//
//def test_weighted_minkowski():
//    v = np.abs(np.random.randn(spatial_data.shape[1]))
//    dist_matrix = pairwise_distances(spatial_data, metric="wminkowski", w=v, p=3)
//    test_matrix = np.array(
//        [
//            [
//                dist.weighted_minkowski(spatial_data[i], spatial_data[j], v, p=3)
//                for j in range(spatial_data.shape[0])
//            ]
//            for i in range(spatial_data.shape[0])
//        ]
//    )
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric weighted_minkowski",
//    )
//
//
//def test_mahalanobis():
//    v = np.cov(np.transpose(spatial_data))
//    dist_matrix = pairwise_distances(spatial_data, metric="mahalanobis", VI=v)
//    test_matrix = np.array(
//        [
//            [
//                dist.mahalanobis(spatial_data[i], spatial_data[j], v)
//                for j in range(spatial_data.shape[0])
//            ]
//            for i in range(spatial_data.shape[0])
//        ]
//    )
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric mahalanobis",
//    )
//
//
//def test_haversine():
//    tree = BallTree(spatial_data[:, :2], metric="haversine")
//    dist_matrix, _ = tree.query(spatial_data[:, :2], k=spatial_data.shape[0])
//    test_matrix = np.array(
//        [
//            [
//                dist.haversine(spatial_data[i, :2], spatial_data[j, :2])
//                for j in range(spatial_data.shape[0])
//            ]
//            for i in range(spatial_data.shape[0])
//        ]
//    )
//    test_matrix.sort(axis=1)
//    assert_array_almost_equal(
//        test_matrix,
//        dist_matrix,
//        err_msg="Distances don't match " "for metric haversine",
//    )
//
//
//def test_umap_sparse_trustworthiness():
//    embedding = UMAP(n_neighbors=10).fit_transform(sparse_nn_data[:100])
//    trust = trustworthiness(sparse_nn_data[:100].toarray(), embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.92,
//        "Insufficiently trustworthy embedding for"
//        "sparse test dataset: {}".format(trust),
//    )
//
//
//def test_umap_trustworthiness_on_iris():
//    data = iris.data
//    embedding = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit_transform(data)
//    trust = trustworthiness(iris.data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.97,
//        "Insufficiently trustworthy embedding for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_umap_trustworthiness_on_iris_random_init():
//    data = iris.data
//    embedding = UMAP(
//        n_neighbors=10, min_dist=0.01, random_state=42, init="random"
//    ).fit_transform(data)
//    trust = trustworthiness(iris.data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.95,
//        "Insufficiently trustworthy embedding for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_supervised_umap_trustworthiness_on_iris():
//    data = iris.data
//    embedding = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit_transform(
//        data, iris.target
//    )
//    trust = trustworthiness(iris.data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.97,
//        "Insufficiently trustworthy embedding for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_semisupervised_umap_trustworthiness_on_iris():
//    data = iris.data
//    target = iris.target.copy()
//    target[25:75] = -1
//    embedding = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit_transform(
//        data, target
//    )
//    trust = trustworthiness(iris.data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.97,
//        "Insufficiently trustworthy embedding for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_initialized_umap_trustworthiness_on_iris():
//    data = iris.data
//    embedding = UMAP(
//        n_neighbors=10, min_dist=0.01, init=data[:, 2:], random_state=42
//    ).fit_transform(data, iris.target)
//    trust = trustworthiness(iris.data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.97,
//        "Insufficiently trustworthy embedding for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_umap_transform_on_iris():
//    data = iris.data[iris_selection]
//    fitter = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit(data)
//
//    new_data = iris.data[~iris_selection]
//    embedding = fitter.transform(new_data)
//
//    trust = trustworthiness(new_data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.89,
//        "Insufficiently trustworthy transform for" "iris dataset: {}".format(trust),
//    )
//
//
//def test_umap_transform_on_iris_modified_dtype():
//    data = iris.data[iris_selection]
//    fitter = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit(data)
//    fitter.embedding_ = fitter.embedding_.astype(np.float64)
//
//    new_data = iris.data[~iris_selection]
//    embedding = fitter.transform(new_data)
//
//    trust = trustworthiness(new_data, embedding, 10)
//    assert_greater_equal(
//        trust,
//        0.89,
//        "Insufficiently trustworthy transform for" "iris dataset: {}".format(trust),
//    )
//
//
//# # This test is currently to expensive to run when turning
//# # off numba JITting to detect coverage.
//# @SkipTest
//# def test_umap_regression_supervision(): # pragma: no cover
//#     boston = datasets.load_boston()
//#     data = boston.data
//#     embedding = UMAP(n_neighbors=10,
//#                      min_dist=0.01,
//#                      target_metric='euclidean',
//#                      random_state=42).fit_transform(data, boston.target)
//
//
//def test_blobs_cluster():
//    data, labels = datasets.make_blobs(n_samples=500, n_features=10, centers=5)
//    embedding = UMAP().fit_transform(data)
//    assert_equal(adjusted_rand_score(labels, KMeans(5).fit_predict(embedding)), 1.0)
//
//
//def test_multi_component_layout():
//    data, labels = datasets.make_blobs(
//        100, 2, centers=5, cluster_std=0.5, center_box=[-20, 20], random_state=42
//    )
//
//    true_centroids = np.empty((labels.max() + 1, data.shape[1]), dtype=np.float64)
//
//    for label in range(labels.max() + 1):
//        true_centroids[label] = data[labels == label].mean(axis=0)
//
//    true_centroids = normalize(true_centroids, norm="l2")
//
//    embedding = UMAP(n_neighbors=4).fit_transform(data)
//    embed_centroids = np.empty((labels.max() + 1, data.shape[1]), dtype=np.float64)
//    embed_labels = KMeans(n_clusters=5).fit_predict(embedding)
//
//    for label in range(embed_labels.max() + 1):
//        embed_centroids[label] = data[embed_labels == label].mean(axis=0)
//
//    embed_centroids = normalize(embed_centroids, norm="l2")
//
//    error = np.sum((true_centroids - embed_centroids) ** 2)
//
//    assert_less(error, 15.0, msg="Multi component embedding to far astray")
//
//
//def test_bad_too_large_min_dist():
//    u = UMAP(min_dist=2.0)
//    # a RuntimeWarning about division by zero in a,b curve fitting is expected
//    # caught and ignored for this test
//    with warnings.catch_warnings():
//        warnings.filterwarnings("ignore", category=RuntimeWarning)
//        assert_raises(ValueError, u.fit, nn_data)
//

  public void testNegativeOp() {
    final Umap umap = new Umap();
    try {
      umap.setSetOpMixRatio(-1.0F);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testTooLargeOp() {
    final Umap umap = new Umap();
    try {
      umap.setSetOpMixRatio(1.5F);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeMinDist() {
    final Umap umap = new Umap();
    try {
      umap.setMinDist(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeComponents() {
    final Umap umap = new Umap();
    try {
      umap.setNumberComponents(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testTooSmallNeighbours() {
    final Umap umap = new Umap();
    try {
      umap.setNumberNearestNeighbours(0);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testBadMetric() {
    final Umap umap = new Umap();
    try {
      umap.setMetric("no-such-metric");
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeLearningRate() {
    final Umap umap = new Umap();
    try {
      umap.setLearningRate(-1.5F);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeRepulsion() {
    final Umap umap = new Umap();
    try {
      umap.setRepulsionStrength(-0.5F);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeSampleRate() {
    final Umap umap = new Umap();
    try {
      umap.setNegativeSampleRate(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeEpochs() {
    final Umap umap = new Umap();
    try {
      umap.setNumberEpochs(-2);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

//def test_bad_init():
//    u = UMAP(init="foobar")
//    assert_raises(ValueError, u.fit, nn_data)
//
//
//def test_bad_numeric_init():
//    u = UMAP(init=42)
//    assert_raises(ValueError, u.fit, nn_data)
//
//
//def test_bad_matrix_init():
//    u = UMAP(init=np.array([[0, 0, 0], [0, 0, 0]]))
//    assert_raises(ValueError, u.fit, nn_data)
//
//
//def test_negative_target_nneighbors():
//    u = UMAP(target_n_neighbors=1)
//    assert_raises(ValueError, u.fit, nn_data)
//
//
//def test_umap_bad_nn():
//    assert_raises(ValueError, nearest_neighbors, nn_data, 10, 42, {}, False, np.random)
//
//
//def test_umap_bad_nn_sparse():
//    assert_raises(
//        ValueError,
//        nearest_neighbors,
//        sparse_nn_data,
//        10,
//        "seuclidean",
//        {},
//        False,
//        np.random,
//    )
//
//
//def test_too_many_neighbors_warns():
//    u = UMAP(a=1.2, b=1.75, n_neighbors=2000, n_epochs=11, init="random")
//    u.fit(nn_data[:100,])
//    assert_equal(u._a, 1.2)
//    assert_equal(u._b, 1.75)
//
//
//def test_umap_fit_params():
//    # x and y are required to be the same length
//    u = UMAP()
//    x = np.random.uniform(0, 1, (256, 10))
//    y = np.random.randint(10, size=(257,))
//    assert_raises(ValueError, u.fit, x, y)
//
//    u = UMAP()
//    x = np.random.uniform(0, 1, (256, 10))
//    y = np.random.randint(10, size=(255,))
//    assert_raises(ValueError, u.fit, x, y)
//
//    u = UMAP()
//    x = np.random.uniform(0, 1, (256, 10))
//    assert_raises(ValueError, u.fit, x, [])
//
//    u = UMAP()
//    x = np.random.uniform(0, 1, (256, 10))
//    y = np.random.randint(10, size=(256,))
//    res = u.fit(x, y)
//    assert isinstance(res, UMAP)
//
//    u = UMAP()
//    x = np.random.uniform(0, 1, (256, 10))
//    res = u.fit(x)
//    assert isinstance(res, UMAP)
//
//
//def test_umap_transform_embedding_stability():
//    """Test that transforming data does not alter the learned embeddings
//
//    Issue #217 describes how using transform to embed new data using a
//    trained UMAP transformer causes the fitting embedding matrix to change
//    in cases when the new data has the same number of rows as the original
//    training data.
//    """
//
//    data = iris.data[iris_selection]
//    fitter = UMAP(n_neighbors=10, min_dist=0.01, random_state=42).fit(data)
//    original_embedding = fitter.embedding_.copy()
//
//    # The important point is that the new data has the same number of rows
//    # as the original fit data
//    new_data = np.random.random(data.shape)
//    embedding = fitter.transform(new_data)
//
//    assert_array_equal(original_embedding,
//                       fitter.embedding_,
//                       "Transforming new data changed the original embeddings")
//
//    # Example from issue #217
//    a = np.random.random((1000, 10))
//    b = np.random.random((1000, 5))
//
//    umap = UMAP()
//    u1 = umap.fit_transform(a[:, :5])
//    u1_orig = u1.copy()
//    assert_array_equal(u1_orig, umap.embedding_)
//
//    u2 = umap.transform(b)
//    assert_array_equal(u1_orig, umap.embedding_)

  private void assertArrayEquals(final double[] expected, final float[] actual) {
    if (expected.length != actual.length) {
      fail("Lengths mismatch: expected=" + expected.length + " actual=" + actual.length);
    }
    for (int k = 0; k < expected.length; ++k) {
      assertEquals("Mismatch at index " + k + " expected=" + expected[k] + " actual=" + actual[k], expected[k], actual[k], 1e-6);
    }
  }

  private void assertArrayEquals(final double[][] expected, final float[][] actual) {
    if (expected.length != actual.length) {
      fail("Lengths mismatch: expected=" + expected.length + " actual=" + actual.length);
    }
    for (int k = 0; k < expected.length; ++k) {
      assertArrayEquals(expected[k], actual[k]);
    }
  }

  public void testSmoothKnnDist() throws IOException {
    final Matrix distances = new IrisData(true).getDistances();
    //System.out.println(distances.toStringNumpy());
    final float[][] smooth21 = Umap.smoothKnnDist(distances.toArray(), 2, 1);
    // Comparison values from Python
    assertArrayEquals(new double[] {0.00326393, 0.00322133, 0.00330938, 0.0026791, 0.00247916, 0.00266279, 0.00299635, 0.00269514, 0.00280051, 0.00712167}, smooth21[0]);
    assertArrayEquals(new double[] {0.5385164, 0.5385164, 0.509902, 4.003748, 3.6166282, 4.1641326, 4.853864, 4.1904655, 4.4170127, 6.3450766}, smooth21[1]);
    final float[][] smooth42 = Umap.smoothKnnDist(distances.toArray(), 4, 2);
    assertArrayEquals(new double[] {0.71514893, 0.25, 0.25, 0.0026791, 0.00247916, 0.00266279, 0.00299635, 0.00269514, 0.00280051, 0.00712167}, smooth42[0]);
    assertArrayEquals(new double[] {0.509902, 0.30000022, 0.30000022, 4.0963397, 3.6864617, 4.236744, 4.9020405, 4.134005, 4.402272, 5.916925}, smooth42[1]);
  }

  public void testNearestNeighborsPrecomputed() throws IOException {
    final Matrix distances = new IrisData(true).getDistances();
    final IndexedDistances id = Umap.nearestNeighbors(distances, 2, PrecomputedMetric.SINGLETON, false, null, false);
    // Comparison values from Python
    assertTrue(Arrays.deepEquals(new int[][] {{0, 2}, {1, 2}, {2, 1}, {3, 5}, {4, 3}, {5, 3}, {6, 5}, {7, 8}, {8, 7}, {9, 2}}, id.getIndices()));
    assertArrayEquals(new double[][] {{0, 0.509902}, {0, 0.30000022}, {0, 0.30000022}, {0, 0.26457536}, {0, 0.64031225}, {0, 0.26457536}, {0, 0.86023235}, {0, 0.51961535}, {0, 0.51961535}, {0, 5.8360944}}, id.getDistances());
    assertTrue(id.getForest().isEmpty());
  }

  public void testComputeMembershipStrengths() throws IOException {
    final Matrix distances = new IrisData(true).getDistances();
    final float[][] sigmaRhos = Umap.smoothKnnDist(distances.toArray(), 2, 1);
    final IndexedDistances id = Umap.nearestNeighbors(distances, 2, PrecomputedMetric.SINGLETON, false, null, false);
    final CooMatrix m = Umap.computeMembershipStrengths(id.getIndices(), id.getDistances(), sigmaRhos[0], sigmaRhos[1], distances.rows(), distances.cols());
    // Comparison values from Python
    // The next three lines are order dependent in the CooMatrix, so not ideal for comparison
//    assertTrue(Arrays.equals(new int[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9}, m.mRow));
//    assertTrue(Arrays.equals(new int[]{0, 2, 1, 2, 1, 2, 3, 5, 3, 4, 3, 5, 5, 6, 7, 8, 7, 8, 2, 9}, m.mCol));
//    assertArrayEquals(new double[]{0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0}, m.mData);
    assertArrayEquals(new double[][]{
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    }, m.toArray());
  }

  public void testFuzzySimplicialSet() throws IOException {
    final Matrix distances = new IrisData(true).getDistances();
    final Matrix m = Umap.fuzzySimplicialSet(distances, 2, null, PrecomputedMetric.SINGLETON, null, null, false, 1, 1, false);
    // Comparison values from Python
    /*
    [[0.         0.         0.99999222 0.         0.         0.          0.         0.         0.         0.        ]
     [0.         0.         1.         0.         0.         0.          0.         0.         0.         0.        ]
     [0.99999222 1.         0.         0.         0.         0.          0.         0.         0.         0.99999443]
     [0.         0.         0.         0.         0.99999023 1.          0.         0.         0.         0.        ]
     [0.         0.         0.         0.99999023 0.         0.          0.         0.         0.         0.        ]
     [0.         0.         0.         1.         0.         0.          0.99999344 0.         0.         0.        ]
     [0.         0.         0.         0.         0.         0.99999344  0.         0.         0.         0.        ]
     [0.         0.         0.         0.         0.         0.          0.         0.         1.         0.        ]
     [0.         0.         0.         0.         0.         0.          0.         1.         0.         0.        ]
     [0.         0.         0.99999443 0.         0.         0.          0.         0.         0.         0.        ]]
     */
    assertArrayEquals(new double[][]{
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0},
      {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0},
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0},
      {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
    }, m.toArray());
  }

  public void testFastIntersection() throws IOException {
    final CooMatrix distances = new IrisData(true).getDistances().toCoo();
    final float[] target = new float[distances.rows()];
    for (int k = 0; k < target.length; ++k) {
      target[k] = k % 3;
    }
    distances.fastIntersection(target, 1.0F, 1.0e8F);
    // Comparison values from Python
    assertArrayEquals(new double[][]{
      {0, 0, 0, 4.003748, 0, 0, 4.853864, 0, 0, 6.3450766},
      {0, 0, 0, 0, 3.6864617, 0, 0, 4.134005, 0, 0},
      {0, 0, 0, 0, 0, 4.4158807, 0, 0, 4.544227, 0},
      {4.003748, 0, 0, 0, 0, 0, 1.1, 0, 0, 9.126335},
      {0, 3.6864617, 0, 0, 0, 0, 0, 1.2165527, 0, 0},
      {0, 0, 4.4158807, 0, 0, 0, 0, 0, 1.4662877, 0},
      {4.853864, 0, 0, 1.1, 0, 0, 0, 0, 0, 9.481561},
      {0, 4.134005, 0, 0, 1.2165527, 0, 0, 0, 0, 0},
      {0, 0, 4.544227, 0, 0, 1.4662877, 0, 0, 0, 0},
      {6.3450766, 0, 0, 9.126335, 0, 0, 9.481561, 0, 0, 0},
    }, distances.toArray());
  }

  public void testMakeEpochsPerSample() {
    assertEquals("[84.0, 42.0, 10.5, 1.0]", Arrays.toString(Umap.makeEpochsPerSample(new float[] {0.5F, 1, 4, 42}, 10)));
  }

  public void testClip() {
    assertEquals(0.0F, Umap.clip(0F));
    assertEquals(1.5F, Umap.clip(1.5F));
    assertEquals(4.0F, Umap.clip(4));
    assertEquals(4.0F, Umap.clip(4.01F));
    assertEquals(4.0F, Umap.clip(Float.POSITIVE_INFINITY));
    assertEquals(-4.0F, Umap.clip(-4));
    assertEquals(-4.0F, Umap.clip(-4.01F));
    assertEquals(-4.0F, Umap.clip(Float.NEGATIVE_INFINITY));
  }
}