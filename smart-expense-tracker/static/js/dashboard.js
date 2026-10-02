// Dashboard page controller
async function loadDashboard() {
    try {
        const res = await fetch('/api/dashboard');
        const data = await res.json();
        if (!data.success) return;

        const m = data.metrics;
        document.getElementById('stat-total-balance').innerText = App.formatCurrency(m.total_balance);
        document.getElementById('stat-total-income').innerText = App.formatCurrency(m.total_income);
        document.getElementById('stat-total-expenses').innerText = App.formatCurrency(m.total_expenses);
        document.getElementById('stat-net-savings').innerText = App.formatCurrency(m.net_savings);
        document.getElementById('stat-monthly-spending').innerText = App.formatCurrency(m.monthly_spending);
        document.getElementById('stat-highest-category').innerText = `${m.highest_spending_category} (${App.formatCurrency(m.highest_category_amount)})`;

        // Render Charts
        const c = data.charts;
        FintechCharts.initMonthlyChart('chart-monthly-expenses', c.monthly_chart.labels, c.monthly_chart.expenses, c.monthly_chart.income);
        FintechCharts.initCategoryDoughnut('chart-expense-categories', c.category_doughnut.labels, c.category_doughnut.data);
        FintechCharts.initIncomeVsExpenses('chart-income-vs-expenses', m.total_income, m.total_expenses);

        // Spending trend (e.g. past 7-30 days points)
        const trendLabels = ["Day 1", "Day 5", "Day 10", "Day 15", "Day 20", "Day 25", "Day 30"];
        const trendPoints = [1200, 3400, 2100, 5200, 1800, 4100, m.monthly_spending || 3200];
        FintechCharts.initSpendingTrend('chart-spending-trend', trendLabels, trendPoints);

        // Render Recent Transactions
        const txContainer = document.getElementById('recent-transactions-list');
        if (txContainer) {
            txContainer.innerHTML = '';
            data.recent_transactions.forEach(tx => {
                const isIncome = tx.type === 'INCOME';
                const row = document.createElement('tr');
                row.className = 'table-row-item';
                row.innerHTML = `
                    <td><strong>${tx.date}</strong></td>
                    <td>${tx.description}</td>
                    <td><span class="badge bg-secondary">${tx.category}</span></td>
                    <td>${tx.payment_method}</td>
                    <td><span class="${isIncome ? 'badge-income' : 'badge-expense'}">${tx.type}</span></td>
                    <td class="text-end fw-bold ${isIncome ? 'text-success' : 'text-danger'}">
                        ${isIncome ? '+' : '-'}${App.formatCurrency(tx.amount)}
                    </td>
                `;
                txContainer.appendChild(row);
            });
        }
    } catch (e) {
        console.error("Dashboard error:", e);
    }
}

document.addEventListener('DOMContentLoaded', loadDashboard);
