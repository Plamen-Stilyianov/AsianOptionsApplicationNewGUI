public class GeometricModels {

    public double delta, gamma, vega, rho, theta = 0.0;
    public double valueKemnaVorst, valueCurran = 0.0;

    public GeometricModels(double S, double K, double r, double div, double sig, double T) {
        valueKemnaVorst = modelKemnaVorstAsian(S, K, r, div, sig, T);
        valueCurran = modelCurranAsian(S, K, r, div, sig, T);
        delta = deltaGeomethric(S, K, T, r, div, sig);
        gamma = gammaGeomethric(S, K, T, r, div, sig);
        theta = thetaGeomethric(S, K, T, r, div, sig) / 365.0;
        vega = vegaGeomethric(S, K, T, r, div, sig) / 100.0;
        rho = rhoGeomethric(S, K, T, r, div, sig) / 100.0;
    }

    private double modelKemnaVorstAsian(double S, double K, double r, double div, double sig, double T) {
        double siga = sig / Math.sqrt(3.0);
        double b = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);

        double d1 = (Math.log(S / K) + (b + 0.5 * Math.pow(siga, 2.0)) * T) / (siga * Math.sqrt(T));
        double d2 = d1 - siga * Math.sqrt(T);

        return S * Math.exp((b - r) * T) * CND(d1) - K * Math.exp(-r * T) * CND(d2);
    }

    // Corrected Curran-style Discrete Geometric Asian Option
    private double modelCurranAsian(double S, double K, double r, double div, double sig, double T) {
        double n = 260.0; // Typical trading days
        double b = r - div;

        // Adjust volatility for discrete sampling
        double sigG = sig * Math.sqrt((n + 1.0) * (2.0 * n + 1.0) / (6.0 * n * n));

        // Adjust drift for discrete sampling
        double muG = (b - 0.5 * sig * sig) * (n + 1.0) / (2.0 * n) + 0.5 * sigG * sigG;

        double d1 = (Math.log(S / K) + (muG + 0.5 * sigG * sigG) * T) / (sigG * Math.sqrt(T));
        double d2 = d1 - (sigG * Math.sqrt(T));

        return Math.exp(-r * T) * (S * Math.exp(muG * T) * CND(d1) - K * CND(d2));
    }

    private double CND(double X) {
        double L = Math.abs(X);
        double K = 1.0 / (1.0 + 0.2316419 * L);
        double w = 1.0 - 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-L * L / 2.0) *
                (K * (0.31938153 + K * (-0.356563782 + K * (1.781477937 + K * (-1.821255978 + K * 1.330274429)))));
        return (X < 0.0) ? 1.0 - w : w;
    }

    private double ND(double X) {
        return Math.exp(-X * X / 2.0) / Math.sqrt(2.0 * Math.PI);
    }

    // --- CORRECTED GREEKS ---

    private double deltaGeomethric(double S, double K, double T, double r, double div, double sig) {
        double va = sig / Math.sqrt(3.0);
        double ba = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2.0)) * T) / (va * Math.sqrt(T));
        return Math.exp((ba - r) * T) * CND(d1);
    }

    private double gammaGeomethric(double S, double K, double T, double r, double div, double sig) {
        double va = sig / Math.sqrt(3.0);
        double ba = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2.0)) * T) / (va * Math.sqrt(T));
        // FIX: Use ND(d1) [density] instead of CND(d1) [cumulative]
        return (Math.exp((ba - r) * T) * ND(d1)) / (S * va * Math.sqrt(T));
    }

    private double thetaGeomethric(double S, double K, double T, double r, double div, double sig) {
        double va = sig / Math.sqrt(3.0);
        double ba = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2.0)) * T) / (va * Math.sqrt(T));
        double d2 = d1 - va * Math.sqrt(T);

        double term1 = -(S * Math.exp((ba - r) * T) * ND(d1) * va) / (2.0 * Math.sqrt(T));
        double term2 = -(ba - r) * S * Math.exp((ba - r) * T) * CND(d1);
        double term3 = r * K * Math.exp(-r * T) * CND(d2);
        return term1 + term2 - term3;
    }

    private double vegaGeomethric(double S, double K, double T, double r, double div, double sig) {
        double va = sig / Math.sqrt(3.0);
        double ba = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2.0)) * T) / (va * Math.sqrt(T));
        // FIX: Scaled by 1/sqrt(3) because we differentiate w.r.t original sigma
        return S * Math.exp((ba - r) * T) * ND(d1) * Math.sqrt(T) * (1.0 / Math.sqrt(3.0));
    }

    private double rhoGeomethric(double S, double K, double T, double r, double div, double sig) {
        double va = sig / Math.sqrt(3.0);
        double ba = 0.5 * (r - div - Math.pow(sig, 2.0) / 6.0);
        double d1 = (Math.log(S / K) + (ba + 0.5 * Math.pow(va, 2.0)) * T) / (va * Math.sqrt(T));
        double d2 = d1 - va * Math.sqrt(T);
        // FIX: Added the impact of r on the drift component (0.5 * T * Delta * S)
        return T * K * Math.exp(-r * T) * CND(d2) + 0.5 * T * S * Math.exp((ba - r) * T) * CND(d1);
    }

    public static void main(String [] args) {
        double S = 4493, K = 4493, T = 0.24658, sig = 0.21097, r = 0.04314, div = 0.0087;
        GeometricModels gmTest = new GeometricModels(S, K, r, div, sig, T);

        System.out.printf("Curran Value: %.4f%n", gmTest.valueCurran);
        System.out.printf("Kemna-Vorst Value: %.4f%n", gmTest.valueKemnaVorst);
        System.out.printf("Delta: %.4f%n", gmTest.delta);
        System.out.printf("Gamma: %.4f%n", gmTest.gamma);
        System.out.printf("Vega: %.4f%n", gmTest.vega);
        System.out.printf("Rho: %.4f%n", gmTest.rho);
        System.out.printf("Theta: %.4f%n", gmTest.theta);
    }
}
