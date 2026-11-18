# Asian Option Pricing & Risk Analytics Engine

A high-performance, enterprise Java desktop application built to price, simulate, and analyze continuous and discrete European Asian Call Equity Options. This platform integrates six distinct quantitative models to provide real-time asset valuations and high-precision hedge sensitivities (the Greeks) for sophisticated risk management.

## 🛠️ Core Technological Architecture

### 1. Real-Time Swing UI Desktop Interface
* **Symmetric Component Grid:** Implements an optimized fixed-dimension graphic panel using isolated layout structures (`BorderLayout`, `GridLayout`, and nested `FlowLayout` blocks).
* **Defensive Workflow Control:** Protects system integrity with embedded input validation layers, ensuring formatting typos or string characters never compromise the backend mathematical processing kernels.

### 2. Analytical & Stochastic Execution Suite
The core engine computes exact closed-form solutions and high-performance stochastic path simulations across six pricing profiles:
* **Vanilla Baseline Kernel:** A generalized **Black-Scholes-Merton model** configured to provide the standard vanilla equity option reference benchmark.
* **Arithmetic Analytical Approximations:** Implements continuous and discrete moment-matching approximations via the **Turnbull-Wakeman** and **Levy** methodologies.
* **Geometric Analytical Approximations:** Computes exact closed-form solutions using the **Kemna & Vorst** model and **Curran's discrete approximation** algorithm.
* **Variance-Reduced Monte Carlo Engine:** Simulates tracking arrays using a fixed pseudorandom seed. It deploys a **Geometric Control Variate modifier** to dramatically accelerate path convergence and ensure perfectly deterministic regression testing.

### 3. Automated Regression Framework
* **Unified Suite Orchestration:** Leverages the modern **JUnit 5 / JUnit Platform Suite Architecture** to bundle, sequence, and execute all test targets as a single unified process.
* **Encapsulated Verification:** Employs **Java Reflection** to thoroughly test internal, hidden dependencies (such as high-precision Cumulative Normal Distribution and Normal Density kernels) without forcing structural visibility edits to the underlying source modules.

---

## 📊 Quantitative Analysis: Model Utility & Market Tradeoffs

The application analyzes structural tradeoffs across all tracking models, mapping out distinct operational boundaries for risk managers and quantitative analysts:


| Pricing Methodology | Key Operational Advantage | Computational Performance | Practical Risk Management Tradeoff |
| :--- | :--- | :--- | :--- |
| **Monte Carlo with Control Variates** | **Unmatched Flexibility:** Captures the true path-dependent dynamics of complex, discrete multi-asset sampling dates. | Slowest (requires path iteration) but significantly accelerated by the control variate loop. | Provides the most precise, realistic, and stable **total numerical Greeks** (e.g., Rho = `2.7169`), effectively capturing localized path-bump risk. |
| **Arithmetic Approximations** *(Turnbull-Wakeman / Levy)* | **Real-Time Execution:** Offers near-instant calculations for arithmetic averages, which are the standard for real-world exotic equity contracts. | Sub-millisecond execution times via direct continuous moment matching. | Can introduce truncation errors under extreme volatility profiles. Its analytical formulas capture the strike-discounting slope but omit internal drift factors, resulting in an inflated Rho profile (e.g., `5.6551`). |
| **Geometric Approximations** *(Kemna-Vorst / Curran)* | **Mathematical Precision:** Delivers an exact, closed-form calculus solution for geometric options profiles. | Instantaneous closed-form evaluation. | Limited practical market utility, as most real-world exotic options trade on an *arithmetic* average basis ($AM \ge GM$). However, it serves as the perfect mathematical proxy for stabilizing the Monte Carlo path engine. |

---

## 🚀 Build, Test, and Execution Instructions

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher (compatible with up to Java 20).
* **Apache Maven:** Version 3.8+ installed and configured on your system environment path.

### 1. Project Directory Compilation
To validate the configuration parameters, process file mappings, and compile your Java classes, navigate to the root directory containing your `pom.xml` and run:
```bash
mvn clean compile
```

### 2. Execute the Complete Test Suite
To verify the math engine, launch your automated `AsianOptionTestSuite` directly through the terminal. This completely bypasses local IDE agent conflicts (such as `ClassCircularityError` flags):
```bash
mvn test -U
```
*The `-U` flag forces Maven to clear out old build caches and update dependencies directly from the remote mirrors, eliminating any "IDLE" package download loops.*

### 3. Run the User Interface Application
To execute the compiled project and launch the desktop options pricing calculator pane:
```bash
mvn exec:java -Dexec.mainClass="AsianOptionFrame"
```
![pricer_new_gui.png](images/pricer_new_gui.png)

---

## 🏛️ Empirical Validation & Bloomberg Terminal Benchmarks

The inputs and valuation outputs in this application are empirically verified against live **Bloomberg Terminal** option pricing screens (`OVX` and `OV`) using market data for **IBM (`IBM LN Equity`)** on the London Stock Exchange.

### Reference Market Parameters (`IBM LN Equity`)
* **Underlying Spot Price ($S$):** `4493` GBp
* **Strike Price ($K$):** `4493` GBp *(100.000% ATM)*
* **Time to Expiration ($T$):** `90` days (`0.24658` years; Trade Date `8/30/05` to Expiration `11/28/05`)
* **Risk-Free Interest Rate ($r$):** `4.314%` *(Semiannual)*
* **Continuous Dividend Yield ($d$):** `0.87%`
* **Volatility ($\sigma$):** `21.097%`
* **Averaging Schedule:** `65` discrete, unweighted observation points

### Bloomberg Terminal Benchmark Results

| Bloomberg Screen & Model | Option Price (GBp) | Delta ($\Delta$) | Gamma ($\Gamma$) | Vega ($
u$) | Rho ($
ho$) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Asian Option (`OVX`) — Discrete Arithmetic** | `115.7449` | `0.56865` | `0.00146` | `5.02953` | — |
| **Asian Option (`OVX`) — Discrete Geometric** | `113.3578` | `0.50583` | `0.00147` | `4.80555` | — |
| **Standard Option (`OV`) — Black-Scholes (Continuous Div.)** | `205.9299` | `0.55150` | `0.00084` | `8.80944` | `561.25610` |
| **Standard Option (`OV`) — Trinomial (Discrete Div.)** | `206.1733` | `0.55172` | `0.00084` | `8.82145` | `554.07025` |

### Bloomberg Terminal Verification Screens

#### 1. Asian Option Valuation (`OVX`) — Arithmetic vs. Geometric
| Discrete Arithmetic Average (`115.7449`) | Discrete Geometric Average (`113.3578`) |
| :---: | :---: |
| ![Bloomberg Arithmetic Asian Option](images/asian_option_aritmetic.png) | ![Bloomberg Geometric Asian Option](images/asian_option_geometric.png) |

#### 2. Standard European Option Valuation (`OV`) — Vanilla Reference Benchmarks
| Black-Scholes Continuous Dividends (`205.9299`) | Trinomial Discrete Dividends (`206.1733`) |
| :---: | :---: |
| ![Bloomberg Standard Black-Scholes](images/standard_option_bs.png) | ![Bloomberg Standard Enhanced Dividends](images/standard_option_enhanced_dividents.png) |
