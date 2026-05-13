import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enterprise JUnit 5 test suite for GenBlackScholesModels.
 * Exercises 100% of methods (public fields, public constructors, and private math engines).
 */
public class GenBlackScholesModelsTest {

    private GenBlackScholesModels bsTest;

    // Core parameter inputs matching your script configuration
    private final double S = 4493.0;    // Spot Price
    private final double K = 4493.0;    // Strike Price
    private final double T = 0.24658;   // Maturity in Years
    private final double sig = 0.21097; // Volatility
    private final double r = 0.04314;   // Risk Free Rate
    private final double div = 0.0087;  // Dividend Yield

    // High precision tolerance threshold for analytical convergence validation
    private final double TOLERANCE = 1e-6;

    @BeforeEach
    public void setUp() {
        // Instantiate the Generalized Black-Scholes model framework
        bsTest = new GenBlackScholesModels(S, K, r, div, sig, T);
    }

    // ==========================================
    // 1. PUBLIC FIELD & CONSTRUCTOR TESTS
    // ==========================================

    @Test
    public void testPublicPricingApproximation() {
        // Verifies base vanilla Generalized Black-Scholes option price
        assertEquals(206.06672061902, bsTest.valueBlackScholes, TOLERANCE,
                "Generalized Black-Scholes valuation variable value corrupted.");
    }

    @Test
    public void testPublicHedgeSensitivities() {
        // Verifies public risk sensitivities after constructor initialization scaling
        assertEquals(0.5518933966437, bsTest.delta, TOLERANCE, "Delta field validation failed.");
        assertEquals(0.0008390886561226027, bsTest.gamma, TOLERANCE, "Gamma field validation failed.");

        // Vega scaled by /100.0 inside constructor
        assertEquals(8.8029244969858, bsTest.vega, TOLERANCE, "Vega structural scaling failed.");

        // Rho scaled by /100.0 inside constructor
        assertEquals(5.6062189876344, bsTest.rho, TOLERANCE, "Rho structural scaling failed.");

        // Theta scaled by /365.0 inside constructor
        assertEquals(-1.241347701673, bsTest.theta, TOLERANCE, "Theta structural scaling failed.");
    }

    // ==========================================
    // 2. PRIVATE PRICING MODEL METHOD TESTS (via Reflection)
    // ==========================================

    @Test
    public void testPrivateModelGenBlackScholes() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("modelGenBlackScholes",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        double result = (double) method.invoke(bsTest, S, K, r, div, sig, T);
        assertEquals(206.06672061902, result, TOLERANCE, "Private pricing core logic execution failure.");
    }

    // ==========================================
    // 3. PRIVATE GREEK ENGINE METHOD TESTS (via Reflection)
    // ==========================================

    @Test
    public void testPrivateDeltaBS() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("deltaBS",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        double result = (double) method.invoke(bsTest, S, K, T, r, div, sig);
        assertEquals(0.5518933966437, result, TOLERANCE, "Private Delta calculation output discrepancy.");
    }

    @Test
    public void testPrivateGammaBS() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("gammaBS",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        double result = (double) method.invoke(bsTest, S, K, T, r, div, sig);
        assertEquals(0.0008390886561226027, result, TOLERANCE, "Private Gamma calculation output discrepancy.");
    }

    @Test
    public void testPrivateThetaBS() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("thetaBS",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        // Raw internal function provides full annual framework output before constructor /365.0 shift
        double result = (double) method.invoke(bsTest, S, K, T, r, div, sig);
        assertEquals(-1.241347701673 * 365.0, result, TOLERANCE, "Private Theta engine calculation mismatch.");
    }

    @Test
    public void testPrivateVegaBS() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("vegaBS",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        // Raw internal function provides annual percentage output before constructor /100.0 shift
        double result = (double) method.invoke(bsTest, S, K, T, r, div, sig);
        assertEquals(8.8029244969858 * 100.0, result, TOLERANCE, "Private Vega engine calculation mismatch.");
    }

    @Test
    public void testPrivateRhoBS() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("rhoBS",
                double.class, double.class, double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        // Raw internal function provides annual rate change output before constructor /100.0 shift
        double result = (double) method.invoke(bsTest, S, K, T, r, div, sig);
        assertEquals(5.6062189876344 * 100.0, result, TOLERANCE, "Private Rho engine calculation mismatch.");
    }

    // ==========================================
    // 4. PRIVATE PROBABILITY UTILITY TESTS (via Reflection)
    // ==========================================

    @Test
    public void testPrivateCND() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("CND", double.class);
        method.setAccessible(true);

        assertEquals(0.5, (double) method.invoke(bsTest, 0.0), TOLERANCE, "CND baseline validation failure at origin.");
        assertTrue((double) method.invoke(bsTest, 4.0) > 0.999, "CND upper boundary limits failure.");
        assertTrue((double) method.invoke(bsTest, -4.0) < 0.001, "CND lower boundary limits failure.");
    }

    @Test
    public void testPrivateND() throws Exception {
        Method method = GenBlackScholesModels.class.getDeclaredMethod("ND", double.class);
        method.setAccessible(true);

        double expectedPeak = 1.0 / Math.sqrt(2.0 * Math.PI);
        assertEquals(expectedPeak, (double) method.invoke(bsTest, 0.0), TOLERANCE, "ND probability peak failure.");
    }

    // ==========================================
    // 5. INVARIANT BOUNDARY TESTS
    // ==========================================

    @Test
    public void testFinancialLogicalBoundaries() {
        assertTrue(bsTest.valueBlackScholes > 0.0, "Call pricing arbitrage breach: under zero bounds.");
        assertTrue(bsTest.delta >= 0.0 && bsTest.delta <= 1.0, "Long Call Delta bounds constraint violation.");
        assertTrue(bsTest.gamma > 0.0, "Long positions option curvature convexity failed.");
        assertTrue(bsTest.theta < 0.0, "Time-decay profile boundary violation.");
    }
}
