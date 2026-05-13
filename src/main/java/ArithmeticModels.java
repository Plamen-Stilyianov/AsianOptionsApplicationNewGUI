/**
 * Created by IntelliJ IDEA.
 * Author: Plamen Stilyianov
 * Title: MSc Financial Markets with Information Systems
 * Date: 01-Sep-2005
 * Time: 12:01:58
 * To change this template use File | Settings | File Templates.
 */
public class ArithmeticModels {

    // greeks
    public double delta, gamma, vega, rho, theta = 0.0;
    public double valueLevy, valueTurnbullWakeman = 0.0;

    public ArithmeticModels(double S, double K, double r, double div, double sig, double T) {
        valueTurnbullWakeman = modelTurnbullWakemanAsian(S, K, r, div, sig, T);
        valueLevy = modelLevyAsian(S, K, r, div, sig, T);
        delta = deltaArithmetic(S, K, T, r, div, sig);
        gamma = gammaArithmetic(S, K, T, r, div, sig);
        theta = thetaArithmetic(S, K, T, r, div, sig) / 365.0;
        vega = vegaArithmetic(S, K, T, r, div, sig) / 100.0;
        rho = rhoArithmetic(S, K, T, r, div, sig) / 100.0;
    }

    private double modelTurnbullWakemanAsian(double S, double K, double r, double div, double sig, double T) {
        double b = r - div;
        double tau = 0.0;
        double sig2 = Math.pow(sig, 2);

        double m1 = ((Math.exp(b * T) - Math.exp(b * tau)) / (b * (T - tau))) * S;
        double m2 = Math.pow(S, 2) * 2 * Math.exp((2 * b + sig2) * T) / ((b + sig2) * (2 * b + sig2) * Math.pow(T - tau, 2))
                + (Math.pow(S, 2) * 2 * Math.exp((2 * b + sig2) * tau) / (b * Math.pow(T - tau, 2)))
                * (1.0 / (2 * b + sig2) - (Math.exp(b * (T - tau))) / (b + sig2));

        double modVol = Math.sqrt((1.0 / T) * Math.log(m2 / Math.pow(m1, 2)));
        return modelBlack76(K, m1, modVol * 100, T, r);
    }

    private double modelBlack76(double strike, double m1, double modVol, double yrs, double r) {
        double vol = modVol / 100.0;
        double d1 = (Math.log(m1 / strike) + Math.pow(vol, 2) * (yrs / 2.0)) / (vol * Math.sqrt(yrs));
        double d2 = d1 - vol * Math.sqrt(yrs);
        return Math.exp(-r * yrs) * (m1 * CND(d1) - strike * CND(d2));
    }

    private double modelLevyAsian(double S, double K, double r, double div, double sig, double T) {
        double b = r - div;
        double sig2 = sig * sig;

        // 1. First Moment (M1)
        double m1;
        if (Math.abs(b) < 1e-10) {
            m1 = S;
        } else {
            m1 = (S / (b * T)) * (Math.exp(b * T) - 1);
        }

        // 2. Second Moment (M2) - Stable Formula
        double m2 = (2 * S * S / (b + sig2)) *
                ((Math.exp((2 * b + sig2) * T) - 1) / (2 * b + sig2) - (Math.exp(b * T) - 1) / b)
                / (T * T);

        // 3. Match Moments to Log-Normal Variance
        double sv = Math.log(m2 / (m1 * m1));

        // 4. Corrected d1 and d2 for Levy Approximation
        // Note: The log(m1) term here is specifically for the Levy curve-fit
        double d1 = (Math.log(m1 / K) + 0.5 * sv) / Math.sqrt(sv);
        double d2 = d1 - Math.sqrt(sv);

        // 5. Discounted Price
        double price = Math.exp(-r * T) * (m1 * CND(d1) - K * CND(d2));

        return Math.max(0, price);
    }

    private double CND(double x) {
        // High-precision Cumulative Normal Distribution
        double p = 0.2316419;
        double b1 = 0.319381530, b2 = -0.356563782, b3 = 1.781477937, b4 = -1.821255978, b5 = 1.330274429;
        double t = 1.0 / (1.0 + p * Math.abs(x));
        double z = (1.0 / Math.sqrt(2.0 * Math.PI)) * Math.exp(-x * x / 2.0);
        double res = 1.0 - z * (b1 * t + b2 * t * t + b3 * Math.pow(t, 3) + b4 * Math.pow(t, 4) + b5 * Math.pow(t, 5));
        return (x < 0) ? 1.0 - res : res;
    }

    private double ND(double X) {
        return Math.exp(-Math.pow(X, 2) / 2.0) / Math.sqrt(2.0 * Math.PI);
    }

    // --- REFINED HEDGE SENSITIVITIES ---

