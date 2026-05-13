import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Asian Option Pricing Engine - Complete Test Suite")
@SelectClasses({
        GenBlackScholesModelsTest.class,
        ArithmeticModelsTest.class,
        GeometricModelsTest.class,
        ArithMonteCarloGeoContVarTest.class
})
public class AsianOptionTestSuite {
    // Leave the body empty. It serves as a hook for Maven's execution loop.
}
