/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.Strike;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This is a very basic attempt at PLAT-2215 (Local Volatility Calibration from Forward PDE). This does not converge. It is not clear if this is because the method is
 * unsound or the implementation is faulty 
 */
public class LocalVolFittingTest {
  private final MatrixAlgebra _algebra = new ColtMatrixAlgebra();
  private static ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
  private static NonLinearLeastSquareWithPenalty NLLS = new NonLinearLeastSquareWithPenalty();
  //  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  //  ConjugateDirectionVectorMinimizer MINIMIZER = new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER);
  // ConjugateGradientVectorMinimizer MINIMIZER = new ConjugateGradientVectorMinimizer(LINE_MINIMIZER);
  // MultiDirectionalSimplexMinimizer MINIMIZER = new MultiDirectionalSimplexMinimizer();
  GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE, Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE,
      new FlatExtrapolator1D(), new FlatExtrapolator1D());
  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_COND_PROVIDER = new InitialConditionsProvider();
  private static final ConvectionDiffusionPDESolver SOLVER = new ThetaMethodFiniteDifference(0.5, true);
  final RandomEngine random = new MersenneTwister64();

  private final BasisFunctionGenerator _generator = new BasisFunctionGenerator();

  private static final double IMP_VOL = 0.4;
  private static final double[] EXPIRIES = new double[] {0.1, 0.2, 0.3, 0.5, 0.75, 1, 1.25, 1.5, 2 };
  private static final double[][] STRIKES = new double[][] { {0.7, 0.8, 0.8, 1.0, 1.1, 1.2, 1.3 }, {0.7, 0.8, 0.8, 1.0, 1.1, 1.2, 1.3 }, {0.7, 0.8, 0.8, 1.0, 1.1, 1.2, 1.3 },
      {0.7, 0.8, 0.8, 1.0, 1.1, 1.2, 1.3 }, {0.6, 0.8, 1.0, 1.2, 1.4 }, {0.5, 0.75, 1.0, 1.25, 1.5 }, {0.5, 0.75, 1.0, 1.25, 1.5 }, {0.5, 0.75, 1.0, 1.25, 1.5 },
      {0.4, 0.7, 1.0, 1.3, 1.6 } };

  @Test
      (enabled = false)
      public void test() {
    final int nExp = EXPIRIES.length;
    int temp = 0;
    for (int i = 0; i < nExp; i++) {
      temp += STRIKES[i].length;
    }
    final int totalStrikes = temp;

    final ForwardCurve fwdCurve = new ForwardCurve(1.0);
    double xL = 0.0;
    double xH = 4;
    final BoundaryCondition lower = new DirichletBoundaryCondition(1.0, xL);
    final BoundaryCondition upper = new NeumannBoundaryCondition(0.0, xH, false);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(xL, xH, 1.0, 40, 0.05);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, 2.0, 30, 0.2);
    final PDEGrid1D pdeGrid = new PDEGrid1D(timeMesh, spaceMesh);
    final Function1D<Double, Double> initialCond = INITIAL_COND_PROVIDER.getForwardCallPut(true);
    double[] xa = new double[] {0, 0 };
    double[] xb = new double[] {2.0, xH };
    int[] nKnots = new int[] {3, 10 };
    int[] degree = new int[] {3, 3 };
    final double[] lambda = new double[] {1e-8, 1e-5 };
    final int[] differenceOrder = new int[] {2, 2 };
    int dim = xa.length;
    int[] sizes = new int[dim];
    for (int i = 0; i < dim; i++) {
      sizes[i] = nKnots[i] + degree[i] - 1;
    }

    final List<Function1D<double[], Double>> bSplines = _generator.generateSet(xa, xb, nKnots, degree);
    final int nWeights = bSplines.size();

    PSplineFitter psf = new PSplineFitter();
    DoubleMatrix2D ma = (DoubleMatrix2D) _algebra.scale(psf.getPenaltyMatrix(sizes, differenceOrder[0], 0), lambda[0]);
    for (int i = 1; i < dim; i++) {
      if (lambda[i] > 0.0) {
        final DoubleMatrix2D d = psf.getPenaltyMatrix(sizes, differenceOrder[i], i);
        ma = (DoubleMatrix2D) _algebra.add(ma, _algebra.scale(d, lambda[i]));
      }
    }
    DoubleMatrix2D penalty = ma;//(DoubleMatrix2D) _algebra.multiply(_algebra.getTranspose(ma), ma);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> volFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        double[] weights = new double[nWeights];
        for (int i = 0; i < nWeights; i++) {
          weights[i] = TRANSFORM.inverseTransform(x.getEntry(i));
        }

        LocalVolatilitySurfaceMoneyness localVolSurface = getLocalVol(bSplines, fwdCurve, weights);
        BlackVolatilitySurfaceMoneyness impVol = solveForwardPDE(fwdCurve, lower, upper, pdeGrid, initialCond, localVolSurface);

        double[] vols = new double[totalStrikes];
        @SuppressWarnings("unused")
        double chi2 = 0;
        int index = 0;
        for (int i = 0; i < nExp; i++) {
          double t = EXPIRIES[i];
          final int n = STRIKES[i].length;
          for (int j = 0; j < n; j++) {
            double k = STRIKES[i][j];
            vols[index] = impVol.getVolatility(t, k);
            chi2 += FunctionUtils.square(vols[index] - IMP_VOL);
            index++;
          }
        }
        DoubleMatrix1D debug = new DoubleMatrix1D(vols);
        //  System.out.println(chi2);
        return debug;
      }

    };

    double[] start = new double[nWeights];
    // Arrays.fill(start, 0.4);
    for (int i = 0; i < nWeights; i++) {
      start[i] = TRANSFORM.transform(IMP_VOL + 0.05 * (random.nextDouble() - 0.5));
    }

    //  DoubleMatrix1D res = MINIMIZER.minimize(objective, new DoubleMatrix1D(start));
    DoubleMatrix1D observed = new DoubleMatrix1D(totalStrikes, IMP_VOL);
    LeastSquareResults res = NLLS.solve(observed, volFunc, new DoubleMatrix1D(start), penalty);
    System.out.println(res);

    double[] weights = new double[nWeights];
    for (int i = 0; i < nWeights; i++) {
      weights[i] = TRANSFORM.inverseTransform(res.getFitParameters().getEntry(i));
    }

    LocalVolatilitySurfaceMoneyness lv = getLocalVol(bSplines, fwdCurve, weights);
    PDEUtilityTools.printSurface("lv", lv.getSurface(), 0.01, 2.0, 0.3, 3.0);

    BlackVolatilitySurfaceMoneyness iv = solveForwardPDE(fwdCurve, lower, upper, pdeGrid, initialCond, lv);
    PDEUtilityTools.printSurface("imp vol", iv.getSurface(), 0.01, 2.0, 0.3, 3.0);
  }

  /**
   * A single 
   */
  @Test(enabled = false)
  public void test2() {

    final int nExp = EXPIRIES.length;
    int temp = 0;
    for (int i = 0; i < nExp; i++) {
      temp += STRIKES[i].length;
    }
    final int totalStrikes = temp;

    final List<Function1D<Double, Double>> bSplines = _generator.generateSet(0.0, 6.0, 30, 3);
    final int nKnots = bSplines.size();

    final ForwardCurve fwdCurve = new ForwardCurve(1.0);
    double xL = 0.0;
    double xH = 6;
    final BoundaryCondition lower = new DirichletBoundaryCondition(1.0, xL);
    final BoundaryCondition upper = new NeumannBoundaryCondition(0.0, xH, false);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(xL, xH, 1.0, 40, 0.05);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, 2.0, 30, 0.2);
    final PDEGrid1D pdeGrid = new PDEGrid1D(timeMesh, spaceMesh);
    final Function1D<Double, Double> initialCond = INITIAL_COND_PROVIDER.getForwardCallPut(true);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> volFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {

        double[] weights = new double[nKnots];
        for (int i = 0; i < nKnots; i++) {
          weights[i] = TRANSFORM.inverseTransform(x.getEntry(i));
        }

        LocalVolatilitySurfaceMoneyness localVolSurface = getLocalVol1D(bSplines, fwdCurve, weights);

        BlackVolatilitySurfaceMoneyness impVol = solveForwardPDE(fwdCurve, lower, upper, pdeGrid, initialCond, localVolSurface);

        double[] vols = new double[totalStrikes];
        @SuppressWarnings("unused")
        double chi2 = 0;
        int index = 0;
        for (int i = 0; i < nExp; i++) {
          double t = EXPIRIES[i];
          final int n = STRIKES[i].length;
          for (int j = 0; j < n; j++) {
            double k = STRIKES[i][j];
            vols[index] = impVol.getVolatility(t, k);
            chi2 += FunctionUtils.square(vols[index] - IMP_VOL);
            index++;
          }
        }
        DoubleMatrix1D debug = new DoubleMatrix1D(vols);
        //  System.out.println(chi2);
        return debug;
      }

    };

    PSplineFitter psf = new PSplineFitter();
    DoubleMatrix2D penalty = (DoubleMatrix2D) _algebra.scale(psf.getPenaltyMatrix(nKnots, 2), 0.01);

    double[] start = new double[nKnots];
    // Arrays.fill(start, 0.4);
    for (int i = 0; i < nKnots; i++) {
      start[i] = TRANSFORM.transform(IMP_VOL + 0.05 * (random.nextDouble() - 0.5));
    }

    //  DoubleMatrix1D res = MINIMIZER.minimize(objective, new DoubleMatrix1D(start));
    DoubleMatrix1D observed = new DoubleMatrix1D(totalStrikes, IMP_VOL);
    LeastSquareResults res = NLLS.solve(observed, volFunc, new DoubleMatrix1D(start), penalty);
    System.out.println(res);

  }

  @Test(enabled = false)
  public void test3() {

    final double fwd = 1.0;

    final int n = 3;
    final double[] sigma = new double[] {0.2, 0.5, 2.0 };
    final double[] w = new double[] {0.8, 0.15, 0.05 };
    final double[] f = new double[n];
    f[0] = 1.05 * fwd;
    f[1] = 0.9 * fwd;
    double sum = 0;
    for (int i = 0; i < n - 1; i++) {
      sum += w[i] * f[i];
    }
    f[n - 1] = (fwd - sum) / w[n - 1];
    Validate.isTrue(f[n - 1] > 0);

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        final double expiry = x[0];
        final double k = x[1];
        final boolean isCall = k > fwd;
        double price = 0;
        for (int i = 0; i < n; i++) {
          price += w[i] * BlackFormulaRepository.price(f[i], k, expiry, sigma[i], isCall);
        }
        return BlackFormulaRepository.impliedVolatility(price, fwd, k, expiry, isCall);
      }
    };

    final BlackVolatilitySurface<Strike> surfaceStrike = new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));

    final int nExp = EXPIRIES.length;
    int temp = 0;
    for (int i = 0; i < nExp; i++) {
      temp += STRIKES[i].length;
    }
    final int totalStrikes = temp;

    final ForwardCurve fwdCurve = new ForwardCurve(1.0);

    List<double[]> tk = new ArrayList<double[]>(totalStrikes);
    List<Double> vols = new ArrayList<Double>(totalStrikes);
    List<Double> sigmas = new ArrayList<Double>(totalStrikes);
    for (int i = 0; i < nExp; i++) {
      double t = EXPIRIES[i];
      for (int j = 0; j < STRIKES[i].length; j++) {
        double[] a = new double[] {t, STRIKES[i][j] };
        tk.add(a);
        vols.add(surfaceStrike.getVolatility(t, STRIKES[i][j]));
        sigmas.add(1.0);
      }
    }

    int[] nKnots = new int[] {8, 20 };
    int[] degree = new int[] {3, 3 };
    int[] diff = new int[] {2, 2 };
    double[] lambda = new double[] {0.1, 0.3 };

    PSplineFitter splineFitter = new PSplineFitter();
    GeneralizedLeastSquareResults<double[]> res = splineFitter.solve(tk, vols, sigmas, new double[] {0.0, 0.0 }, new double[] {2.0, 3.0 }, nKnots, degree, lambda, diff);

    System.out.println(res.getChiSq());
    final Function1D<double[], Double> func = res.getFunction();

    Function<Double, Double> temp2 = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tk) {
        return func.evaluate(new double[] {tk[0], tk[1] });
      }
    };

    //PDEUtilityTools.printSurface("start", surfaceStrike.getSurface(), 0.01, 2.0, 0.3, 3.0);

    FunctionalDoublesSurface s = FunctionalDoublesSurface.from(temp2);
    PDEUtilityTools.printSurface("fitted", s, 0.01, 2.0, 0.3, 3.0);

    DupireLocalVolatilityCalculator dCal = new DupireLocalVolatilityCalculator();
    LocalVolatilitySurfaceStrike lv = dCal.getLocalVolatility(new BlackVolatilitySurfaceStrike(s), fwdCurve);
    PDEUtilityTools.printSurface("lv", lv.getSurface(), 0.01, 2.0, 0.3, 3.0);
  }

  /**
   * gets a time independent local vol 
   * @param bSplines The basis functions (1d functions in strike) 
   * @param fwdCurve the forward curve
   * @param weights The weights 
   * @return The local vol surface 
   */
  private LocalVolatilitySurfaceMoneyness getLocalVol1D(final List<Function1D<Double, Double>> bSplines, final ForwardCurve fwdCurve, double[] weights) {
    final Function1D<Double, Double> func = new BasisFunctionAggregation<Double>(bSplines, weights);

    Function<Double, Double> temp = new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... tx) {
        double x = tx[1];
        return func.evaluate(x);
      }
    };

    LocalVolatilitySurfaceMoneyness localVolSurface = new LocalVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(temp), fwdCurve);
    return localVolSurface;
  }

  private LocalVolatilitySurfaceMoneyness getLocalVol(final List<Function1D<double[], Double>> bSplines, final ForwardCurve fwdCurve, double[] weights) {
    final Function1D<double[], Double> func = new BasisFunctionAggregation<double[]>(bSplines, weights);
    Function<Double, Double> temp = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        return func.evaluate(new double[] {x[0], x[1] });
      }

    };

    Surface<Double, Double, Double> surf = FunctionalDoublesSurface.from(temp);
    LocalVolatilitySurfaceMoneyness localVolSurface = new LocalVolatilitySurfaceMoneyness(surf, fwdCurve);
    return localVolSurface;
  }

  private BlackVolatilitySurfaceMoneyness solveForwardPDE(final ForwardCurve fwdCurve, final BoundaryCondition lower, final BoundaryCondition upper, final PDEGrid1D pdeGrid,
      final Function1D<Double, Double> initialCond, LocalVolatilitySurfaceMoneyness localVolSurface) {
    ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_PROVIDER.getForwardLocalVol(localVolSurface);

    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCond, lower, upper, pdeGrid);
    PDEFullResults1D res = (PDEFullResults1D) SOLVER.solve(db);
    Map<DoublesPair, Double> volsurf = PDEUtilityTools.modifiedPriceToImpliedVol(res, 0.1, 2.0, 0.3, 3.0, true);

    final Map<Double, Interpolator1DDataBundle> idb = INTERPOLATOR.getDataBundle(volsurf);
    Function<Double, Double> f2 = new Function<Double, Double>() {

      @Override
      public Double evaluate(Double... x) {
        DoublesPair data = new DoublesPair(x[0], x[1]);
        return INTERPOLATOR.interpolate(idb, data);
      }
    };

    BlackVolatilitySurfaceMoneyness impVol = new BlackVolatilitySurfaceMoneyness(FunctionalDoublesSurface.from(f2), fwdCurve);
    return impVol;
  }

}
