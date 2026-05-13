import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enterprise JUnit 5 verification layer for the GeometricModels engine.
 * Fully tests all public variables and private pricing/Greek calculation methods.
 */
public class GeometricModelsTest {

    private GeometricModels geoTest;

    // Strict parameter inputs matching your core script definition
    private final double S = 4493.0;    // Spot Price
    private final double K = 4493.0;    // Strike Price
    private final double T = 0.24658;   // Maturity in Years
    private final double sig = 0.21097; // Volatility
    private final double r = 0.04314;   // Risk Free Rate
    private final double div = 0.0087;  // Dividend Yield

    // Strict tolerance threshold for high-precision validation
    private final double TOLERANCE = 1e-6;

    @BeforeEach
    public void setUp() {
        // Instantiate the model engine before running each test case
        geoTest = new GeometricModels(S, K, r, div, sig, T);
    }

    /**
     * Test Case 1: Validates public analytic pricing fields.
     */
    @Test
    public void testPublicPricingModels() {
        assertEquals(115.35811948080331, geoTest.valueCurran, TOLERANCE,
                "Curran discrete pricing model value mismatch.");
        assertEquals(115.0099083919954, geoTest.valueKemnaVorst, TOLERANCE,
                "Kemna-Vorst continuous pricing model value mismatch.");
    }

    /**
     * Test Case 2: Validates public risk sensitivities (Greeks).
     */
    @Test
    public void testPublicHedgeSensitivities() {
        assertEquals(0.5301107712591591, geoTest.delta, TOLERANCE, "Delta field calculation failed.");
        assertEquals(0.0014520476764, geoTest.gamma, TOLERANCE, "Gamma field calculation failed.");
        assertEquals(5.0828934952952, geoTest.vega, TOLERANCE, "Vega field structural scaling failed.");
        assertEquals(8.5259267163725, geoTest.rho, TOLERANCE, "Rho field structural scaling failed.");
        assertEquals(-0.670303989365, geoTest.theta, TOLERANCE, "Theta field day-count scaling failed.");
    }

    /**
     * Test Case 3: Reflection test exercising private high-precision Cumulative Normal Distribution (CND).
     */
    @Test
    public void testPrivateCND() throws Exception {
        Method method = GeometricModels.class.getDeclaredMethod("CND", double.class);
        method.setAccessible(true);

        // Core statistical anchors
        assertEquals(0.5, (double) method.invoke(geoTest, 0.0), TOLERANCE);
        assertTrue((double) method.invoke(geoTest, 3.0) > 0.99);
        assertTrue((double) method.invoke(geoTest, -3.0) < 0.01);
    }

    /**
     * Test Case 4: Reflection test exercising private standard Normal Density (ND).
     */
    @Test
    public void testPrivateND() throws Exception {
        Method method = GeometricModels.class.getDeclaredMethod("ND", double.class);
        method.setAccessible(true);

        // Symmetrical Gaussian profile verification
        double zeroHeight = (double) method.invoke(geoTest, 0.0);
        assertEquals(1.0 / Math.sqrt(2.0 * Math.PI), zeroHeight, TOLERANCE);
        assertEquals((double) method.invoke(geoTest, 1.0), (double) method.invoke(geoTest, -1.0), TOLERANCE);
    }

    /**
     * Test Case 5: Reflection test exercising private pricing models directly.
     */
    @Test
    public void testPrivatePricingMethods() throws Exception {
        Method kvMethod = GeometricModels.class.getDeclaredMethod("modelKemnaVorstAsian",
                double.class, double.class, double.class, double.class, double.class, double.class);
        kvMethod.setAccessible(true);
        double kvResult = (double) kvMethod.invoke(geoTest, S, K, r, div, sig, T);
        assertEquals(115.0099083919954, kvResult, TOLERANCE);

        Method curranMethod = GeometricModels.class.getDeclaredMethod("modelCurranAsian",
                double.class, double.class, double.class, double.class, double.class, double.class);
        curranMethod.setAccessible(true);
        double curranResult = (double) curranMethod.invoke(geoTest, S, K, r, div, sig, T);
        assertEquals(115.35811948080331, curranResult, TOLERANCE);
    }

    /**
     * Test Case 6: Reflection test exercising private analytic Greek methods individually.
     */
    @Test
    public void testPrivateGreekMethods() throws Exception {
        Class<?>[] params = {double.class, double.class, double.class, double.class, double.class, double.class};

        Method deltaM = GeometricModels.class.getDeclaredMethod("deltaGeomethric", params);
        deltaM.setAccessible(true);
        assertEquals(0.5301107712591591, (double) deltaM.invoke(geoTest, S, K, T, r, div, sig), TOLERANCE);

        Method gammaM = GeometricModels.class.getDeclaredMethod("gammaGeomethric", params);
        gammaM.setAccessible(true);
        assertEquals(0.001452047676436948, (double) gammaM.invoke(geoTest, S, K, T, r, div, sig), TOLERANCE);

        Method thetaM = GeometricModels.class.getDeclaredMethod("thetaGeomethric", params);
        thetaM.setAccessible(true);
        assertEquals(-0.670303989365 * 365.0, (double) thetaM.invoke(geoTest, S, K, T, r, div, sig), TOLERANCE);

        Method vegaM = GeometricModels.class.getDeclaredMethod("vegaGeomethric", params);
        vegaM.setAccessible(true);
        assertEquals(5.0828934952952 * 100.0, (double) vegaM.invoke(geoTest, S, K, T, r, div, sig), TOLERANCE);

        Method rhoM = GeometricModels.class.getDeclaredMethod("rhoGeomethric", params);
        rhoM.setAccessible(true);
        assertEquals(8.5259267163725 * 100.0, (double) rhoM.invoke(geoTest, S, K, T, r, div, sig), TOLERANCE);
    }

    /**
     * Test Case 7: Verifies financial boundaries and invariant model logic.
     */
    @Test
    public void testFinancialLogicalBoundaries() {
        assertTrue(geoTest.valueCurran > 0.0);
        assertTrue(geoTest.valueKemnaVorst > 0.0);
        assertTrue(geoTest.delta >= 0.0 && geoTest.delta <= 1.0);
        assertTrue(geoTest.gamma > 0.0);
        assertTrue(geoTest.theta < 0.0);
    }
}