    private double deltaArithmetic(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double m1_ratio = (Math.exp(b * T) - 1.0) / (b * T);
        double sig2 = Math.pow(sig, 2);
        double m2_ratio = (2.0 / ((b + sig2) * (2 * b + sig2) * T * T)) * (Math.exp((2 * b + sig2) * T) - 1.0)
                - (2.0 / (b * (b + sig2) * T * T)) * (Math.exp(b * T) - 1.0);

        double ba = Math.log(m1_ratio) / T;
        double va = Math.sqrt(Math.log(m2_ratio) / T - 2.0 * ba);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2)) * T) / (va * Math.sqrt(T));
        return Math.exp((ba - r) * T) * CND(d1);
    }

    private double gammaArithmetic(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double m1_ratio = (Math.exp(b * T) - 1.0) / (b * T);
        double sig2 = Math.pow(sig, 2);
        double m2_ratio = (2.0 / ((b + sig2) * (2 * b + sig2) * T * T)) * (Math.exp((2 * b + sig2) * T) - 1.0)
                - (2.0 / (b * (b + sig2) * T * T)) * (Math.exp(b * T) - 1.0);

        double ba = Math.log(m1_ratio) / T;
        double va = Math.sqrt(Math.log(m2_ratio) / T - 2.0 * ba);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2)) * T) / (va * Math.sqrt(T));
        // FIX: Switched from CND to ND for Gamma
        return (Math.exp((ba - r) * T) * ND(d1)) / (S * va * Math.sqrt(T));
    }

    private double thetaArithmetic(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double m1_ratio = (Math.exp(b * T) - 1.0) / (b * T);
        double sig2 = Math.pow(sig, 2);
        double m2_ratio = (2.0 / ((b + sig2) * (2 * b + sig2) * T * T)) * (Math.exp((2 * b + sig2) * T) - 1.0)
                - (2.0 / (b * (b + sig2) * T * T)) * (Math.exp(b * T) - 1.0);

        double ba = Math.log(m1_ratio) / T;
        double va = Math.sqrt(Math.log(m2_ratio) / T - 2.0 * ba);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2)) * T) / (va * Math.sqrt(T));
        double d2 = d1 - va * Math.sqrt(T);

        double term1 = -(S * Math.exp((ba - r) * T) * ND(d1) * va) / (2.0 * Math.sqrt(T));
        double term2 = -(ba - r) * S * Math.exp((ba - r) * T) * CND(d1);
        double term3 = r * K * Math.exp(-r * T) * CND(d2);
        return term1 + term2 - term3;
    }

    private double vegaArithmetic(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double m1_ratio = (Math.exp(b * T) - 1.0) / (b * T);
        double sig2 = Math.pow(sig, 2);
        double m2_ratio = (2.0 / ((b + sig2) * (2 * b + sig2) * T * T)) * (Math.exp((2 * b + sig2) * T) - 1.0)
                - (2.0 / (b * (b + sig2) * T * T)) * (Math.exp(b * T) - 1.0);

        double ba = Math.log(m1_ratio) / T;
        double va = Math.sqrt(Math.log(m2_ratio) / T - 2.0 * ba);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2)) * T) / (va * Math.sqrt(T));
        // FIX: Completed vega logic using adjusted volatility scaling
        return S * Math.exp((ba - r) * T) * ND(d1) * Math.sqrt(T) * (va / sig);
    }

    private double rhoArithmetic(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double m1_ratio = (Math.exp(b * T) - 1.0) / (b * T);
        double sig2 = Math.pow(sig, 2);
        double m2_ratio = (2.0 / ((b + sig2) * (2 * b + sig2) * T * T)) * (Math.exp((2 * b + sig2) * T) - 1.0)
                - (2.0 / (b * (b + sig2) * T * T)) * (Math.exp(b * T) - 1.0);

        double ba = Math.log(m1_ratio) / T;
        double va = Math.sqrt(Math.log(m2_ratio) / T - 2.0 * ba);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2)) * T) / (va * Math.sqrt(T));
        double d2 = d1 - va * Math.sqrt(T);
        // FIX: Completed Rho based on strike discounting
        return T * K * Math.exp(-r * T) * CND(d2);
    }


    public static void main(String [] args) {
        // Initial parameters
        double S = 4493;    // Stock price
        double K = 4493;    // Strike price
        double T = 0.24658; // Maturity date (approx 3 months)
        double sig = 0.21097; // Volatility
        double r = 0.04314;   // Interest rate
        double div = 0.0087;  // Dividend yield

        ArithmeticModels arTest = new ArithmeticModels(S, K, r, div, sig, T);

        // Printing results with controlled precision
        System.out.println("--- Arithmetic Asian Option Pricing ---");
        System.out.printf("Turnbull-Wakeman Price: %.4f%n", arTest.valueTurnbullWakeman);
        System.out.printf("Levy Approximation Price: %.4f%n", arTest.valueLevy);

        System.out.println("\n--- Hedge Sensitivities (Greeks) ---");
        System.out.printf("Delta: %.4f%n", arTest.delta);
        // Gamma is usually small for high spot prices; using more decimals
        System.out.printf("Gamma: %.8f%n", arTest.gamma);
        System.out.printf("Vega (per 1%% vol): %.4f%n", arTest.vega);
        System.out.printf("Rho (per 1%% r):   %.4f%n", arTest.rho);
        System.out.printf("Theta (per day):    %.4f%n", arTest.theta);
    }
}

/*
        --- Arithmetic Asian Option Pricing ---
Turnbull-Wakeman Price: 117.3715
Levy Approximation Price: 117.3715

        --- Hedge Sensitivities (Greeks) ---
Delta: 0.5366
Gamma: 0.00144914
Vega (per 1% vol): 5.0881
Rho (per 1% r):   5.6551
Theta (per day):    -0.6963

 */