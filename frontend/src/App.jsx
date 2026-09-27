import { useState, useEffect, useCallback } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api/v1/transactions";

function App() {
  const [formData, setFormData] = useState({
    customerId: "",
    merchantId: "",
    amount: "",
    currency: "INR",
  });

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Dashboard data
  const [transactions, setTransactions] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState("");
  const [backendStatus, setBackendStatus] = useState("Checking");

  // Fetch transaction history from Spring Boot
  const fetchTransactions = useCallback(async () => {
    try {
      setHistoryError("");

      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error("Failed to fetch transaction history");
      }

      const data = await response.json();

      if (!Array.isArray(data)) {
        throw new Error("Invalid transaction history response");
      }

      setTransactions(data);
      setBackendStatus("Connected");
    } catch (err) {
      setBackendStatus("Disconnected");
      setHistoryError(
        err.message || "Unable to load transaction history"
      );
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  // Fetch history when dashboard first loads
  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  // Calculate live dashboard metrics
  const totalTransactions = transactions.length;

  const allowedTransactions = transactions.filter(
    (transaction) => transaction.decision === "ALLOW"
  ).length;

  const reviewTransactions = transactions.filter(
    (transaction) => transaction.decision === "REVIEW"
  ).length;

  const blockedTransactions = transactions.filter(
    (transaction) => transaction.decision === "BLOCK"
  ).length;

  const stats = [
    {
      label: "Total Transactions",
      value: totalTransactions,
      icon: "↗",
      color: "blue",
    },
    {
      label: "Allowed",
      value: allowedTransactions,
      icon: "✓",
      color: "green",
    },
    {
      label: "Under Review",
      value: reviewTransactions,
      icon: "◷",
      color: "orange",
    },
    {
      label: "Blocked",
      value: blockedTransactions,
      icon: "⊘",
      color: "red",
    },
  ];

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setLoading(true);
    setError("");
    setResult(null);

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          ...formData,
          amount: Number(formData.amount),
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || "Transaction evaluation failed"
        );
      }

      setResult(data);

      // Refresh the dashboard with the newly saved transaction
      await fetchTransactions();
    } catch (err) {
      setError(err.message || "Unable to connect to backend");
    } finally {
      setLoading(false);
    }
  };

  // Format transaction amount
  const formatAmount = (amount, currency) => {
    try {
      return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: currency || "INR",
        maximumFractionDigits: 2,
      }).format(amount);
    } catch {
      return `${currency || ""} ${amount}`;
    }
  };

  // Format transaction timestamp
  const formatDate = (date) => {
    if (!date) return "—";

    const parsedDate = new Date(date);

    if (Number.isNaN(parsedDate.getTime())) {
      return "—";
    }

    return parsedDate.toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">S</div>
          <div>
            <h2>Sentinel</h2>
            <span>Fraud Intelligence</span>
          </div>
        </div>

        <p className="nav-label">WORKSPACE</p>

        <nav>
          <a className="nav-item active" href="#dashboard">
            ▦ Dashboard
          </a>
          <a className="nav-item" href="#transactions">
            ⇄ Transactions
          </a>
          <a className="nav-item" href="#analyze">
            ◎ Analyze Transaction
          </a>
        </nav>

        <div className="sidebar-bottom">
          <div className="system-status">
            <span
              className={`status-dot ${
                backendStatus === "Connected"
                  ? "status-connected"
                  : backendStatus === "Disconnected"
                  ? "status-disconnected"
                  : ""
              }`}
            />

            <div>
              <strong>System status</strong>
              <p>
                {backendStatus === "Connected"
                  ? "Backend connected"
                  : backendStatus === "Disconnected"
                  ? "Backend unavailable"
                  : "Checking connection..."}
              </p>
            </div>
          </div>

          <p className="sidebar-footer">
            SENTINEL · MVP v1.0
          </p>
        </div>
      </aside>

      <main className="main" id="dashboard">
        <header className="topbar">
          <div>
            <span className="breadcrumb">
              Workspace / Overview
            </span>
            <h1>Fraud Dashboard</h1>
          </div>

          <div className="profile">
            <div className="avatar">A</div>
            <div>
              <strong>Admin</strong>
              <span>Risk Analyst</span>
            </div>
          </div>
        </header>

        <section className="welcome">
          <div>
            <p className="eyebrow">
              REAL-TIME RISK MONITORING
            </p>
            <h2>Transaction Intelligence</h2>
            <p>
              Monitor transactions, detect suspicious activity,
              and review risk decisions from one place.
            </p>
          </div>

          <a className="primary-btn" href="#analyze">
            + Analyze Transaction
          </a>
        </section>

        {/* Live statistics */}
        <section className="stats-grid">
          {stats.map((stat) => (
            <div className="stat-card" key={stat.label}>
              <div className={`stat-icon ${stat.color}`}>
                {stat.icon}
              </div>

              <p>{stat.label}</p>
              <h2>{stat.value}</h2>

              <span className="stat-note">
                {historyLoading
                  ? "Loading data..."
                  : "From transaction history"}
              </span>
            </div>
          ))}
        </section>

        <section className="content-grid">
          {/* Analyze transaction form */}
          <div className="panel" id="analyze">
            <div className="panel-heading">
              <div>
                <h3>Analyze a Transaction</h3>
                <p>
                  Submit a transaction for fraud risk evaluation.
                </p>
              </div>
              <span className="panel-icon">◎</span>
            </div>

            <form
              className="transaction-form"
              onSubmit={handleSubmit}
            >
              <label>
                Customer ID
                <input
                  name="customerId"
                  value={formData.customerId}
                  onChange={handleChange}
                  placeholder="e.g. CUSTOMER001"
                  required
                />
              </label>

              <label>
                Merchant ID
                <input
                  name="merchantId"
                  value={formData.merchantId}
                  onChange={handleChange}
                  placeholder="e.g. MERCHANT001"
                  required
                />
              </label>

              <div className="form-row">
                <label>
                  Amount
                  <input
                    name="amount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={formData.amount}
                    onChange={handleChange}
                    placeholder="2500.00"
                    required
                  />
                </label>

                <label>
                  Currency
                  <select
                    name="currency"
                    value={formData.currency}
                    onChange={handleChange}
                  >
                    <option value="INR">INR</option>
                    <option value="USD">USD</option>
                    <option value="EUR">EUR</option>
                    <option value="GBP">GBP</option>
                  </select>
                </label>
              </div>

              <button
                type="submit"
                className="primary-btn submit-btn"
                disabled={loading}
              >
                {loading
                  ? "Evaluating..."
                  : "Evaluate Transaction →"}
              </button>

              {error && (
                <p className="error-message">{error}</p>
              )}

              {result && (
                <div
                  className={`result-card ${result.decision?.toLowerCase()}`}
                >
                  <h3>Fraud Evaluation Result</h3>

                  <p>
                    Risk Score:{" "}
                    <strong>{result.riskScore}/100</strong>
                  </p>

                  <p>
                    Decision:{" "}
                    <strong>{result.decision}</strong>
                  </p>

                  <p>{result.reason}</p>
                </div>
              )}
            </form>
          </div>

          {/* Recent transactions from database */}
          <div className="panel" id="transactions">
            <div className="panel-heading">
              <div>
                <h3>Recent Transactions</h3>
                <p>Your latest transaction activity.</p>
              </div>

              <button
                className="text-btn"
                type="button"
                onClick={fetchTransactions}
                disabled={historyLoading}
              >
                {historyLoading ? "Loading..." : "Refresh ↻"}
              </button>
            </div>

            {historyError && (
              <p className="error-message">
                {historyError}
              </p>
            )}

            {historyLoading ? (
              <div className="empty-state">
                <h4>Loading transactions...</h4>
              </div>
            ) : transactions.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">⇄</div>
                <h4>No transactions to display</h4>
                <p>
                  Submit a transaction to see its fraud
                  evaluation here.
                </p>
              </div>
            ) : (
              <div className="transactions-table-wrapper">
                <table className="transactions-table">
                  <thead>
                    <tr>
                      <th>Transaction</th>
                      <th>Amount</th>
                      <th>Risk</th>
                      <th>Decision</th>
                    </tr>
                  </thead>

                  <tbody>
                    {transactions.slice(0, 8).map(
                      (transaction) => (
                        <tr key={transaction.id}>
                          <td>
                            <strong>
                              {transaction.merchantId}
                            </strong>
                            <span className="transaction-subtext">
                              {transaction.customerId}
                            </span>
                            <span className="transaction-subtext">
                              {formatDate(transaction.createdAt)}
                            </span>
                          </td>

                          <td>
                            {formatAmount(
                              transaction.amount,
                              transaction.currency
                            )}
                          </td>

                          <td>
                            {transaction.riskScore ?? 0}/100
                          </td>

                          <td>
                            <span
                              className={`decision-badge ${
                                transaction.decision?.toLowerCase() ||
                                ""
                              }`}
                            >
                              {transaction.decision || "UNKNOWN"}
                            </span>
                          </td>
                        </tr>
                      )
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </section>

        <footer className="footer">
          <span>Sentinel Fraud Detection Platform</span>
          <span>Built with React + Spring Boot</span>
        </footer>
      </main>
    </div>
  );
}

export default App;