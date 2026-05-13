/**
 * Created by IntelliJ IDEA.
 * Author: Plamen Stilyianov
 * Title: MSc Financial Markets with Information Systems
 * Date: 02-Sep-2005
 * Time: 00:10:17
 * To change this template use File | Settings | File Templates.
 */
public class GenBlackScholesModels {

    public double delta, gamma, vega, rho, theta = 0.0;
    public double valueBlackScholes = 0.0;

    public GenBlackScholesModels(double S, double K, double r, double div, double sig, double T) {
        valueBlackScholes = modelGenBlackScholes(S, K, r, div, sig, T);
        delta = deltaBS(S, K, T, r, div, sig);
        gamma = gammaBS(S, K, T, r, div, sig);
        theta = thetaBS(S, K, T, r, div, sig) / 365.0;
        vega = vegaBS(S, K, T, r, div, sig) / 100.0;
        rho = rhoBS(S, K, T, r, div, sig) / 100.0;
    }

    private double modelGenBlackScholes(double S, double K, double r, double div, double sig, double T) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        double d2 = d1 - (sig * Math.sqrt(T));
        return (S * Math.exp((b - r) * T) * CND(d1)) - (K * Math.exp(-r * T) * CND(d2));
    }

    // --- CORRECTED GREEKS ---

    private double deltaBS(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        return Math.exp((b - r) * T) * CND(d1);
    }

    private double gammaBS(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        // FIX: Changed CND(d1) to ND(d1). Gamma is a density function.
        gamma =  (Math.exp((b - r) * T) * ND(d1)) / (S * sig * Math.sqrt(T));

        return gamma; // Math.round(gamma * 1000000.0) / 1000000.0;
    }

    private double thetaBS(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        double d2 = d1 - (sig * Math.sqrt(T));

        // Refined standard BS Theta including dividend/cost-of-carry term
        double term1 = -(S * Math.exp((b - r) * T) * ND(d1) * sig) / (2.0 * Math.sqrt(T));
        double term2 = -(b - r) * S * Math.exp((b - r) * T) * CND(d1);
        double term3 = -r * K * Math.exp(-r * T) * CND(d2);

        return term1 + term2 + term3;
    }

    private double vegaBS(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        return S * Math.exp((b - r) * T) * ND(d1) * Math.sqrt(T);
    }

    private double rhoBS(double S, double K, double T, double r, double div, double sig) {
        double b = r - div;
        double d1 = (Math.log(S / K) + (b + 0.5 * sig * sig) * T) / (sig * Math.sqrt(T));
        double d2 = d1 - (sig * Math.sqrt(T));

        // For General BS, Rho is the sensitivity to the risk-free rate r.
        // Changing r affects both the discount factor and the cost-of-carry b.
        if (div == 0) { // Standard Black-Scholes (no dividend)
            return K * T * Math.exp(-r * T) * CND(d2);
        } else {
            return T * K * Math.exp(-r * T) * CND(d2);
        }
    }

    // Helper functions
    private double ND(double X) {
        return Math.exp(-X * X / 2.0) / Math.sqrt(2.0 * Math.PI);
    }

    private double CND(double X) {
        double L = Math.abs(X);
        double K = 1.0 / (1.0 + 0.2316419 * L);
        double a1 = 0.31938153, a2 = -0.356563782, a3 = 1.781477937, a4 = -1.821255978, a5 = 1.330274429;
        double w = 1.0 - 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-L * L / 2.0) *
                (K * (a1 + K * (a2 + K * (a3 + K * (a4 + K * a5)))));
        return (X < 0.0) ? 1.0 - w : w;
    }

    public static void main(String [] args) {

        //initial parameters
        double S = 4493;    // Stock price
        double K = 4493;    // strike price
        double T = 0.24658;      // Maturity date
        double sig = 0.21097;  // Volatility
        double r = 0.04314;   // Interest rate
        double div = 0.0087; // Divident yield

        //double S = 35.75;    // Stock price
        //double K = 35.75;    // strike price
        //double T = 0.24658;      // Maturity date
        //double sig = 0.26354;  // Volatility
        //double r = 0.04271;   // Interest rate
        //double div = 0.0457; // Divident yield

        GenBlackScholesModels bsTest = new GenBlackScholesModels(S, K, r, div, sig, T);

        System.out.println("blackScholesOptionValue: " + bsTest.valueBlackScholes);
        System.out.println("Black Scholes delta: " + bsTest.delta);
        System.out.println("Black Scholes gamma: " + bsTest.gamma);
        System.out.println("Black Scholes vega: " + bsTest.vega);
        System.out.println("Black Scholes rho: " + bsTest.rho);
        System.out.println("Black Scholes theta: " + bsTest.theta);
    }
}
