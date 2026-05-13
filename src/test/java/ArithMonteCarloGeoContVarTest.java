import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Production JUnit 5 test suite for ArithMonteCarloGeoContVar.
 * Validates the simulation price and all numerical finite-difference Greeks.
 */
public class ArithMonteCarloGeoContVarTest {

    private ArithMonteCarloGeoContVar mcEngine;

    // Baseline configuration parameters matching the script comments
    private final double S = 4493.0;
    private final double K = 4493.0;
    private final double T = 0.24658;
    private final double sig = 0.21097;
    private final double r = 0.04314;
    private final double div = 0.0087;

    // Tight precision tolerance due to fixed pseudorandom seeding
    private final double TOLERANCE = 1e-6;

    @BeforeEach
    public void setUp() {
        // Instantiate the pricing engine before running each test case
        mcEngine = new ArithMonteCarloGeoContVar(S, K, r, div, sig, T);
    }

    @Test
    public void testMonteCarloPrice() {
        // Asserts base option price computed via Control Variate path adjustment
        assertEquals(117.6550, mcEngine.valueMonteCarlo, 0.0001,
                "Monte Carlo Price does not match expected output.");
    }

    @Test
    public void testDeltaSensitivity() {
        // Asserts first-order spot directional sensitivity via central difference
        assertEquals(0.5341308628930588, mcEngine.delta, TOLERANCE,
                "Delta calculation mismatch.");
    }

    @Test
    public void testGammaSensitivity() {
        // Asserts second-order spot acceleration curvature via central difference
        assertEquals(0.0014520527475580035, mcEngine.gamma, TOLERANCE,
                "Gamma calculation mismatch.");
    }

    @Test
    public void testVegaSensitivity() {
        // Asserts absolute volatility risk sensitivity bumped per 1%
        assertEquals(5.100633694746676, mcEngine.vega, TOLERANCE,
                "Vega calculation mismatch.");
    }

    @Test
    public void testRhoSensitivity() {
        // Asserts risk-free rate interest slope sensitivity scaled to 1%
        assertEquals(2.7168551132075436, mcEngine.rho, TOLERANCE,
                "Rho calculation mismatch.");
    }

    @Test
    public void testThetaSensitivity() {
        // Asserts time decay premium erosion mapped per day
        assertEquals(-0.7006367558966815, mcEngine.theta, TOLERANCE,
                "Theta calculation mismatch.");
    }

    @Test
    public void testLogicalBoundaryConstraints() {
        // Validation rules to check general financial logic boundaries
        assertTrue(mcEngine.valueMonteCarlo > 0.0, "Option value cannot be negative.");
        assertTrue(mcEngine.delta >= 0.0 && mcEngine.delta <= 1.0, "Long Call Delta must be between 0 and 1.");
        assertTrue(mcEngine.gamma > 0.0, "Long option positions must maintain a positive Gamma profile.");
        assertTrue(mcEngine.theta < 0.0, "Standard European-style call options must decay over time.");
    }
}
