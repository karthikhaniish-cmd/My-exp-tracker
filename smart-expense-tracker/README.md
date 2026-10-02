# Smart Expense Tracker 💰 — Full-Stack Fintech SaaS & Native Android Platform

A modern, responsive personal finance tracking system with real-time financial analytics, monthly budget management, automated keyword category detection, recurring subscription tracking, and intelligent spending insights.

---

## 🌟 Key Capabilities

1. **Executive Dashboard**:
   - Total Balance, Total Income, Total Expenses, Net Savings, Monthly Spending, and Highest Spending Category.
   - Interactive charts: Spending Trend line, Categories Doughnut, Monthly Expenses bar, Income vs Expenses comparison.
2. **Add Transaction with Smart Category Auto-Detection**:
   - Keyword classifier detects "Swiggy" → Food, "Uber" → Transport, "Amazon" → Shopping, "Netflix" → Entertainment, etc.
   - Real-time updates to balances, charts, and budget progress.
3. **Transaction History & Auditing**:
   - Real-time search across descriptions, categories, payment methods, and notes.
   - Multi-filter: Income / Expense / All, Categories, Date ranges (Today, This Week, This Month, All Time).
   - Multi-sort: Newest, Oldest, Highest Amount, Lowest Amount.
   - Edit and delete with confirmation safeguards.
4. **Budget Management**:
   - Category budget limit caps with progress bars.
   - Dynamic alert thresholds: Normal (<75%), Warning (≥75%), Alert (≥90%), Exceeded (≥100%).
5. **Smart Spending Insights Engine**:
   - Evaluates month-over-month category differences, savings rate percentages, weekend vs weekday spending behaviors, and high concentration categories.
   - Rule-based algorithmic engine with optional Google Gemini AI deep analytics support.
6. **Recurring Payments**:
   - Automated tracking of Netflix, rent, gym, internet, utilities with frequency and next due date markers.
7. **Export & Import**:
   - Export all transactions as `expenses.csv` and `expenses.json`.
   - CSV importer with row validation, column verification, and preview before confirmation.

---

## 📱 Dual Platform Architecture

- **Native Android App (`app/`)**: Built in Kotlin & Jetpack Compose using Clean MVVM Architecture, Room Database (SQLite offline persistence), and reactive Flows. Matches the exact `#0B1420` dark navy and `#C9A24B` luxury gold fintech design.
- **Full-Stack Web App (`smart-expense-tracker/`)**: Python Flask backend, SQLite database, Chart.js visualizations, and Bootstrap 5 responsive UI.

---

## 🚀 How to Run the Web Application

```bash
cd smart-expense-tracker
pip install -r requirements.txt
python app.py
```

Then open your browser at:
`http://127.0.0.1:5000`

Default demo login:
- **Email**: `demo@smartfinance.com`
- **Password**: `Password@123`
