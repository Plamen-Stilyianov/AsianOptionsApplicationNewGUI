import java.util.Random;


/**
 * Created by IntelliJ IDEA.
 * Author: Plamen Stilyianov
 * Title: MSc Financial Markets with Information Systems
 * Date: 02-Sep-2005
 * Time: 00:42:00
 * To change this template use File | Settings | File Templates.
 * @noinspection ALL
 */


public class ArithMonteCarloGeoContVar {
    // Arithmetic Monte Carlo with Geometric Control Variates

    public double valueMonteCarlo, delta, gamma, vega, rho, theta;

    public ArithMonteCarloGeoContVar(double S, double K, double r, double div, double sig, double T) {
        int M = 10000; // Total paths
        int N = 250;   // Monitoring steps

        // Step sizes for Finite Difference (Bumps)
        double hS = S * 0.01;   // 1% bump for Spot
        double hVol = 0.01;     // 1% bump for Volatility
        double hR = 0.001;      // 0.1% bump for Rho
        double hT = 1.0 / 365.0; // 1 day bump for Theta

        // 1. Base Price
        this.valueMonteCarlo = solveMC(S, K, r, div, sig, T, M, N);

        // 2. Delta & Gamma (Central Difference)
        double pUpS = solveMC(S + hS, K, r, div, sig, T, M, N);
        double pDownS = solveMC(S - hS, K, r, div, sig, T, M, N);
        this.delta = (pUpS - pDownS) / (2 * hS);
        this.gamma = (pUpS - 2 * valueMonteCarlo + pDownS) / (hS * hS);

        // 3. Vega (Sensitivity to 1% absolute change in sigma)
        double pUpV = solveMC(S, K, r, div, sig + hVol, T, M, N);
        this.vega = (pUpV - valueMonteCarlo); // Change per 1% vol

        // 4. Rho (Sensitivity to 1% absolute change in r)
        // We bump by 0.1% and scale to 1% for stability
        double pUpR = solveMC(S, K, r + hR, div, sig, T, M, N);
        this.rho = (pUpR - valueMonteCarlo) * 10;

        // 5. Theta (Sensitivity to 1 day passing)
        // Note: Solve for T - 1 day
        double pDownT = solveMC(S, K, r, div, sig, T - hT, M, N);
        this.theta = (pDownT - valueMonteCarlo); // Decay per day
    }

    private double solveMC(double S, double K, double r, double div, double sig, double T, int M, int N) {
        if (T <= 0) return Math.max(0, S - K); // Handle maturity edge case for Theta

        double dt = T / N;
        double nudt = (r - div - 0.5 * sig * sig) * dt;
        double sigsdt = sig * Math.sqrt(dt);

        double sumArithPayoff = 0;
        double sumGeomPayoff = 0;

        // CRITICAL: Fixed seed ensures we compare the same paths during bumps
        Random pathRand = new Random(12345);

        for (int j = 0; j < M; j++) {
            double St = S;
            double runningArithSum = S;
            double runningLogSum = Math.log(S);

            for (int i = 1; i <= N; i++) {
                double epsilon = pathRand.nextGaussian();
                St *= Math.exp(nudt + sigsdt * epsilon);
                runningArithSum += St;
                runningLogSum += Math.log(St);
            }

            double arithAvg = runningArithSum / (N + 1);
            double geomAvg = Math.exp(runningLogSum / (N + 1));

            sumArithPayoff += Math.max(0, arithAvg - K);
            sumGeomPayoff += Math.max(0, geomAvg - K);
        }

        double priceArith = (sumArithPayoff / M) * Math.exp(-r * T);
        double priceGeom = (sumGeomPayoff / M) * Math.exp(-r * T);

        double exactGeom = getGeometricExact(S, K, r, div, sig, T, N);

        return priceArith - (priceGeom - exactGeom);
    }

    private double getGeometricExact(double S, double K, double r, double div, double sig, double T, int N) {
        double b = r - div;
        double sigG = sig * Math.sqrt((double)(N + 1) * (2 * N + 1) / (6.0 * N * N));
        double muG = (b - 0.5 * sig * sig) * 0.5 * (1 + 1.0/N) + 0.5 * sigG * sigG;

        double d1 = (Math.log(S / K) + (muG + 0.5 * sigG * sigG) * T) / (sigG * Math.sqrt(T));
        double d2 = d1 - sigG * Math.sqrt(T);

        return Math.exp(-r * T) * (S * Math.exp(muG * T) * CND(d1) - K * CND(d2));
    }

    private double CND(double x) {
        double p = 0.2316419;
        double b1 = 0.319381530, b2 = -0.356563782, b3 = 1.781477937, b4 = -1.821255978, b5 = 1.330274429;
        double t = 1.0 / (1.0 + p * Math.abs(x));
        double z = (1.0 / Math.sqrt(2.0 * Math.PI)) * Math.exp(-x * x / 2.0);
        double res = 1.0 - z * (b1 * t + b2 * t * t + b3 * Math.pow(t, 3) + b4 * Math.pow(t, 4) + b5 * Math.pow(t, 5));
        return (x < 0) ? 1.0 - res : res;
    }

    public static void main(String [] args) {
        double S = 4493, K = 4493, T = 0.24658, sig = 0.21097, r = 0.04314, div = 0.0087;

        ArithMonteCarloGeoContVar mcTest = new ArithMonteCarloGeoContVar(S, K, r, div, sig, T);

        System.out.println("Price: " + mcTest.valueMonteCarlo);
        System.out.println("Delta: " + mcTest.delta);
        System.out.println("Gamma: " + mcTest.gamma);
        System.out.println("Vega (1%): " + mcTest.vega);
        System.out.println("Rho (1%): " + mcTest.rho);
        System.out.println("Theta (1 Day): " + mcTest.theta);
    }
}

/*
        --- Arithmetic Monte Carlo with Geometric Control Variates ---
Price: 117.3715

        --- Hedge Sensitivities (Greeks) ---
Delta: 0.5341308628930588
Gamma: 0.0014520527475580035
Vega (per 1% vol): 5.100633694746676
Rho (per 1% r):   2.7168551132075436
Theta (per day):  -0.7006367558966815
*/