import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enterprise JUnit 5 verification layer for the ArithmeticModels engine.
 * Fully exercises all public fields, analytical pricing models, and private mathematical utilities via Reflection.
 */
public class ArithmeticModelsTest {

    private ArithmeticModels arTest;

    // Standard baseline configuration parameters matching your main routine
    private final double S = 4493.0;    // Spot Price
    private final double K = 4493.0;    // Strike Price
    private final double T = 0.24658;   // Maturity horizon (Years)
    private final double sig = 0.21097; // Implied Volatility
    private final double r = 0.04314;   // Risk-Free Discount Rate
    private final double div = 0.0087;  // Continuous Dividend Yield

    // Strict numerical convergence threshold for analytical assertions
    private final double TOLERANCE = 1e-6;

    @BeforeEach
    public void setUp() {
        // Instantiate the option pricing library block before running each isolated method
        arTest = new ArithmeticModels(S, K, r, div, sig, T);
    }

    /**
     * Test Case 1: Validates public analytic approximation pricing fields.
     */
    @Test
    public void testPublicPricingApproximations() {
        // Verify Turnbull-Wakeman continuous moment matching output
        assertEquals(117.3714618786756, arTest.valueTurnbullWakeman, TOLERANCE,
                "Turnbull-Wakeman model pricing field value corrupted.");

        // Verify Levy curve approximation moment matching output
        assertEquals(117.37146187281924, arTest.valueLevy, TOLERANCE,
                "Levy approximation model pricing field value corrupted.");
    }

    /**
     * Test Case 2: Validates public primary directional and curvature Greeks.
     */
    @Test
    public void testPublicHedgeSensitivities() {
        // Delta: Directional price slope
        assertEquals(0.5365612026995257, arTest.delta, TOLERANCE, "Delta field calibration failed.");

        // Gamma: Directional curvature acceleration
        assertEquals(0.0014491408045527701, arTest.gamma, TOLERANCE, "Gamma field calibration failed.");

        // Vega: Absolute volatility exposure scaled via /100.0
        assertEquals(5.088143214829235, arTest.vega, TOLERANCE, "Vega structural scaling failed.");

        // Rho: Interest rate discounting slope scaled via /100.0
        assertEquals(5.655061494488344, arTest.rho, TOLERANCE, "Rho structural scaling failed.");

        // Theta: Calendar calendar day-count decay scaled via /365.0
        assertEquals(-0.696291015452815, arTest.theta, TOLERANCE, "Theta structural scaling failed.");
    }

    /**
     * Test Case 3: Reflection Test exercising private high-precision Cumulative Normal Distribution (CND).
     */
    @Test
    public void testPrivateCumulativeNormalDistribution() throws Exception {
        Method cndMethod = ArithmeticModels.class.getDeclaredMethod("CND", double.class);
        cndMethod.setAccessible(true);

        // Standard probability boundary assertions
        double zeroPoint = (double) cndMethod.invoke(arTest, 0.0);
        assertEquals(0.5, zeroPoint, TOLERANCE, "CND at zero axis must evaluate to exactly 0.5");

        double deepInMoney = (double) cndMethod.invoke(arTest, 5.0);
        assertEquals(0.9999997, deepInMoney, 1e-5, "CND asymptotic boundary fail.");

        double deepOutMoney = (double) cndMethod.invoke(arTest, -5.0);
        assertEquals(0.0000003, deepOutMoney, 1e-5, "CND negative trailing boundary fail.");
    }

    /**
     * Test Case 4: Reflection Test exercising private standard Normal Density (ND).
     */
    @Test
    public void testPrivateNormalDensityDistribution() throws Exception {
        Method ndMethod = ArithmeticModels.class.getDeclaredMethod("ND", double.class);
        ndMethod.setAccessible(true);

        // Apex height of the standard Gaussian bell curve
        double zeroHeight = (double) ndMethod.invoke(arTest, 0.0);
        assertEquals(1.0 / Math.sqrt(2.0 * Math.PI), zeroHeight, TOLERANCE, "ND apex must equal 1/sqrt(2*pi).");

        // Symmetrical curve tracking validations
        double positiveSymmetry = (double) ndMethod.invoke(arTest, 1.5);
        double negativeSymmetry = (double) ndMethod.invoke(arTest, -1.5);
        assertEquals(positiveSymmetry, negativeSymmetry, TOLERANCE, "ND fails to express mathematical mirror symmetry.");
    }

    /**
     * Test Case 5: Reflection Test exercising private Black-76 Option Valuation Core Engine.
     */
    @Test
    public void testPrivateBlack76Kernel() throws Exception {
        Method blackMethod = ArithmeticModels.class.getDeclaredMethod("modelBlack76",
                double.class, double.class, double.class, double.class, double.class);
        blackMethod.setAccessible(true);

        // Parameters: (strike, m1, modVol, yrs, r)
        double strike = 100.0;
        double forwardM1 = 105.0;
        double volatilityScaled = 20.0; // 20% expressed as modVol * 100
        double time = 1.0;
        double rate = 0.05;

        double kernelPrice = (double) blackMethod.invoke(arTest, strike, forwardM1, volatilityScaled, time, rate);

        // Verify output remains rational and positive
        assertTrue(kernelPrice > 0.0, "Private Black-76 option kernel returned negative pricing boundary output.");
        assertEquals(10.373712837740444, kernelPrice, 1e-5, "Black-76 model kernel pricing logic calculation failed.");
    }

    /**
     * Test Case 6: Verifies standard quantitative boundary conditions and invariant financial inequalities.
     */
    @Test
    public void testFinancialLogicalBoundaries() {
        // Long call option options values are constrained strictly above zero
        assertTrue(arTest.valueTurnbullWakeman > 0.0, "Arbitrage boundary broken: Call value under zero.");
        assertTrue(arTest.valueLevy > 0.0, "Arbitrage boundary broken: Call value under zero.");

        // Delta for a simple long call option must strictly reside within a probability band of 0 and 1
        assertTrue(arTest.delta >= 0.0 && arTest.delta <= 1.0, "Delta bounds constraint broken.");

        // Long option profiles represent convex asset growth structures and require non-negative Gamma
        assertTrue(arTest.gamma > 0.0, "Option convexity failed: Gamma cannot be zero or negative.");

        // European call options on standard equities lose premium over a static timeline as maturity compresses
        assertTrue(arTest.theta < 0.0, "Option time-decay constraint failed: Theta must represent a negative premium drag.");
    }
}
